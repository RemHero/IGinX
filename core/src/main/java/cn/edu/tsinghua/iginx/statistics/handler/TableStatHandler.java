package cn.edu.tsinghua.iginx.statistics.handler;

import static cn.edu.tsinghua.iginx.utils.ByteUtils.getValueFromByteBufferByDataType;

import cn.edu.tsinghua.iginx.engine.ContextBuilder;
import cn.edu.tsinghua.iginx.engine.StatementExecutor;
import cn.edu.tsinghua.iginx.engine.shared.RequestContext;
import cn.edu.tsinghua.iginx.engine.shared.Result;
import cn.edu.tsinghua.iginx.statistics.collector.SampleCollector;
import cn.edu.tsinghua.iginx.statistics.data.ColumnStatistic;
import cn.edu.tsinghua.iginx.statistics.data.Histogram;
import cn.edu.tsinghua.iginx.statistics.data.TableStatistic;
import cn.edu.tsinghua.iginx.thrift.DataType;
import cn.edu.tsinghua.iginx.thrift.ExecuteSqlReq;
import cn.edu.tsinghua.iginx.utils.Bitmap;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;
import javafx.util.Pair;

// 对应 Handle 类
public class TableStatHandler {
  public static boolean flag = true;
  Logger LOGGER = Logger.getLogger(TableStatHandler.class.getName());
  ContextBuilder contextBuilder = ContextBuilder.getInstance();
  Map<String, TableStatistic> cache = new HashMap<>();
  final String DATA_RANGE = "select * from %s where key >= %d and key <= %d;";
  final String DATA_SEARCH = "select";
  final String COLUMN_SCAN = "select %s from %s;";
  final String STATISTICS_QUERY = "select";
  public static final String KEY_COLUMN_NAME = "key";
  final int MAX_BUCKET_SIZE = 20;
  final int MAX_SAMPLE_SIZE = 2000;
  StatementExecutor executor = StatementExecutor.getInstance();

  // 加载缓存
  void loadCache() {}

  /*
   * 1、首先通过底层数据源直接获取统计信息
   * 2、通过SQL采样
   */
  // 参考 func (h *Handle) Update 函数
  public void update(String name) {
    if (cache != null && !cache.isEmpty() && cache.containsKey(name)) {
      return;
    }
    TableStatistic tableStatistic = null;
    if (updateByQuery(name)) {
      return;
    }
    tableStatistic = updateByCalculate(name);
    if (tableStatistic == null) {
      LOGGER.warning("Failed to update statistics for table " + name);
      // 设置默认统计信息
      cache.put(name, mockTableStats(name));
      return;
    }
    cache.put(name, tableStatistic);
  }

  public Pair<Long, Long> getBoundary(String name) {
    return null;
  }

  public final boolean hasMore(Result result, int index) {
    List<ByteBuffer> valuesList = result.getValuesList();
    List<ByteBuffer> bitmapList = result.getBitmapList();
    if (valuesList != null && index < valuesList.size()) {
      return true;
    }
    bitmapList = null;
    valuesList = null;
    index = 0;
    return false;
  }

  public final Pair<Object[], List<DataType>> nextRow(Result result, int index) {
    if (!hasMore(result, index)) {
      return null;
    }
    List<ByteBuffer> valuesList = result.getValuesList();
    List<ByteBuffer> bitmapList = result.getBitmapList();
    List<DataType> dataTypeList = new ArrayList<>(result.getDataTypeList());
    // nextRow 只会返回本地的 row，如果本地没有，在进行 hasMore 操作时候，就一定也已经取回来了
    ByteBuffer valuesBuffer = valuesList.get(index);
    ByteBuffer bitmapBuffer = bitmapList.get(index);
    Bitmap bitmap = new Bitmap(dataTypeList.size(), bitmapBuffer.array());
    //最后一个对应key列
    Object[] values = new Object[dataTypeList.size()+1];
    for (int i = 0; i < dataTypeList.size(); i++) {
      if (bitmap.get(i)) {
        values[i] = getValueFromByteBufferByDataType(valuesBuffer, dataTypeList.get(i));
      }
    }
    // deal with key column
    values[dataTypeList.size()] = result.getKeys()[index];
    dataTypeList.add(DataType.LONG);
    return new Pair<>(values, dataTypeList);
  }

