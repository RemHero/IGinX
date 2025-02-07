package cn.edu.tsinghua.iginx.statistics.handler;

import cn.edu.tsinghua.iginx.engine.shared.operator.*;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.engine.shared.operator.type.OuterJoinType;
import cn.edu.tsinghua.iginx.engine.shared.source.FragmentSource;
import cn.edu.tsinghua.iginx.engine.shared.source.OperatorSource;
import cn.edu.tsinghua.iginx.engine.shared.source.Source;
import cn.edu.tsinghua.iginx.statistics.data.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * 注意ExtractColGroups(colGroups [][]*expression.Column)这里的colGroups代表的是从上至下传递下来的所有涉及到的列
 * 然后一层层来计算可能的ndv
 * 2024.12.24日，开始处理table scan 逻辑，对应到 tidb 就是 Source 的相关逻辑；然后发现需要补充之前的ExtractColGroups逻辑，因为这个逻辑是
 * 为了计算这个算子是否需要 多个列共同构成的数据 的ndv或者rowcount值
 */

public class StatsHandler {
  // SelectionFactor is the default factor of the selectivity.
  // For example, If we have no idea how to estimate the selectivity
  // of a Selection or a JoinCondition, we can use this default value.
  private static double DISTINCTFACTOR = 0.8;
  private static double SELECTIONFACTOR = 0.8;


  public StatsInfo recursiveDeriveStats(Operator operator, List<List<String>> colGroups) {
    List<Operator> operators = new ArrayList<>();
    // 按照operator类型递归获取stats
    if (operator instanceof BinaryOperator) {
      OperatorSource source1 = (OperatorSource) ((BinaryOperator) operator).getSourceA();
      OperatorSource source2 = (OperatorSource) ((BinaryOperator) operator).getSourceB();
      operators.add(source1.getOperator());
      operators.add(source2.getOperator());
    } else if (operator instanceof UnaryOperator) {
      Source source = ((UnaryOperator) operator).getSource();
      if (source instanceof OperatorSource) {
        operators.add(((OperatorSource) source).getOperator());
      }
    } else if (operator instanceof MultipleOperator) {
      List<Operator> convertedOperators =
          ((MultipleOperator) operator)
              .getSources().stream()
                  .filter(source -> source instanceof Operator) // 确保可以安全转换
                  .map(source -> (Operator) source) // 转换为Operator
                  .collect(Collectors.toList());
      operators.addAll(convertedOperators);
    }

    // 递归获取stats
    List<StatsInfo> statsInfos = new ArrayList<>();
    List<List<String>> sumColGroups = extractColGroups(operator, colGroups);
    for (Operator op : operators) {
      StatsInfo statsInfo = recursiveDeriveStats(op, sumColGroups);
      if (statsInfo != null) {
        // 得到子节点信息
        statsInfos.add(statsInfo);
      }
    }

    return DeriveStats(operator, statsInfos, colGroups);
  }

  List<List<String>> extractColGroups(Operator operator, List<List<String>> colGroups) {
    if (operator instanceof Filter) {
      //            return extractColGroups_((Filter)operator,colGroups);
    } else if (operator instanceof AbstractJoin) {
      return extractColGroups_((AbstractJoin) operator, colGroups);
    } else {
      return null;
    }
    return null;
  }

  private StatsInfo DeriveStats(
      Operator operator, List<StatsInfo> opStats, List<List<String>> colGroups) {
    AbstractOperator op = (AbstractOperator) operator;
    // cache hit
    if (op.getStatsInfo() != null) {
      return op.getStatsInfo();
    }

    if (operator instanceof Select) {
      return DeriveStats_((Select) operator, opStats, colGroups);
    } else if (operator instanceof Project) {
      return DeriveStats_((Project) operator, opStats, colGroups);
    } else if (operator instanceof AbstractJoin) {
      return DeriveStats_((AbstractJoin) operator, opStats, colGroups);
    } else {
      return DeriveStats_(op, opStats, colGroups);
    }
  }

