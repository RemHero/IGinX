package cn.edu.tsinghua.iginx.statistics.util;

import cn.edu.tsinghua.iginx.engine.shared.expr.BaseExpression;
import cn.edu.tsinghua.iginx.engine.shared.expr.ConstantExpression;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.ExprFilter;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Op;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.ValueFilter;

public class FilterUtil {
    public static ExprFilter switch2ExprFilter(ValueFilter valueFilter) {
        Op op = valueFilter.getOp();
        BaseExpression baseExpression = new BaseExpression(valueFilter.getPath());
        ConstantExpression constantExpression = new ConstantExpression(valueFilter.getValue());
        return new ExprFilter(baseExpression, op, constantExpression);
    }
}
