package cn.edu.tsinghua.iginx.engine.shared.operator;

import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import javax.annotation.Nullable;

public interface Measurable {
  @Nullable
  default Double getRowCount() {
    return null;
  }

  @Nullable
  default Double getSelectivity(Filter filter) {
    return null;
  }
}
