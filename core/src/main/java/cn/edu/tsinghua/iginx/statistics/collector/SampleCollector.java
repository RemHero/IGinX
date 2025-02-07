package cn.edu.tsinghua.iginx.statistics.collector;

import cn.edu.tsinghua.iginx.statistics.Statistics;
import cn.edu.tsinghua.iginx.thrift.DataType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleCollector extends AbstractStatisticsCollector {
  private static final CollectorType collectorType = CollectorType.SampleCollector;

  List<SampleItem> samples = new ArrayList<>();
  int nullCount = 0;
  int count = 0;
  int seenValues = 0; // seenValues is the current seen values.
  int maxSampleSize = 0;
  int totalSize = 0; // TODO: totalSize is the total size of the data.
  int memSize = 0;
  boolean isMerger = false;

  public SampleCollector(int mazSampleSize) {
    this.maxSampleSize = mazSampleSize;
  }

  public void collect(Object value, DataType dataType) {
    if (!isMerger) {
      if (value == null) {
        nullCount++;
        return;
      }
      count++;
    }
    seenValues++;
    if (samples.size() < maxSampleSize) {
      SampleItem sampleItem = new SampleItem();
      sampleItem.value = value;
      sampleItem.dataType = dataType;
      samples.add(sampleItem);
    } else {
      Random rand = new Random();
      // 蓄水池算法随机采样
      boolean shouldAdd = rand.nextInt(seenValues) < maxSampleSize;
      if (shouldAdd) {
        int idx = rand.nextInt(maxSampleSize);
        SampleItem newItem = new SampleItem();
        newItem.value = value;
        newItem.dataType = dataType;
        // To keep the order of the elements, we use remove and add, not direct replacement.
        samples.remove(idx);
        samples.add(newItem);
      }
    }
  }
  

  public List<SampleItem> getSamples() {
    return samples;
  }

  public int getNullCount() {
    return nullCount;
  }

  public int getCount() {
    return count;
  }

  public int getTotalSize() {
    return totalSize;
  }

  @Override
  protected CollectorType getCollectorType() {
    return collectorType;
  }

  @Override
  protected void processStatistics(Statistics statistics) {}

  @Override
  public void broadcastStatistics() {}

  public class SampleItem {
    Object value;
    DataType dataType;
    int ordinal;

    public Object getValue() {
      return value;
    }

    public DataType getDataType() {
      return dataType;
    }

    public int getOrdinal() {
      return ordinal;
    }

    public void setOrdinal(int ordinal) {
      this.ordinal = ordinal;
    }

    public void setValue(Object value) {
      this.value = value;
    }

    public void setDataType(DataType dataType) {
      this.dataType = dataType;
    }
  }
}
