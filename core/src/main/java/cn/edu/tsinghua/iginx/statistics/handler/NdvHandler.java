package cn.edu.tsinghua.iginx.statistics.handler;

import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.statistics.data.GroupNDV;
import cn.edu.tsinghua.iginx.statistics.data.StatsInfo;
import java.util.List;

public class NdvHandler {

  static Double EstimateColsNDVWithMatchedLen(List<String> cols, StatsInfo profile) {
    double ndv = 1.0;
    GroupNDV groupNDV = profile.getGroupNDV4Cols(cols);
    if (groupNDV != null) {
      return Math.max(groupNDV.getNdv(), ndv);
    }

    for (String col : cols) {
      Double colNDV = profile.getColNDV(col);
      if (colNDV != null) {
        ndv = Math.max(ndv, colNDV);
      }
    }
    for (String col : cols) {
      Double colNDV = profile.getCardinality(col);
      ndv = Math.max(ndv, colNDV);
    }
    return ndv;
  }
}