  // default
  private StatsInfo DeriveStats_(
      AbstractOperator op, List<StatsInfo> opStats, List<List<String>> colGroups) {
    if (opStats.size() == 1) {
      op.setStatsInfo(opStats.get(0));
      return opStats.get(0);
    }

    if (opStats.size() > 1) {
      throw new java.lang.IllegalArgumentException(
          "LogicalPlans with more than one child should implement their own DeriveStats().");
    }

    if (op.getStatsInfo() != null) {
      return op.getStatsInfo();
    }

    StatsInfo profile = new StatsInfo(1, new HashMap<>(), null, null);
    op.setStatsInfo(profile);
    return profile;
  }

  private StatsInfo deriveStatsByFilter(Select select, Filter filter, List<String> accessPath) {
    StatsInfo statsInfo = select.getStatsInfo();
//    selectivity, nodes, err := ds.tableStats.HistColl.Selectivity(ds.ctx, conds, filledPaths)
//    Double selectivity = statsInfo.getHistColl().selectivity(filter, accessPath);
  }

  private StatsInfo DeriveStats_(
      Select select, List<StatsInfo> opStats, List<List<String>> colGroups) {
    if (select.getStatsInfo() != null) {
      return select.getStatsInfo();
    }


    StatsInfo childStats = opStats.get(0);
    Filter filter = select.getFilter();
    Double selectivity = childStats.getHistColl().selectivity(filter, accessPath);

    return childStats;
  }

  // TODO: 这里因为HistColl没有支持索引，所以没法支持获取GroupNDV
  private List<GroupNDV> getGroupNDVs(Project project, List<List<String>> colGroups) {
    if (colGroups.size() == 0) {
      return null;
    }

    List<GroupNDV> groupNDVs = new ArrayList<>();
    return groupNDVs;
  }

  // TODO: 这里需要考虑到colGroups的影响，以及构建HistColl！！！
  private void initStats(Project project, List<List<String>> colGroups) {
    if (project.getStatsInfo() != null) {
      project.getStatsInfo().setGroupNDVs(getGroupNDVs(project, colGroups));
    }

    // 构建HistColl
    if (project.getTableStatistic() == null) {
      TableStatHandler tbStatHandler = new TableStatHandler();
      // 在逻辑阶段，一般只有一列
      TableStatistic tableStatistic = tbStatHandler.getStatsTable(project.getPatterns().get(0));
      project.setTableStatistic(tableStatistic);
    }
  }

  private StatsInfo DeriveStats_(
      Project project, List<StatsInfo> opStats, List<List<String>> colGroups) {
    // the leaf node of the logical plan
    // like the table scan
    if (opStats.isEmpty()) {
      if (project.getStatsInfo() != null && colGroups.size() == 0) {
        return project.getStatsInfo();
      }

      initStats(project, colGroups);
      TableStatistic tableStatistic = project.getTableStatistic();
      StatsInfo profile =
          new StatsInfo(tableStatistic.getRowCount(), new TableStatistic(tableStatistic.getColumns()));

      for (String col : project.getPatterns()) {
        ColumnStatistic columnStatistic = tableStatistic.getColumn(col);
        if (tableStatistic != null) {
          if (columnStatistic.getCount() != null) {
            double factor = tableStatistic.getRowCount() / columnStatistic.getCount();
            profile.getColNDVMap().put(col, columnStatistic.getNDV() * factor);
          } else {
            profile.getColNDVMap().put(col, profile.getCount() * DISTINCTFACTOR);
          }
        }
      }

      project.setStatsInfo(profile);
      return profile;
    }

    // the project op
    StatsInfo childStats = opStats.get(0);
    if (project.getStatsInfo() != null) {
      return project.getStatsInfo();
    }
    // 直接获取子节点的ndv数据
    StatsInfo profile =
        new StatsInfo(
            childStats.getCount(),
            childStats.getColNDVMap(),
            childStats.getHistColl(),
            childStats.getGroupNDVs());
    project.setStatsInfo(profile);
    return profile;
  }

