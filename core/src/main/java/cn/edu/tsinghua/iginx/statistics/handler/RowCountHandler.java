package cn.edu.tsinghua.iginx.statistics.handler;

import static cn.edu.tsinghua.iginx.statistics.handler.NdvHandler.EstimateColsNDVWithMatchedLen;

import cn.edu.tsinghua.iginx.engine.shared.operator.*;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.statistics.data.StatsInfo;
import java.util.List;

public class RowCountHandler implements StatisticHandler.RowCount {
  public final RowCount provider;

  // 不同的RowCount计算方式
  public RowCountHandler(RowCount provider) {
    this.provider = provider;
  }

  @Override
  public Double getRowCount(Operator operator) {
    // 做一些统一的前提判断  以及  cache的实现
    return getRowCount_(operator);
  }

  Double getRowCount_(Operator operator) {
    if (operator instanceof Filter) {
      return provider.getRowCount((Filter) operator);
    }
    if (operator instanceof Project) {
      return provider.getRowCount((Project) operator);
    } else if (operator instanceof AbstractJoin) {
      return provider.getRowCount((AbstractJoin) operator);
    } else {
      throw new java.lang.IllegalArgumentException(
          "No handler for operator " + operator.getClass());
    }
  }

  static Double estimateFullJoinRowCount(
      boolean isCartesian, StatsInfo leftProfile, StatsInfo rightProfile, List<String> joinKeys) {
    if (isCartesian) {
      return (double) leftProfile.getCount() * (double) rightProfile.getCount();
    }
    Double leftKeyNDV, rightKeyNDV;
    leftKeyNDV = EstimateColsNDVWithMatchedLen(joinKeys, leftProfile);
    rightKeyNDV = EstimateColsNDVWithMatchedLen(joinKeys, rightProfile);

    return leftProfile.getCount() * rightProfile.getCount() / Math.max(leftKeyNDV, rightKeyNDV);
  }
}
