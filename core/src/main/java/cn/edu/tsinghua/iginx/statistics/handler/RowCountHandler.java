package cn.edu.tsinghua.iginx.statistics.handler;

import static cn.edu.tsinghua.iginx.statistics.handler.NdvHandler.EstimateColsNDVWithMatchedLen;
import static cn.edu.tsinghua.iginx.statistics.handler.TableStatHandler.KEY_COLUMN_NAME;

import cn.edu.tsinghua.iginx.engine.shared.data.Value;
import cn.edu.tsinghua.iginx.engine.shared.operator.*;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.statistics.data.*;
import cn.edu.tsinghua.iginx.statistics.util.RangeUtil;
import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
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

    // deal with join by
    if (joinKeys.size() == 1) {
      ColumnStatistic left = leftProfile.getHistColl().getColumn(joinKeys.get(0));
      ColumnStatistic right = rightProfile.getHistColl().getColumn(joinKeys.get(0));

      List<Bounds> leftBounds = left.getHistogram().getBounds();
      List<Bounds> rightBounds = right.getHistogram().getBounds();

      Value leftLowV = leftBounds.get(0).getLowerValue();
      Value leftUpV = leftBounds.get(leftBounds.size() - 1).getUpperValue();
      Value rightLowV = rightBounds.get(0).getLowerValue();
      Value rightUpV = rightBounds.get(rightBounds.size() - 1).getUpperValue();

      long leftCount = left.getCount();
      long rightCount = right.getCount();
      Range leftRange = new Range(leftLowV, leftUpV);
      Range rightRange = new Range(rightLowV, rightUpV);
      Pair<Range, Boolean> result = RangeUtil.intersect(leftRange, rightRange);
      if (result.getValue()) {
        double leftIntersectCount = left.getColumnRowCount(Collections.singletonList(result.getKey()));
        double rightIntersectCount = right.getColumnRowCount(Collections.singletonList(result.getKey()));
        if (joinKeys.get(0).equals(KEY_COLUMN_NAME)) {
          // deal with join by
          return leftCount + rightCount - Math.min(leftIntersectCount, rightIntersectCount);
        } else {
          return Math.max(leftIntersectCount, rightIntersectCount);
        }
      }
    }

    return leftProfile.getCount() * rightProfile.getCount() / Math.max(leftKeyNDV, rightKeyNDV);
  }
}
