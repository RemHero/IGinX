package cn.edu.tsinghua.iginx.statistics.data;

import cn.edu.tsinghua.iginx.engine.shared.data.Value;

public class Point {
    private Value value;
    private boolean excl=false;
    private boolean start=false;

    public Point(Value value, boolean start, boolean excl) {
        this.value = value;
        this.start = start;
        this.excl = excl;
    }

    public Point(Value value, boolean start) {
        this.value = value;
        this.start = start;
    }

    public Point(boolean excl) {
        this.excl = excl;
    }

    public Point(boolean excl, boolean start) {
        this.excl = excl;
        this.start = start;
    }

    public Point(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public boolean isExcl() {
        return excl;
    }

    public void setExcl(boolean excl) {
        this.excl = excl;
    }

    public boolean isStart() {
        return start;
    }
}
