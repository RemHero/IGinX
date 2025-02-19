package cn.edu.tsinghua.iginx.statistics.data;

import cn.edu.tsinghua.iginx.engine.shared.data.Value;

public class Range {
    private Value LowVal;
    private Value HighVal;

    boolean LowExclude; // Low value is exclusive.
    boolean HighExclude; // High value is exclusive.

    public Range(Value lowVal, boolean lowExclude, Value highVal, boolean highExclude) {
        LowVal = lowVal;
        HighVal = highVal;
        LowExclude = lowExclude;
        HighExclude = highExclude;
    }

    public Range(Value lowVal, Value highVal) {
        LowVal = lowVal;
        HighVal = highVal;
    }

    public Value getLowVal() {
        return LowVal;
    }

    public Value getHighVal() {
        return HighVal;
    }

    public boolean isLowExclude() {
        return LowExclude;
    }

    public boolean isHighExclude() {
        return HighExclude;
    }
}
