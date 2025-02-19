package cn.edu.tsinghua.iginx.statistics.util;

import cn.edu.tsinghua.iginx.engine.shared.data.Value;
import cn.edu.tsinghua.iginx.engine.shared.expr.BaseExpression;
import cn.edu.tsinghua.iginx.engine.shared.expr.BinaryExpression;
import cn.edu.tsinghua.iginx.engine.shared.expr.ConstantExpression;
import cn.edu.tsinghua.iginx.engine.shared.expr.Expression;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.*;
import cn.edu.tsinghua.iginx.sql.utils.ExpressionUtils;
import cn.edu.tsinghua.iginx.statistics.data.Point;
import cn.edu.tsinghua.iginx.thrift.DataType;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PointUtil {
    public static List<Point> getFullRange(DataType dataType) {
        Value lower = ValueUtil.minValue(dataType);
        return Collections.singletonList(new Point(lower, true));
    }

    public static List<Point> get2RangeFull(DataType dataType) {
        Point starPoint1 = new Point(ValueUtil.minValue(dataType), true);
        Point endPoint1 = new Point(true);
        endPoint1.setValue(new Value(DataType.INTEGER, 0));
        Point starPoint2 = new Point(true, true);
        starPoint2.setValue(new Value(DataType.INTEGER, 0));
        Point endPoint2 = new Point(ValueUtil.maxValue(dataType), false);
        return Arrays.asList(starPoint1, endPoint1, starPoint2, endPoint2);
    }

    private static List<Point> buildFromIsTrue(BoolFilter boolFilter, int isNot) {
        // TRUE range is {[-inf 0) (0 +inf]}
        Point starPoint1 = new Point(ValueUtil.minValue(DataType.INTEGER), true);
        Point endPoint1 = new Point(true);
        endPoint1.setValue(new Value(DataType.INTEGER, 0));
        Point starPoint2 = new Point(true, true);
        starPoint2.setValue(new Value(DataType.INTEGER, 0));
        Point endPoint2 = new Point(ValueUtil.maxValue(DataType.INTEGER));
        return Arrays.asList(starPoint1, endPoint1, starPoint2, endPoint2);
    }

    private static List<Point> buildFromIn(InFilter inFilter, DataType dataType) {
        Set<Value> values = inFilter.getValues();
        List<Value> valuesList = new ArrayList<>(values);
        ValueUtil.sort(dataType, valuesList);
        List<Point> rangePoints = new ArrayList<>();
        // TODO:LHZ这里默认是in (1,2,3)这样的语句，其余not情况再补充
        for (Value value : values) {
            Point startPoint = new Point(value, true);
            Point endPoint = new Point(value, false);
            rangePoints.add(startPoint);
            rangePoints.add(endPoint);
        }
        return rangePoints;
    }

    private static List<Point> buildFormBinOp(ExprFilter expression, DataType dataType) {
        Op op = null;
        Value value = null;
        if (ExpressionUtils.isConstantArithmeticExpr(expression.getExpressionA())) {
            Expression expressionA = expression.getExpressionA();
            // TODO: 后续支持表达式计算，这里仅处理constant类型
            if (expressionA.getType()==Expression.ExpressionType.Constant) {
                Object valueA = ((ConstantExpression) expressionA).getValue();
                value = new Value(dataType, valueA);
                switch (expression.getOp()) {
                    case L:
                        op = Op.G;
                        break;
                    case G:
                        op = Op.L;
                        break;
                    case LE:
                        op = Op.GE;
                        break;
                    case GE:
                        op = Op.LE;
                        break;
                    default:
                        op = expression.getOp();
                }
            } else {
                Expression expressionB = expression.getExpressionB();
                // TODO: 后续支持表达式计算，这里仅处理constant类型
                if (expressionB.getType()==Expression.ExpressionType.Constant) {
                    Object valueB = ((ConstantExpression) expressionB).getValue();
                    value = new Value(dataType, valueB);
                    op = expression.getOp();
                }
            }
        }
        if (value==null) {
            return null;
        }

        switch (op) {
            case L:
                return Arrays.asList(new Point(ValueUtil.minValue(dataType), true),
                    new Point(value, false, true));
            case G:
                return Arrays.asList(new Point(value, true, true),
                    new Point(ValueUtil.maxValue(dataType)));
            case LE:
                return Arrays.asList(new Point(ValueUtil.minValue(dataType), true),
                    new Point(value));
            case GE:
                return Arrays.asList(new Point(value, true),
                    new Point(ValueUtil.maxValue(dataType)));
            case NE:
                return Arrays.asList(new Point(ValueUtil.minValue(dataType), true),
                    new Point(value,true),
                    new Point(value, false, true),
                    new Point(ValueUtil.maxValue(dataType)));
            case E:
                return Arrays.asList(new Point(value, true), new Point(value));
        }
        return null;
    }

    public static List<Point> intersection(List<Point> points1, List<Point> points2, DataType dataType) {
        return merge(points1, points2, false, dataType);
    }

    public static List<Point> union(List<Point> points1, List<Point> points2, DataType dataType) {
        return merge(points1, points2, true, dataType);
    }

    public static List<Point> merge(List<Point> points1, List<Point> points2, boolean isUnion, DataType dataType) {
        List<Point> points = new ArrayList<>();
        points.addAll(points1);
        points.addAll(points2);
        sortPoints(dataType, points);

        int     inRangeCount=0;
        int requiredInRangeCount=0;
        if (isUnion) {
            requiredInRangeCount = 1;
        } else {
            requiredInRangeCount = 2;
        }
        List<Point> mergedPoints = new ArrayList<>();
        //注意这里仅仅对两个区间进行操作，因为最终只传出一个区间，而又传进来一个区间，所以只对两个区间进行操作
        for (Point point : points) {
            if (point.isStart()) {
                inRangeCount++;
                if (inRangeCount == requiredInRangeCount) {
                    mergedPoints.add(point);
                }
            } else {
                if (inRangeCount == requiredInRangeCount) {
                    mergedPoints.add(point);
                }
                inRangeCount--;
            }
        }
        return mergedPoints;
    }

    public static List<Point> build(Filter filter, DataType dataType) {
        List<Point> result = new ArrayList<>();
        switch (filter.getType()) {
            case Bool:
                return buildFromIsTrue((BoolFilter) filter, ((BoolFilter) filter).isTrue()?0:1);
            case Or:
                OrFilter orFilter = (OrFilter) filter;
                for (Filter childFilter : orFilter.getChildren()) {
                    union(result, build(childFilter, dataType), dataType);
                }
                return result;
            case And:
                AndFilter andFilter = (AndFilter) filter;
                for (Filter childFilter : andFilter.getChildren()) {
                    intersection(result, build(childFilter, dataType), dataType);
                }
                return result;
            case Not:
                NotFilter notFilter = (NotFilter) filter;
                return build(notFilter.getChild(), dataType);
            case Key:
                // TODO: not sure?
                return null;
            case Path:
                return null;
            case In:
                return buildFromIn((InFilter) filter, dataType);
            case Value:
                ExprFilter valueFilter = FilterUtil.switch2ExprFilter((ValueFilter) filter);
                return buildFormBinOp(valueFilter, dataType);
            case Expr:
                ExprFilter exprFilter = (ExprFilter) filter;
                return buildFormBinOp(exprFilter, dataType);
        }
        return null;
    }

    public static List<Point> sortPoints(DataType dataType, List<Point> points) {
        points.sort((p1, p2) -> {
            Value o1 = p1.getValue();
            Value o2 = p2.getValue();
            switch (dataType) {
                case INTEGER:
                    return o1.getIntV().compareTo(o2.getIntV());
                case LONG:
                    return o1.getLongV().compareTo(o2.getLongV());
                case FLOAT:
                    return o1.getFloatV().compareTo(o2.getFloatV());
                case DOUBLE:
                    return o1.getDoubleV().compareTo(o2.getDoubleV());
                case BINARY:
                    return new String(o1.getBinaryV()).compareTo(new String(o2.getBinaryV()));
                default:
                    throw new IllegalArgumentException("unknown data type: " + dataType);
            }
        });
        return points;
    }

}








