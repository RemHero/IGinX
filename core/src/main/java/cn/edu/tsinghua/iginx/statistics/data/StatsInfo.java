package cn.edu.tsinghua.iginx.statistics.data;

import java.util.*;

public class StatsInfo {
  private Long count = null;
  private Map<String, Double> colNDVs = new HashMap<>();

  private TableStatistic histColl;
  private List<GroupNDV> groupNDVs;
  // column -> cardinality
  private Map<String, Double> cardinality;

  public StatsInfo(long rowCount) {
    this.count = rowCount;
  }

  public StatsInfo(long rowCount, Map<String, Double> colNDVs) {
    this(rowCount, colNDVs, null, null);
  }

  public StatsInfo(long rowCount, TableStatistic histColl) {
    this(rowCount, null, histColl, null);
  }

  public StatsInfo(long rowCount, Map<String, Double> colNDVs, List<GroupNDV> groupNDVs) {
    this(rowCount, colNDVs, null, groupNDVs);
  }

  public StatsInfo(
      long rowCount, Map<String, Double> colNDVs, TableStatistic histColl, List<GroupNDV> groupNDVs) {
    this.count = rowCount;
    if (colNDVs != null) {
      this.colNDVs.putAll(colNDVs);
    }
    this.histColl = histColl;
    this.groupNDVs = groupNDVs;
  }

  @Override
  public String toString() {
    return "count " + count + ", ColNDVs " + colNDVs;
  }

  public Long getCount() {
    return count;
  }

  public Double getColNDV(String col) {
    return colNDVs.get(col);
  }

  public Map<String, Double> getColNDVMap() {
    return colNDVs;
  }

  public List<GroupNDV> getGroupNDVs() {
    return groupNDVs;
  }

  public TableStatistic getHistColl() {
    return histColl;
  }

  public void setGroupNDVs(List<GroupNDV> groupNDVs) {
    this.groupNDVs = groupNDVs;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public void setHistColl(TableStatistic histColl) {
    this.histColl = histColl;
  }

  public void addCardinality(String col, Double value) {
    if (cardinality == null) {
      cardinality = new HashMap<>();
    }
    if (cardinality.containsKey(col) && cardinality.get(col) < value) {
      cardinality.put(col, value);
    } else if (!cardinality.containsKey(col)) {
      cardinality.put(col, value);
    }
  }

  public double getCardinality(String col) {
    return cardinality.getOrDefault(col, 0.0);
  }

  public StatsInfo scale(double factor) {
    Map<String, Double> scaledColNDVs = new HashMap<>();
    for (Map.Entry<String, Double> entry : colNDVs.entrySet()) {
      scaledColNDVs.put(entry.getKey(), entry.getValue() * factor);
    }

    // 这里maybe有问题
    List<GroupNDV> scaledGroupNDVs = new ArrayList<>();
    for (GroupNDV group : groupNDVs) {
      scaledGroupNDVs.add(new GroupNDV(group.getCols(), group.getNdv() * factor));
    }

    return new StatsInfo((long) (count * factor), scaledColNDVs, histColl, scaledGroupNDVs);
  }

  public Void setColNDVs(Map<String, Double> colNDVs) {
    this.colNDVs = colNDVs;
    return null;
  }

  public StatsInfo scaleByExpectCnt(double expectCnt) {
    if (expectCnt >= count) {
      return this;
    }
    if (count > 1.0) { // Prevent potential overflow with very small row counts
      return scale(expectCnt / count);
    }
    return this;
  }

  public GroupNDV getGroupNDV4Cols(List<String> cols) {
    if (cols == null || cols.isEmpty() || groupNDVs == null || groupNDVs.isEmpty()) {
      return null;
    }

    Collections.sort(cols);

    for (GroupNDV groupNDV : groupNDVs) {
      if (cols.size() != groupNDV.getCols().size()) {
        continue;
      }
      boolean match = true;
      for (int i = 0; i < cols.size(); i++) {
        if (!Objects.equals(cols.get(i), groupNDV.getCols().get(i))) {
          match = false;
          break;
        }
      }
      if (match) {
        return groupNDV;
      }
    }
    return null;
  }
}
