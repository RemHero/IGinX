package cn.edu.tsinghua.iginx.statistics.data;

import cn.edu.tsinghua.iginx.engine.shared.data.Value;
import cn.edu.tsinghua.iginx.statistics.util.ValueUtil;
import cn.edu.tsinghua.iginx.thrift.DataType;

import java.util.List;

public class ColumnStatistic {
  private Histogram histogram;
  private long count;

  public ColumnStatistic(Histogram histogram, long count) {
    this.histogram = histogram;
    this.count = count;
  }

  private double equalRowCount(Value value) {
    if (value.isNull()) return 0;
    if (histogram.getBounds()== null || histogram.getBounds().isEmpty()) {
      return 0;
    }
    return histogram.equalRowCount(value);
  }

  private double betweenRowCount(Value lower, Value upper) {
    double lessCountA = histogram.lessRowCount(lower);
    double lessCountB = histogram.lessRowCount(upper);
    // If lessCountA is not less than lessCountB, it may be that they fall to the same bucket and we cannot estimate
    // the fraction, so we use `totalCount / NDV` to estimate the row count, but the result should not greater than lessCountB.
    if (lessCountA >= lessCountB && histogram.getNDV() > 0) {
      return Math.min(lessCountB, histogram.totalRowCount()/histogram.getNDV());
    }
    return lessCountB - lessCountA;
  }

  public double getColumnRowCount(List<Range> ranges) {
    double ret = 0;
    for (Range range : ranges) {
      Value lower = range.getLowVal();
      Value upper = range.getHighVal();
      int cmp = ValueUtil.compareTo(lower, upper);
      if (cmp==0) {
        // the point case.
        if (!range.LowExclude && !range.HighExclude) {
          ret += equalRowCount(lower);
        }
        continue;
      }
      // the interval case.
      ret+=betweenRowCount(lower, upper);
      if (range.isLowExclude()) {
        ret-=equalRowCount(lower);
      }
      if (!range.isHighExclude()) {
        ret+=equalRowCount(upper);
      }
    }
    double totalRowCount = histogram.totalRowCount();
    if (ret>totalRowCount) {
      return totalRowCount;
    } else if(ret<0) {
      return 0;
    }
    return ret;
  }

  public DataType getDataType() {
    return histogram.getBounds().get(0).getDataType();
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

  public Range getRange() {
    return histogram.getRange();
  }

  public String toString() {
    return "ColumnStatistic{" +
            "histogram=" + histogram +
            ", count=" + count +
            '}';
  }
}
