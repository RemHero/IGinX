package cn.edu.tsinghua.iginx.filesystem.tools;

import cn.edu.tsinghua.iginx.engine.shared.operator.filter.*;
import cn.edu.tsinghua.iginx.utils.JsonUtils;
import com.google.common.collect.BiMap;

public class FilterTransformer {
  private static int index = 0;
  private static int deep = 0;
  private static String prefix = "A";

  public static String toString(Filter filter) {
    if (filter == null) {
      return null;
    }
    return new String(JsonUtils.toJson(filter));
  }

  public static Filter toFilter(String filter) {
    return JsonUtils.fromJson(filter.getBytes(), Filter.class);
  }

  public static String toString(Filter filter, BiMap<String, String> vals) {
    if (filter == null) {
      return "";
    }
    deep++;
    switch (filter.getType()) {
      case And:
        return toString((AndFilter) filter, vals);
      case Or:
        return toString((OrFilter) filter, vals);
      case Not:
        return toString((NotFilter) filter, vals);
      case Value:
        return toString((ValueFilter) filter, vals);
      case Key:
        return toString((KeyFilter) filter, vals);
      default:
        return "";
    }
  }

  private static String toString(AndFilter filter, BiMap<String, String> vals) {
    String res = "(";
    for (Filter f : filter.getChildren()) {
      res += toString(f, vals);
      res += "&";
    }
    if (res.length() != 1) res = res.substring(0, res.length() - 1);
    res += ")";
    refreshIndex();
    return res;
  }

  private static String toString(NotFilter filter, BiMap<String, String> vals) {
    refreshIndex();
    return "!" + toString(filter.getChild(), vals);
  }

  private static String toString(KeyFilter filter, BiMap<String, String> vals) {
    String val = "key" + " " + Op.op2Str(filter.getOp()) + " " + filter.getValue();
    if (!vals.containsValue(val)) vals.put(prefix + (index++), val);
    refreshIndex();
    return vals.inverse().get(val);
  }

  private static String toString(ValueFilter filter, BiMap<String, String> vals) {
    String val;
    if (filter.getOp().equals(Op.LIKE)) {
      val = filter.getPath() + " like " + filter.getValue().getBinaryVAsString();
    } else {
      val = filter.getPath() + " " + Op.op2Str(filter.getOp()) + " " + filter.getValue().getValue();
    }
    if (!vals.containsValue(val)) vals.put(prefix + (index++), val);
    refreshIndex();
    return vals.inverse().get(val);
  }

  private static String toString(OrFilter filter, BiMap<String, String> vals) {
    String res = "(";
    for (Filter f : filter.getChildren()) {
      res += toString(f, vals);
      res += "|";
    }
    if (res.length() != 1) res = res.substring(0, res.length() - 1);
    res += ")";
    refreshIndex();
    return res;
  }

  private static final void refreshIndex() {
    deep--;
    if (deep == 0) {
      index = 0;
    }
  }
}
