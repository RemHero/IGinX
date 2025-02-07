package cn.edu.tsinghua.iginx.statistics.handler;

import cn.edu.tsinghua.iginx.engine.shared.operator.*;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.engine.shared.operator.type.OuterJoinType;
import cn.edu.tsinghua.iginx.engine.shared.source.OperatorSource;
import cn.edu.tsinghua.iginx.engine.shared.source.Source;
import cn.edu.tsinghua.iginx.engine.shared.source.SourceType;
import java.util.List;
import javax.annotation.Nullable;

public class RowCount {
  RowCountHandler rowCountHandler = new RowCountHandler(this);
  SelectivityHandler selectivityHandler = new SelectivityHandler(new Selectivity());

  public @Nullable Double getRowCount(AbstractJoin join) {
    Source sourceA = join.getSourceA();
    Operator opA = null, opB = null;
    if (sourceA.getType() == SourceType.Operator) {
      opA = ((OperatorSource) sourceA).getOperator();
    }
    Source sourceB = join.getSourceB();
    if (sourceB.getType() == SourceType.Operator) {
      opB = ((OperatorSource) sourceA).getOperator();
    }

    Double rowsA = null;
    if (opA != null) {
      rowsA = rowCountHandler.getRowCount(opA);
    }
    Double rowsB = null;
    if (opB != null) {
      rowsB = rowCountHandler.getRowCount(opB);
    }

    if (rowsA == null || rowsB == null) {
      return null;
    }

    if (join instanceof CrossJoin) {
      // CrossJoin是笛卡尔积，直接返回两个数据源行数相乘的结果
      return rowsA * rowsB;
    } else if (join instanceof MarkJoin
        || join instanceof InnerJoin
        || join instanceof SingleJoin) {
      Filter filter = null;
      if (join instanceof MarkJoin) {
        filter = ((MarkJoin) join).getFilter();
      } else if (join instanceof InnerJoin) {
        filter = ((InnerJoin) join).getFilter();
      } else {
        filter = ((SingleJoin) join).getFilter();
      }
      Double selectivity = selectivityHandler.getSelectivity(join, filter);
      if (selectivity == null) {
        return rowsA * rowsB;
      }
      return rowsA * rowsB * selectivity;
    } else if (join instanceof OuterJoin) {
      // 假设OuterJoin包含左外、右外、全外等情况，先获取连接条件的选择性
      Filter filter = ((OuterJoin) join).getFilter();
      Double selectivity = selectivityHandler.getSelectivity(join, filter);
      if (selectivity == null) {
        return null;
      }
      if (((OuterJoin) join).getOuterJoinType() == OuterJoinType.LEFT) {
        return rowsA * (1D - selectivity) + rowsA * rowsB * selectivity;
      } else if (((OuterJoin) join).getOuterJoinType() == OuterJoinType.RIGHT) {
        return rowsB * (1D - selectivity) + rowsA * rowsB * selectivity;
      } else if (((OuterJoin) join).getOuterJoinType() == OuterJoinType.FULL) {
        return (rowsA + rowsB) * (1D - selectivity) + rowsA * rowsB * selectivity;
      }
    } else {
      throw new IllegalArgumentException("Unexpected join type: " + join.getClass());
    }
    return null;
  }

  public @Nullable Double getRowCount(Filter filter) {
    return null;
  }

  public @Nullable Double getRowCount(Project project) {
    List<String> patterns = project.getPatterns();
    for (String pattern : patterns) {
      // 根据pattern获取数据源的行数

    }
    return null;
  }
}
