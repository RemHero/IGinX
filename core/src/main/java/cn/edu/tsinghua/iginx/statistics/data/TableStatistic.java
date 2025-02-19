package cn.edu.tsinghua.iginx.statistics.data;

import cn.edu.tsinghua.iginx.engine.physical.memory.execute.utils.FilterUtils;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.statistics.handler.StatsHandler;
import cn.edu.tsinghua.iginx.statistics.util.BoundsUntil;
import cn.edu.tsinghua.iginx.statistics.util.PointUtil;
import cn.edu.tsinghua.iginx.statistics.util.RangeUtil;
import cn.edu.tsinghua.iginx.statistics.util.ValueUtil;
import cn.edu.tsinghua.iginx.thrift.DataType;
import cn.edu.tsinghua.iginx.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TableStatistic {
  private long tableId;
  private long rowCount;
  private Map<String, ColumnStatistic> columns;

  public TableStatistic(Map<String, ColumnStatistic> columns) {
    this.columns = columns;
  }

  public TableStatistic(long rowCount, Map<String, ColumnStatistic> columns) {
    this.rowCount = rowCount;
    this.columns = columns;
  }

  // Selectivity is a function calculate the selectivity of the expressions.
  // The definition of selectivity is (row count after filter / row count before filter).
  // And exprs must be CNF now, in other words, `exprs[0] and exprs[1] and ... and exprs[len - 1]` should be held when you call this.
  // Currently the time complexity is o(n^2).
  public double selectivity(Filter filter, List<String> accessPath) {
    if (rowCount == 0 || filter==null) {
      return 1.0;
    }
    List<String> extractedCols = FilterUtils.getAllPathsFromFilter(filter);
    List<ExprSet> sets = new ArrayList<>();
    List<Filter> filters = FilterUtils.splitAndFilter(filter);
    for(String col : columns.keySet()) {
      if (extractedCols.contains(col)) {
        // This column should have histogram.
        Pair<Integer, List<Range>> result = getMaskAndRanges(filter, col);
        int mask = result.getK();
        List<Range> ranges = result.getV();
        sets.add(new ExprSet(mask, col, ranges));
      }
    }
    //TODO:LHZ getUsableSetsByGreedy后续再看
    double ret = 1.0;
    // Initialize the mask with the full set.
    int mask = (1 << filters.size()) - 1;
    for (ExprSet set : sets) {
      mask ^= set.mask;
      double rowCount = getRowCountByColumnRanges(set.col, set.ranges);
      ret *= rowCount / this.rowCount;
    }
    if (mask>0) {
      ret*= StatsHandler.SELECTIONFACTOR;
    }
    return ret;
  }

  // getIncreaseFactor will return a factor of data increasing after the last analysis.
  double getIncreaseFactor(ColumnStatistic column,int totalCount) {
    int columnCount = (int) column.getHistogram().totalRowCount();
    if (columnCount == 0) {
      // avoid dividing by 0
      return 1.0;
    }
    return totalCount / columnCount;
  }

  public double getRowCountByColumnRanges(String col, List<Range> ranges) {
    // TODO:LHZ isValidColumn后续再看
    ColumnStatistic column = columns.get(col);
    double result = column.getColumnRowCount(ranges);
    result *= getIncreaseFactor(column, (int) rowCount);
    return result;
  }

  private Pair<Integer, List<Range>> getMaskAndRanges(Filter filter, String col) {
      List<Filter> filters = FilterUtils.splitAndFilter(filter);
      int mask = 0;
      List<Filter> fr = new ArrayList<>();
      for (Filter f : filters) {
        // extractAccessConditionsForColumn
        if (FilterUtils.checkForStat(f, col)) {
          fr.add(f);
        }
      }
      List<Range> ranges= RangeUtil.buildColumnRange(fr, columns.get(col).getDataType());
      for (int i = 0; i < filters.size(); i++) {
        for (int j = 0; j < fr.size(); j++) {
          if (FilterUtils.equal(filters.get(i),fr.get(j))) {
            mask |= 1 << i;
            break;
          }
        }
      }
      return new Pair<>(mask, ranges);
  }




  public long getTableId() {
    return tableId;
  }

  public Long getRowCount() {
    return rowCount;
  }

  public Map<String, ColumnStatistic> getColumns() {
    return columns;
  }

  public void setRowCount(long rowCount) {
    this.rowCount = rowCount;
  }

  public void setColumns(Map<String, ColumnStatistic> columns) {
    this.columns = columns;
  }

  public void setTableId(long tableId) {
    this.tableId = tableId;
  }

  public ColumnStatistic getColumn(String columnName) {
    return columns.get(columnName);
  }

  public void addColumn(String columnName, ColumnStatistic column) {
    columns.put(columnName, column);
  }

  public String toString() {
    return "TableStatistic{" +
            "tableId=" + tableId +
            ", rowCount=" + rowCount +
            ", columns=" + columns +
            '}';
  }

  class ExprSet {
    int mask;
    String col;
    List<Range> ranges;

    public ExprSet(int mask, String col, List<Range> ranges) {
      this.mask = mask;
      this.col = col;
      this.ranges = ranges;
    }
  }
}
