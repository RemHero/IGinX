package cn.edu.tsinghua.iginx.statistics.data;

import java.util.List;

public class GroupNDV {
  private List<String> cols;
  private double ndv;

  public GroupNDV(List<String> cols, double ndv) {
    this.cols = cols;
    this.ndv = ndv;
  }

  public List<String> getCols() {
    return cols;
  }

  public double getNdv() {
    return ndv;
  }

  @Override
  public String toString() {
    return "GroupNDV{" + "cols=" + cols + ", ndv=" + ndv + '}';
  }
}
