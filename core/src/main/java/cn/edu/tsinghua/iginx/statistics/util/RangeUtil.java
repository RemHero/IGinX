package cn.edu.tsinghua.iginx.statistics.util;

import cn.edu.tsinghua.iginx.engine.shared.data.Value;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.statistics.data.Bounds;
import cn.edu.tsinghua.iginx.statistics.data.Point;
import cn.edu.tsinghua.iginx.statistics.data.Range;
import cn.edu.tsinghua.iginx.thrift.DataType;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RangeUtil {

    public static List<Range> buildColumnRange(List<Filter> filters, DataType dataType) {
        if (filters.size() == 0) {
            return Arrays.asList(new Range(ValueUtil.minValue(dataType), ValueUtil.maxValue(dataType)));
        }
        List<Point> fullRange = PointUtil.getFullRange(dataType);
        for (Filter filter : filters) {
            fullRange = PointUtil.intersection(fullRange, PointUtil.build(filter, dataType), dataType);
        }
        List<Range> ranges = RangeUtil.points2TableRanges(fullRange, dataType);
        return ranges;
    }

    private static boolean validInterval(Point high, Point low) {
        if (ValueUtil.compareTo(high.getValue(), low.getValue())<0) {
            return false;
        }
        return true;
    }

    public static List<Range> points2TableRanges(List<Point> points, DataType dataType) {
        List<Range> ranges = new ArrayList<>();
        Value minValue=ValueUtil.minValue(dataType), maxValue=ValueUtil.maxValue(dataType);
        for (int i=0;i<points.size(); i+=2) {
            Point startPoint = convertPoint(points.get(i), dataType);
            if (startPoint.getValue().isNull()) {
                startPoint.setValue(minValue);
                startPoint.setExcl(false);
            }
            Point endPoint = points.get(i+1);
            if (endPoint.getValue().isNull()) {
                continue;
            }
            boolean isValid = validInterval(endPoint, startPoint);
            if (!isValid) {
                continue;
            }
            Range range = new Range(startPoint.getValue(), startPoint.isExcl(), endPoint.getValue(), endPoint.isExcl());
            ranges.add(range);
        }
        return ranges;
    }


    private static Point convertPoint(Point point, DataType dataType) {
        Value value = point.getValue();
        switch (value.getDataType()) {
            case INTEGER:
                if (value.getIntV()==Integer.MIN_VALUE) {
                    return point;
                }
            case LONG:
                if (value.getLongV()==Long.MIN_VALUE) {
                    return point;
                }
            case FLOAT:
                if (value.getFloatV()==Float.MIN_VALUE) {
                    return point;
                }
            case DOUBLE:
                if (value.getDoubleV()==Double.MIN_VALUE) {
                    return point;
                }
            case BINARY:
                if (value.getBinaryV().length==0) {
                    return point;
                }
        }
        //TODO:LHZ后续补充
        return point;
    }

    public static Pair<Range, Boolean> intersect(Range rangeA, Range rangeB) {
        Value lowA = rangeA.getLowVal();
        Value highA = rangeA.getHighVal();
        Value lowB = rangeB.getLowVal();
        Value highB = rangeB.getHighVal();

        if (ValueUtil.compareTo(highA, lowB) < 0 || ValueUtil.compareTo(highB, lowA) < 0) {
            return new Pair<>(null, false);
        }
        if (ValueUtil.compareTo(highA, highB) < 0) {
            return new Pair<>(new Range(lowB,highA), true);
        } else {
            return new Pair<>(new Range(lowA,highB), true);
        }
    }
}
