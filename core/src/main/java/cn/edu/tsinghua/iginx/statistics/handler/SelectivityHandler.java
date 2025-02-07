package cn.edu.tsinghua.iginx.statistics.handler;

import cn.edu.tsinghua.iginx.engine.shared.operator.*;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;

public class SelectivityHandler implements StatisticHandler.Selectivity {
  public final Selectivity provider;

  // 不同的RowCount计算方式
  public SelectivityHandler(Selectivity provider) {
    this.provider = provider;
  }

  @Override
  public Double getSelectivity(Operator operator, Filter filter) {
    // 做一些统一的前提判断  以及  cache的实现
    return getSelectivity_(operator, filter);
  }

  Double getSelectivity_(Operator operator, Filter filter) {
    if (operator instanceof Union) {
      return provider.getSelectivity((Project) operator, filter);
    } else if (operator instanceof AbstractJoin) {
      return provider.getSelectivity((AbstractJoin) operator, filter);
    } else {
      throw new java.lang.IllegalArgumentException(
          "No handler for operator " + operator.getClass());
    }
  }
}
