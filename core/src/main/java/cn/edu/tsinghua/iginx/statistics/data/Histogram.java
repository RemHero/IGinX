package cn.edu.tsinghua.iginx.statistics.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Histogram {
  private List<Bounds> bounds = new ArrayList<>();

  private List<Bucket> buckets = new ArrayList<>();

  private List<Scalar> scalars = new ArrayList<>();

  private long id;

  private long ndv;

  private long nullCount;

  private long lastUpdateVersion;

  private long totColSize;

  private double correlation;

  private int maxBucketSize;
  private int valuesPerBucket = 1;
  private int bucketIdx = 0;
  private int lastNumber = 0;

  public Histogram(int mazBucketSize) {
    this.maxBucketSize = mazBucketSize;
  }

  // 构造函数
  public Histogram(long ndv, long nullCount, long bucketSize, long totColSize) {
    List<Bounds> bounds = new ArrayList<>();
    List<Bucket> buckets = new ArrayList<>();
    for (int i = 0; i < bucketSize; i++) {
      buckets.add(new Bucket());
    }
    this.ndv = ndv;
    this.nullCount = nullCount;
    this.totColSize = totColSize;
  }

  // 构造函数
  public Histogram(
      List<Bounds> bounds,
      List<Bucket> buckets,
      List<Scalar> scalars,
      long id,
      long ndv,
      long nullCount,
      long lastUpdateVersion,
      long totColSize,
      double correlation) {
    this.bounds = bounds;
    this.buckets = buckets;
    this.scalars = scalars;
    this.id = id;
    this.ndv = ndv;
    this.nullCount = nullCount;
    this.lastUpdateVersion = lastUpdateVersion;
    this.totColSize = totColSize;
    this.correlation = correlation;
  }

  public void updateLastBucket(
      int bucketIdx, Object value, long count, long repeat, boolean needBucketNDV) {
    int len = buckets.size();
    bounds.set(bucketIdx, buildBounds(bounds.get(bucketIdx).getLowerBound(), value));
    buckets.get(bucketIdx).setCount(count);
    buckets.get(bucketIdx).setRepeat(repeat);
  }

  public void appendBucket(Object lowerBound, Object upperBound, long count, long repeatCount) {
    appendBucketWithNDV(lowerBound, upperBound, count, repeatCount, 0);
  }

  private <T extends Comparable<T> & Serializable> Bounds<T> buildBounds(
      Object lowerObj, Object upperObj) {
    // 首先检查是否为 null
    if (lowerObj == null || upperObj == null) {
      throw new IllegalArgumentException("Lower and upper bounds cannot be null");
    }
    // 检查是否实现了 Comparable 和 Serializable 接口
    if (!(lowerObj instanceof Comparable && lowerObj instanceof Serializable)
        || !(upperObj instanceof Comparable && upperObj instanceof Serializable)) {
      throw new IllegalArgumentException(
          "Lower and upper bounds must implement Comparable and Serializable");
    }
    // 获取类对象
    Class<?> lowerClass = lowerObj.getClass();
    Class<?> upperClass = upperObj.getClass();
    // 检查上下界的类型是否一致
    if (!lowerClass.equals(upperClass)) {
      throw new IllegalArgumentException("Lower and upper bounds must be of the same type");
    }
    // 进行类型转换
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) lowerClass;
    try {
      T lower = (T) lowerObj;
      T upper = (T) upperObj;
      // 比较上下界
      return new Bounds<>(lower, upper, type);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void appendBucketWithNDV(
      Object lowerBound, Object upperBound, long count, long repeatCount, long ndv) {
    buckets.add(new Bucket(count, repeatCount, ndv));
    bounds.add(buildBounds(lowerBound, upperBound));
  }

  public Object getUpper(int index) {
    return bounds.get(index).getUpperBound();
  }

  public Object getLower(int index) {
    return bounds.get(index).getLowerBound();
  }

  // 获取边界数据块
  public List<Bounds> getBounds() {
    return bounds;
  }

  // 设置边界数据块
  public void setBounds(List<Bounds> bounds) {
    this.bounds = bounds;
  }

  // 获取存储桶列表
  public List<Bucket> getBuckets() {
    return buckets;
  }

  // 设置存储桶列表
  public void setBuckets(List<Bucket> buckets) {
    this.buckets = buckets;
  }

  // 获取scalar列表
  public List<Scalar> getScalars() {
    return scalars;
  }

  // 设置scalar列表
  public void setScalars(List<Scalar> scalars) {
    this.scalars = scalars;
  }

  // 获取列ID
  public long getId() {
    return id;
  }

  // 设置列ID
  public void setId(long id) {
    this.id = id;
  }

  // 获取不同值数量
  public long getNdv() {
    return ndv;
  }

  // 设置不同值数量
  public void setNdv(long ndv) {
    this.ndv = ndv;
  }

  // 获取空值数量
  public long getNullCount() {
    return nullCount;
  }

  // 设置空值数量
  public void setNullCount(long nullCount) {
    this.nullCount = nullCount;
  }

  // 获取上次更新版本
  public long getLastUpdateVersion() {
    return lastUpdateVersion;
  }

  // 设置上次更新版本
  public void setLastUpdateVersion(long lastUpdateVersion) {
    this.lastUpdateVersion = lastUpdateVersion;
  }

  // 获取总列大小
  public long getTotColSize() {
    return totColSize;
  }

  // 设置总列大小
  public void setTotColSize(long totColSize) {
    this.totColSize = totColSize;
  }

  // 获取统计相关性
  public double getCorrelation() {
    return correlation;
  }

  // 设置统计相关性
  public void setCorrelation(double correlation) {
    this.correlation = correlation;
  }

  public Long getNDV() {
    return ndv;
  }

  public int getBucketIdx() {
    return bucketIdx;
  }

  public void addBucketIdx() {
    this.bucketIdx++;
  }

  public int getLastNumber() {
    return lastNumber;
  }

  public String toString() {
    return "Histogram{" +
            "\nbounds=" + bounds +
            "\n, buckets=" + buckets +
            "\n, scalars=" + scalars +
            "\n, id=" + id +
            "\n, ndv=" + ndv +
            "\n, nullCount=" + nullCount +
            "\n, lastUpdateVersion=" + lastUpdateVersion +
            "\n, totColSize=" + totColSize +
            "\n, correlation=" + correlation +
            '}';
  }
}
