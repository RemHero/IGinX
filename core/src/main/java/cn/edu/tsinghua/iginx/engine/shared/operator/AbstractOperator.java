/*
 * IGinX - the polystore system with high performance
 * Copyright (C) Tsinghua University
 * TSIGinX@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package cn.edu.tsinghua.iginx.engine.shared.operator;

import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.engine.shared.operator.type.OperatorType;
import cn.edu.tsinghua.iginx.statistics.data.StatsInfo;

public abstract class AbstractOperator implements Operator {

  private final OperatorType type;

  private Double rowCount;

  private Double selectivity;

  private StatsInfo statsInfo;

  public AbstractOperator() {
    this.type = OperatorType.Unknown;
  }

  public AbstractOperator(OperatorType type) {
    if (type == null) {
      throw new IllegalArgumentException("operator type shouldn't be null");
    }
    this.type = type;
  }

  public StatsInfo getStatsInfo() {
    return statsInfo;
  }

  public void setStatsInfo(StatsInfo statsInfo) {
    this.statsInfo = statsInfo;
  }

  @Override
  public OperatorType getType() {
    return type;
  }

  @Override
  public Double getRowCount() {
    return rowCount;
  }

  public void setRowCount(Double rowCount) {
    this.rowCount = rowCount;
  }

  @Override
  public Double getSelectivity(Filter filter) {
    return selectivity;
  }

  public void setSelectivity(Filter filter, Double selectivity) {}
}