  List<SampleCollector> collectColumnStats(Result result) {
    List<SampleCollector> collectors = new ArrayList<>();
    for (int i = 0; i < result.getDataTypeList().size(); i++) {
      collectors.add(new SampleCollector(MAX_SAMPLE_SIZE));
    }
    // for key column这里需要注意
    collectors.add(new SampleCollector(MAX_SAMPLE_SIZE));

    // 采样
    int index = 0;
    while (hasMore(result, index)) {
      Pair<Object[], List<DataType>> pair = nextRow(result, index++);
      Object[] values = pair.getKey();
      // 实际上只会有一列数据（或者说是key和value）
      List<DataType> dataTypeList = pair.getValue();
      for (int i = 0; i < values.length; i++) {
        collectors.get(i).collect(values[i], dataTypeList.get(i));
      }
    }

    for (SampleCollector collector : collectors) {
      int idx = 0;
      for (SampleCollector.SampleItem item : collector.getSamples()) {
        item.setOrdinal(idx++);
      }
    }

    return collectors;
  }

  public int compare(SampleCollector.SampleItem item1, SampleCollector.SampleItem item2) {
    // 假设 Value 是可比较的对象，如果不是可比较的对象，需要根据具体情况进行转换或处理
    // 同时假设 item1 和 item2 的 Value 不会为 null
    if (item1.getValue() instanceof Comparable && item2.getValue() instanceof Comparable) {
      Comparable<Object> value1 = (Comparable<Object>) item1.getValue();
      Comparable<Object> value2 = (Comparable<Object>) item2.getValue();
      return value1.compareTo(item2.getValue());
    } else {
      // 如果无法比较，抛出异常或进行其他处理，这里简单地返回 0
      return 0;
    }
  }

  // buildHist builds histogram from samples and other information.
  // It stores the built histogram in hg and return corrXYSum used for calculating the correlation.
  double buildHist(
      Histogram hist,
      long count,
      long nullcount,
      long ndv,
      int bucketNum,
      List<SampleCollector.SampleItem> sampleItems) {
    int sampleNum = sampleItems.size();
    // As we use samples to build the histogram, the bucket number and repeat should multiply a
    // factor.
    double sampleFactor = count / sampleNum;
    double ndvFactor = ndv / sampleNum;
    if (ndvFactor > sampleFactor) {
      ndvFactor = sampleFactor;
    }
    // Since bucket count is increased by sampleFactor, so the actual max values per bucket is
    // floor(valuesPerBucket/sampleFactor)*sampleFactor, which may less than valuesPerBucket,
    // thus we need to add a sampleFactor to avoid building too many buckets.
    double valuesPerBucket = count / bucketNum + sampleFactor;

    int bucketIdx = 0;
    long lastCount = 0;
    double corrXYSum = 0;

    hist.appendBucket(
        sampleItems.get(0).getValue(),
        sampleItems.get(0).getValue(),
        (int) sampleFactor,
        (int) ndvFactor);
    for (int i = 1; i < sampleNum; i++) {
      SampleCollector.SampleItem item = sampleItems.get(i);
      corrXYSum += i * item.getOrdinal();
      Object value = item.getValue();
      boolean sameToUpper = hist.getBounds().get(bucketIdx).isSameToUpper((Comparable) value);
      long totalCount = (i + 1) * (long) sampleFactor;
      if (sameToUpper) {
        // The new item has the same value as current bucket value, to ensure that
        // a same value only stored in a single bucket, we do not increase bucketIdx even if it
        // exceeds
        // valuesPerBucket.
        hist.getBuckets().get(bucketIdx).setCount(totalCount);
        if (hist.getBuckets().get(bucketIdx).getRepeat() == ndvFactor) {
          hist.getBuckets().get(bucketIdx).setRepeat(2 * (long) sampleFactor);
        } else {
          hist.getBuckets().get(bucketIdx).addRepeat((long) sampleFactor);
        }
      } else if (totalCount - lastCount <= valuesPerBucket) {
        // The bucket still have room to store a new item, update the bucket.
        hist.updateLastBucket(bucketIdx, value, totalCount, (long) ndvFactor, false);
      } else {
        lastCount = hist.getBuckets().get(bucketIdx).getCount();
        // The bucket is full, store the item in the next bucket.
        bucketIdx++;
        hist.appendBucket(value, value, totalCount, (int) ndvFactor);
      }
    }
    return corrXYSum;
  }

