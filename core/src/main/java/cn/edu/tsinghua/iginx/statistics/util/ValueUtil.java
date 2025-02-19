package cn.edu.tsinghua.iginx.statistics.util;

import cn.edu.tsinghua.iginx.engine.shared.data.Value;
import cn.edu.tsinghua.iginx.thrift.DataType;

import java.util.List;

public class ValueUtil {
    public static Value maxValue(DataType dataType) {
        switch (dataType) {
            case INTEGER:
                return new Value(Integer.MAX_VALUE);
            case LONG:
                return new Value(Long.MAX_VALUE);
            case FLOAT:
                return new Value(Float.MAX_VALUE);
            case DOUBLE:
                return new Value(Double.MAX_VALUE);
            case BINARY:
                return new Value(new byte[0]);
            default:
                throw new IllegalArgumentException("unknown data type: " + dataType);
        }
    }

    public static Value minValue(DataType dataType) {
        switch (dataType) {
            case INTEGER:
                return new Value(Integer.MIN_VALUE);
            case LONG:
                return new Value(Long.MIN_VALUE);
            case FLOAT:
                return new Value(Float.MIN_VALUE);
            case DOUBLE:
                return new Value(Double.MIN_VALUE);
            case BINARY:
                return new Value(new byte[0]);
            default:
                throw new IllegalArgumentException("unknown data type: " + dataType);
        }
    }

    public static List<Value> sort(DataType dataType, List<Value> values) {
        values.sort((o1, o2) -> {
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
        return values;
    }

    public static int compareTo(Value value1, Value value2) {
        switch (value1.getDataType()) {
            case INTEGER:
                return value1.getIntV().compareTo(value2.getIntV());
            case LONG:
                return value1.getLongV().compareTo(value2.getLongV());
            case FLOAT:
                return value1.getFloatV().compareTo(value2.getFloatV());
            case DOUBLE:
                return value1.getDoubleV().compareTo(value2.getDoubleV());
            case BINARY:
                return new String(value1.getBinaryV()).compareTo(new String(value2.getBinaryV()));
            default:
                throw new IllegalArgumentException("unknown data type: " + value1.getDataType());
        }
    }

    public static double division(Value value1, Value value2) {
        switch (value1.getDataType()) {
            case INTEGER:
                return value1.getIntV() / value2.getIntV();
            case LONG:
                return value1.getLongV() / value2.getLongV();
            case FLOAT:
                return value1.getFloatV() / value2.getFloatV();
            case DOUBLE:
                return value1.getDoubleV() / value2.getDoubleV();
            default:
                throw new IllegalArgumentException("unknown data type: " + value1.getDataType());
        }
    }

    public static double minus(Value value1, Value value2) {
        switch (value1.getDataType()) {
            case INTEGER:
                return value1.getIntV() - value2.getIntV();
            case LONG:
                return value1.getLongV() - value2.getLongV();
            case FLOAT:
                return value1.getFloatV() - value2.getFloatV();
            case DOUBLE:
                return value1.getDoubleV() - value2.getDoubleV();
            default:
                throw new IllegalArgumentException("unknown data type: " + value1.getDataType());
        }
    }
}
