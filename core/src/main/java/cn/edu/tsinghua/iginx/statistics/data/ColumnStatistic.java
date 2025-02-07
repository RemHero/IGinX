package cn.edu.tsinghua.iginx.statistics.data;

public class ColumnStatistic {
  private Histogram histogram;
  private long count;

  public ColumnStatistic(Histogram histogram, long count) {
    this.histogram = histogram;
    this.count = count;
  }

  public Histogram getHistogram() {
    return histogram;
  }

  public Long getCount() {
    return count;
  }

  public Long getNDV() {
    return histogram.getNDV();
  }

  public String toString() {
    return "ColumnStatistic{" +
            "histogram=" + histogram +
            ", count=" + count +
            '}';
  }
}
