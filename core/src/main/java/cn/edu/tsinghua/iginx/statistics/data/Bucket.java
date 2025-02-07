package cn.edu.tsinghua.iginx.statistics.data;

public class Bucket {
  private long count;

  private long repeat;

  private long ndv;

  public Bucket() {
    this.count = 0;
    this.repeat = 0;
    this.ndv = 0;
  }

  // 构造函数
  public Bucket(long count, long repeat, long ndv) {
    this.count = count;
    this.repeat = repeat;
    this.ndv = ndv;
  }

  // 获取桶内元素数量
  public long getCount() {
    return count;
  }

  // 设置桶内元素数量
  public void setCount(long count) {
    this.count = count;
  }

  // 获取桶值重复次数
  public long getRepeat() {
    return repeat;
  }

  // 设置桶值重复次数
  public void setRepeat(long repeat) {
    this.repeat = repeat;
  }

  // 获取桶内不同值数量
  public long getNdv() {
    return ndv;
  }

  // 设置桶内不同值数量
  public void setNdv(long ndv) {
    this.ndv = ndv;
  }

  public void addCount(long count) {
    this.count += count;
  }

  public void addRepeat(long repeat) {
    this.repeat += repeat;
  }

  public String toString() {
    return "Bucket{" +
            "count=" + count +
            ", repeat=" + repeat +
            ", ndv=" + ndv +
            '}';
  }
}
