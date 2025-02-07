package cn.edu.tsinghua.iginx.statistics.data;

import cn.edu.tsinghua.iginx.engine.physical.memory.execute.utils.FilterUtils;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.utils.Pair;

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
    for(String col : columns.keySet()) {
      if (extractedCols.contains(col)) {
        // This column should have histogram.
        Pair<Integer, List<Bounds>> result = getMaskAndRanges(filter, col);

      }
    }


  }

    private Pair<Integer, List<Bounds>> getMaskAndRanges(Filter filter, String col) {
        List<Filter> filters = filter.getFilters();
        int mask = 0;
        List<Range> ranges = new ArrayList<>();
        for (Filter f : filters) {
        if (f instanceof BinaryFilter) {
            BinaryFilter bf = (BinaryFilter) f;
            if (bf.getCol().equals(col)) {
            mask |= 1;
            ranges.add(new Range(bf.getOp(), bf.getValue()));
            }
        }
        }；
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
}
