package cn.edu.tsinghua.iginx.statistics.data;

import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistColl {
  private Map<String, ColumnStatistic> histColl = new HashMap<>();
  // indices
  // 实际上如果需要colgroup的话，这里应该是一个索引map，但是目前系统不支持索引，所以这里之后再考虑

  public HistColl(Map<String, ColumnStatistic> histColl) {
    this.histColl = histColl;
  }

  public Map<String, ColumnStatistic> getHistColl() {
    return histColl;
  }


}
