package cn.edu.tsinghua.iginx.statistics.handler;

import cn.edu.tsinghua.iginx.engine.shared.operator.*;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import javax.annotation.Nullable;

public class Selectivity {
  //    public @Nullable Double getSelectivity(AbstractJoin join, Filter filter) {
  //        Filter joinFilter = null;
  //        if (join instanceof CrossJoin) {
  //            joinFilter = ((CrossJoin)join).getFilter();
  //        } else if (join instanceof MarkJoin || join instanceof InnerJoin || join instanceof
  // SingleJoin) {
  //        } else if (join instanceof OuterJoin) {
  //            // 假设OuterJoin包含左外、右外、全外等情况，先获取连接条件的选择性
  //            Filter filter = ((OuterJoin)join).getFilter();
  //            Double selectivity = selectivityHandler.getSelectivity(join, filter);
  //            if (selectivity == null) {
  //                return null;
  //            }
  //            if (((OuterJoin) join).getOuterJoinType() == OuterJoinType.LEFT) {
  //                return rowsA * (1D - selectivity) + rowsA * rowsB * selectivity;
  //            } else if (((OuterJoin) join).getOuterJoinType() == OuterJoinType.RIGHT) {
  //                return rowsB * (1D - selectivity) + rowsA * rowsB * selectivity;
  //            } else if (((OuterJoin) join).getOuterJoinType() == OuterJoinType.FULL) {
  //                return (rowsA + rowsB) * (1D - selectivity) + rowsA * rowsB * selectivity;
  //            }
  //        } else {
  //            throw new IllegalArgumentException("Unexpected join type: " + join.getClass());
  //        }
  //
  //        if (filter != null) {
  //
  //        }
  //        return null;
  //    }

  public @Nullable Double getSelectivity(Project join, Filter filter) {

    return null;
  }

  // Catch-all rule when none of the others apply.
  public @Nullable Double getSelectivity(Operator op, Filter filter) {
    return null;
  }
}