  // DeriveStats implement LogicalPlan DeriveStats interface.
  // If the type of join is SemiJoin, the selectivity of it will be same as selection's.
  // If the type of join is LeftOuterSemiJoin, it will not add or remove any row. The last column is
  // a boolean value, whose NDV should be two.
  // If the type of join is inner/outer join, the output of join(s, t) should be N(s) * N(t) /
  // (V(s.key) * V(t.key)) * Min(s.key, t.key).
  // N(s) stands for the number of rows in relation s. V(s.key) means the NDV of join key in s.
  // This is a quite simple strategy: We assume every bucket of relation which will participate join
  // has the same number of rows, and apply cross join for
  // every matched bucket.
  private StatsInfo DeriveStats_(AbstractJoin join, List<StatsInfo> opStats, List<List<String>> colGroups) {
    StatsInfo leftStats = opStats.get(0);
    StatsInfo rightStats = opStats.get(1);
    Double outCnt = 0.0;

    if (join.getStatsInfo() != null) {
      // Reload GroupNDVs since colGroups may have changed.
      join.getStatsInfo().setGroupNDVs(getGroupNDVs(join, opStats));
      return join.getStatsInfo();
    }

    if (join instanceof CrossJoin) {
      outCnt = RowCountHandler.estimateFullJoinRowCount(true, leftStats, rightStats, null);
    } else if (join instanceof InnerJoin) {
      InnerJoin IJoin = (InnerJoin) join;
      List<String> keys = IJoin.getJoinColumns();
      outCnt =
          RowCountHandler.estimateFullJoinRowCount(
              IJoin.getFilter() != null, leftStats, rightStats, keys);
      //        } else if (join instanceof MarkJoin || join instanceof SingleJoin) {

    } else if (join instanceof OuterJoin) {
      OuterJoin OJoin = (OuterJoin) join;
      if (OJoin.getOuterJoinType() == OuterJoinType.LEFT) {
        outCnt = Math.max(outCnt, leftStats.getCount());
      } else if (OJoin.getOuterJoinType() == OuterJoinType.RIGHT) {
        outCnt = Math.max(outCnt, rightStats.getCount());
      }
    }
    Map<String, Double> colsNDV = new HashMap<>();

    for (String col : leftStats.getColNDVMap().keySet()) {
      Map<String, Double> leftColsNDV = leftStats.getColNDVMap();
      colsNDV.put(col, Math.min(leftColsNDV.get(col), outCnt));
    }
    for (String col : rightStats.getColNDVMap().keySet()) {
      Map<String, Double> rightColsNDV = rightStats.getColNDVMap();
      colsNDV.put(col, Math.min(rightColsNDV.get(col), outCnt));
    }

    StatsInfo joinStats = new StatsInfo(outCnt.longValue(), colsNDV);
    // Reload GroupNDVs since colGroups may have changed.
    joinStats.setGroupNDVs(getGroupNDVs(join, opStats));
    join.setStatsInfo(joinStats);

    return joinStats;
  }

  List<List<String>> extractColGroups_(AbstractJoin join, List<List<String>> colGroups) {
    // 这里不仅要处理 getJoinColumns
    Filter filter = null;
    // 仅仅在using 语句时才会指定 JoinColumns
    if (join instanceof InnerJoin) {
      InnerJoin IJoin = (InnerJoin) join;
      List<String> joinKey = IJoin.getJoinColumns();
      if (joinKey != null && !joinKey.isEmpty()) {
        colGroups.add(joinKey);
      }
      filter = IJoin.getFilter();
    } else if (join instanceof OuterJoin) {
      OuterJoin OJoin = (OuterJoin) join;
      List<String> joinKey = OJoin.getJoinColumns();
      if (joinKey != null && !joinKey.isEmpty()) {
        colGroups.add(joinKey);
      }
      filter = OJoin.getFilter();
    }

    // TODO: 还要考虑到 filter效
    if (filter != null) {}

    return colGroups;
  }

  public List<GroupNDV> getGroupNDVs(AbstractJoin join, List<StatsInfo> childStats) {
    int outerIdx = -1;
    if (join instanceof OuterJoin) {
      OuterJoin OJoin = (OuterJoin) join;
      if (OJoin.getOuterJoinType() == OuterJoinType.LEFT) {
        outerIdx = 0;
      } else if (OJoin.getOuterJoinType() == OuterJoinType.RIGHT) {
        outerIdx = 1;
      }
    }
    if (outerIdx >= 0) {
      return childStats.get(outerIdx).getGroupNDVs();
    }
    return null;
  }
}
