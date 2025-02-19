package cn.edu.tsinghua.iginx.statistics.util;

import cn.edu.tsinghua.iginx.engine.shared.data.Value;
import cn.edu.tsinghua.iginx.statistics.data.Bounds;
import cn.edu.tsinghua.iginx.thrift.DataType;
import javafx.util.Pair;

import java.util.List;

public class BoundsUntil {
    public static Bounds makeBounds(Value lower, Value upper) {
        DataType dataType = lower.getDataType();
        switch (dataType) {
            case INTEGER:
                return Bounds.intBounds(lower.getIntV(), upper.getIntV());
            case LONG:
                return Bounds.longBounds(lower.getLongV(), upper.getLongV());
            case FLOAT:
                return Bounds.floatBounds(lower.getFloatV(), upper.getFloatV());
            case DOUBLE:
                return Bounds.doubleBounds(lower.getDoubleV(), upper.getDoubleV());
            case BINARY:
                return Bounds.stringBounds(new String(lower.getBinaryV()), new String(upper.getBinaryV()));
            default:
                throw new IllegalArgumentException("unknown data type: " + dataType);
        }
    }

    public static Pair<Integer, Boolean> LowerBound(List<Bounds> bounds, Value value) {
        boolean match = false;
        int idx = 0;
        for (int i = 0; i < bounds.size(); i++) {
            int compare = ValueUtil.compareTo(bounds.get(i).getLowerValue(), value);
            if (compare == 0) {
                match = true;
                break;
            }
            if (compare >= 0) {
                idx = i;
                break;
            }
        }
        if (match) {
            return new Pair<>(idx, true);
        } else {
            return new Pair<>(bounds.size()-1, false);
        }
    }
}
