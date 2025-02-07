package cn.edu.tsinghua.iginx.statistics.handler;

import cn.edu.tsinghua.iginx.engine.shared.operator.Operator;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import javax.annotation.Nullable;

public abstract class StatisticHandler {
  public interface Selectivity {
    @Nullable
    Double getSelectivity(Operator operator, Filter filter);
  }

  public interface RowCount {
    @Nullable
    Double getRowCount(Operator operator);
  }
}