  // 通过采样计算统计信息
  // 参考 CollectColumnStats 函数
  TableStatistic updateByCalculate(String name) {
    // TODO: 对接层需要支持 min,max,count接口，以获取数据范围。如果不支持，则需要通过二分法查找边界（可以作为第一版实现方案）
    /*
    // getBoundary(name);
    RequestContext ctx = contextBuilder.build(new ExecuteSqlReq(0, COLUMN_SCAN));
    executor.execute(ctx);
    Result result = ctx.getResult();

    long begin = 0, end = 0;
    int interval=1;
    for (long i=begin; i<=end; i+=interval) {
        ctx = contextBuilder.build(new ExecuteSqlReq(0, DATA_RANGE));
        executor.execute(ctx);
        result = ctx.getResult();
    }
    */
    // version 1 先通过scan获取数据
    // 实际在处理的逻辑参考 (h *Handle) TableStatsFromStorage -> func TableStatsFromStorage ...-> func
    // HistogramFromStorage
    int lastDotIndex = name.lastIndexOf('.');
    // 截取最后一个点之后的部分
    String subString1 = name.substring(lastDotIndex + 1);
    // 截取从开始到最后一个点之前的部分
    String subString2 = name.substring(0, lastDotIndex);
    RequestContext ctx =
        contextBuilder.build(new ExecuteSqlReq(0,
            String.format(COLUMN_SCAN, subString1, subString2)
//            "select s1 from us.d1;"
        ));
    ctx.setIsStatistic(true);
    // TODO: 发现了一个BUG，就是通过select * 的方式获取的数据，在pattern解析阶段会无法识别，比如，a,b,*
    executor.execute(ctx);
    Result result = ctx.getResult();

    List<SampleCollector> collectors = collectColumnStats(result);
    Map<String, ColumnStatistic> columns = new HashMap<>();

    // TODO:LHZ 未来考虑 FMSketch 的ndv值
    int maxCount = -1;
    for (int i = 0; i < collectors.size(); i++) {
      SampleCollector collector = collectors.get(i);
      String path = null;
      int ndv = 0;
      if (i==collectors.size()-1) {
        path = KEY_COLUMN_NAME;
        ndv = collector.getSamples().size();
      } else {
        path = result.getPaths().get(i);
      }
      int count = collector.getCount();
      maxCount = Math.max(maxCount, count);
      int nullCount = collector.getNullCount();
      int totalSize = collector.getTotalSize();

      List<SampleCollector.SampleItem> samples = collector.getSamples();

      if (count == 0 || samples.size() == 0) {
        columns.put(path, new ColumnStatistic(new Histogram(0, nullCount, 0, totalSize), count));
        continue;
      }

      // 对采样值进行排序
      Collections.sort(
          samples,
          (item1, item2) -> {
            if (item1.getValue() instanceof Comparable && item2.getValue() instanceof Comparable) {
              Comparable<Object> value1 = (Comparable<Object>) item1.getValue();
              Comparable<Object> value2 = (Comparable<Object>) item2.getValue();
              return value1.compareTo(item2.getValue());
            } else {
              // 如果无法比较，抛出异常或进行其他处理，这里简单地返回 0
              return 0;
            }
          });

      Histogram hist = new Histogram(ndv, nullCount, MAX_BUCKET_SIZE, totalSize);
      double corrXYSum = buildHist(hist, count, nullCount, ndv, MAX_BUCKET_SIZE, samples);
      hist.setCorrelation(corrXYSum);

      columns.put(path, new ColumnStatistic(hist, count));
    }

    TableStatistic tableStatistic = new TableStatistic(maxCount, columns);
    return tableStatistic;
  }

  // TODO: 对接层支持统计信息直接查询
  boolean updateByQuery(String name) {
    // STATISTICS_QUERY
    // 收集数据
//    StatementExecutor executor = StatementExecutor.getInstance();
//    RequestContext ctx = contextBuilder.build(new ExecuteSqlReq(0, STATISTICS_QUERY));
//    executor.execute(ctx);
//    Result result = ctx.getResult();

    return false;
  }

  TableStatistic mockTableStats(String name) {
    TableStatistic tableStatistic = new TableStatistic(0, null);
    return tableStatistic;
  }

  // 根据列名name获取表的统计信息
  public TableStatistic getStatsTableWithKey(String name) {
    if (cache == null || cache.isEmpty()) {
      loadCache();
    }
    if (cache.containsKey(name)) {
      return cache.get(name);
    }

    update(name);
    return cache.get(name);
  }
}
