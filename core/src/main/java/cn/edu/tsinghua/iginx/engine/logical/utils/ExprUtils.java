package cn.edu.tsinghua.iginx.engine.logical.utils;

import cn.edu.tsinghua.iginx.engine.shared.KeyRange;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.*;
import cn.edu.tsinghua.iginx.exceptions.SQLParserException;
import cn.edu.tsinghua.iginx.metadata.entity.ColumnsInterval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExprUtils {

  public static Filter toDNF(Filter filter) {
    filter = removeNot(filter);
    filter = removeSingleFilter(filter);
    FilterType type = filter.getType();
    switch (type) {
      case Key:
      case Value:
      case Path:
        return filter;
      case Not:
        throw new SQLParserException("Get DNF failed, filter has not-subFilter.");
      case And:
        return toDNF((AndFilter) filter);
      case Or:
        return toDNF((OrFilter) filter);
      default:
        throw new SQLParserException("Get DNF failed, token type is: " + filter.getType());
    }
  }

  private static Filter toDNF(AndFilter andFilter) {
    List<Filter> children = andFilter.getChildren();
    List<Filter> dnfChildren = new ArrayList<>();
    children.forEach(child -> dnfChildren.add(toDNF(child)));

    boolean childrenWithoutOr = true;
    for (Filter child : dnfChildren) {
      if (child.getType().equals(FilterType.Or)) {
        childrenWithoutOr = false;
        break;
      }
    }

    List<Filter> newChildren = new ArrayList<>();
    if (childrenWithoutOr) {
      dnfChildren.forEach(
          child -> {
            if (FilterType.isLeafFilter(child.getType())) {
              newChildren.add(child);
            } else {
              newChildren.addAll(((AndFilter) child).getChildren());
            }
          });
      return new AndFilter(newChildren);
    } else {
      newChildren.addAll(getConjunctions(dnfChildren));
      return new OrFilter(newChildren);
    }
  }

  private static Filter toDNF(OrFilter orFilter) {
    List<Filter> children = orFilter.getChildren();
    List<Filter> newChildren = new ArrayList<>();
    children.forEach(
        child -> {
          Filter newChild = toDNF(child);
          if (FilterType.isLeafFilter(newChild.getType())
              || newChild.getType().equals(FilterType.And)) {
            newChildren.add(newChild);
          } else {
            newChildren.addAll(((OrFilter) newChild).getChildren());
          }
        });
    return new OrFilter(newChildren);
  }

  private static List<Filter> getConjunctions(List<Filter> filters) {
    List<Filter> cur = getAndChild(filters.get(0));
    for (int i = 1; i < filters.size(); i++) {
      cur = getConjunctions(cur, getAndChild(filters.get(i)));
    }
    return cur;
  }

  private static List<Filter> getConjunctions(List<Filter> first, List<Filter> second) {
    List<Filter> ret = new ArrayList<>();
    for (Filter firstFilter : first) {
      for (Filter secondFilter : second) {
        ret.add(
            mergeToConjunction(
                new ArrayList<>(Arrays.asList(firstFilter.copy(), secondFilter.copy()))));
      }
    }
    return ret;
  }

  private static Filter mergeToConjunction(List<Filter> filters) {
    List<Filter> children = new ArrayList<>();
    filters.forEach(
        child -> {
          if (FilterType.isLeafFilter(child.getType())) {
            children.add(child);
          } else {
            children.addAll(((AndFilter) child).getChildren());
          }
        });
    return new AndFilter(children);
  }

  private static List<Filter> getAndChild(Filter filter) {
    if (filter.getType().equals(FilterType.Or)) {
      return ((OrFilter) filter).getChildren();
    } else {
      return Collections.singletonList(filter);
    }
  }

  public static Filter toCNF(Filter filter) {
    filter = removeNot(filter);
    filter = removeSingleFilter(filter);
    FilterType type = filter.getType();
    switch (type) {
      case Key:
      case Value:
      case Path:
        return filter;
      case Not:
        throw new SQLParserException("Get CNF failed, filter has not-subFilter.");
      case And:
        return toCNF((AndFilter) filter);
      case Or:
        return toCNF((OrFilter) filter);
      default:
        throw new SQLParserException("Get CNF failed, token type is: " + filter.getType());
    }
  }

  private static Filter toCNF(AndFilter andFilter) {
    List<Filter> children = andFilter.getChildren();
    List<Filter> newChildren = new ArrayList<>();
    children.forEach(
        child -> {
          Filter newChild = toDNF(child);
          if (FilterType.isLeafFilter(newChild.getType())
              || newChild.getType().equals(FilterType.Or)) {
            newChildren.add(newChild);
          } else {
            newChildren.addAll(((AndFilter) newChild).getChildren());
          }
        });
    return new AndFilter(newChildren);
  }

  private static Filter toCNF(OrFilter orFilter) {
    List<Filter> children = orFilter.getChildren();
    List<Filter> cnfChildren = new ArrayList<>();
    children.forEach(child -> cnfChildren.add(toCNF(child)));

    boolean childrenWithoutAnd = true;
    for (Filter child : cnfChildren) {
      if (child.getType().equals(FilterType.And)) {
        childrenWithoutAnd = false;
        break;
      }
    }

    List<Filter> newChildren = new ArrayList<>();
    if (childrenWithoutAnd) {
      cnfChildren.forEach(
          child -> {
            if (FilterType.isLeafFilter(child.getType())) {
              newChildren.add(child);
            } else {
              newChildren.addAll(((AndFilter) child).getChildren());
            }
          });
      return new OrFilter(newChildren);
    } else {
      newChildren.addAll(getDisjunctions(cnfChildren));
      return new AndFilter(newChildren);
    }
  }

  private static List<Filter> getDisjunctions(List<Filter> filters) {
    List<Filter> cur = getOrChild(filters.get(0));
    for (int i = 1; i < filters.size(); i++) {
      cur = getDisjunctions(cur, getOrChild(filters.get(i)));
    }
    return cur;
  }

  private static List<Filter> getDisjunctions(List<Filter> first, List<Filter> second) {
    List<Filter> ret = new ArrayList<>();
    for (Filter firstFilter : first) {
      for (Filter secondFilter : second) {
        ret.add(
            mergeToDisjunction(
                new ArrayList<>(Arrays.asList(firstFilter.copy(), secondFilter.copy()))));
      }
    }
    return ret;
  }

  private static Filter mergeToDisjunction(List<Filter> filters) {
    List<Filter> children = new ArrayList<>();
    filters.forEach(
        child -> {
          if (FilterType.isLeafFilter(child.getType())) {
            children.add(child);
          } else {
            children.addAll(((OrFilter) child).getChildren());
          }
        });
    return new OrFilter(children);
  }

  private static List<Filter> getOrChild(Filter filter) {
    if (filter.getType().equals(FilterType.And)) {
      return ((AndFilter) filter).getChildren();
    } else {
      return Collections.singletonList(filter);
    }
  }

  public static Filter removeSingleFilter(Filter filter) {
    if (filter.getType().equals(FilterType.Or)) {
      List<Filter> children = ((OrFilter) filter).getChildren();
      for (int i = 0; i < children.size(); i++) {
        Filter childWithoutSingle = removeSingleFilter(children.get(i));
        children.set(i, childWithoutSingle);
      }
      return children.size() == 1 ? children.get(0) : filter;
    } else if (filter.getType().equals(FilterType.And)) {
      List<Filter> children = ((AndFilter) filter).getChildren();
      for (int i = 0; i < children.size(); i++) {
        Filter childWithoutSingle = removeSingleFilter(children.get(i));
        children.set(i, childWithoutSingle);
      }
      return children.size() == 1 ? children.get(0) : filter;
    } else if (filter.getType().equals(FilterType.Not)) {
      NotFilter notFilter = (NotFilter) filter;
      notFilter.setChild(removeSingleFilter(notFilter.getChild()));
      return filter;
    }
    return filter;
  }

  public static Filter removeNot(Filter filter) {
    FilterType type = filter.getType();
    switch (type) {
      case Key:
      case Value:
      case Path:
        return filter;
      case And:
        return removeNot((AndFilter) filter);
      case Or:
        return removeNot((OrFilter) filter);
      case Not:
        return removeNot((NotFilter) filter);
      default:
        throw new SQLParserException(String.format("Unknown token [%s] in reverse filter.", type));
    }
  }

  private static Filter removeNot(AndFilter andFilter) {
    List<Filter> andChildren = andFilter.getChildren();
    for (int i = 0; i < andChildren.size(); i++) {
      Filter childWithoutNot = removeNot(andChildren.get(i));
      andChildren.set(i, childWithoutNot);
    }
    return andFilter;
  }

  private static Filter removeNot(OrFilter orFilter) {
    List<Filter> orChildren = orFilter.getChildren();
    for (int i = 0; i < orChildren.size(); i++) {
      Filter childWithoutNot = removeNot(orChildren.get(i));
      orChildren.set(i, childWithoutNot);
    }
    return orFilter;
  }

  private static Filter removeNot(NotFilter notFilter) {
    return reverseFilter(notFilter.getChild());
  }

  private static Filter reverseFilter(Filter filter) {
    if (filter == null) return null;

    FilterType type = filter.getType();
    switch (filter.getType()) {
      case Key:
        ((KeyFilter) filter).reverseFunc();
        return filter;
      case Value:
        ((ValueFilter) filter).reverseFunc();
        return filter;
      case Path:
        ((PathFilter) filter).reverseFunc();
        return filter;
      case And:
        List<Filter> andChildren = ((AndFilter) filter).getChildren();
        for (int i = 0; i < andChildren.size(); i++) {
          Filter childWithoutNot = reverseFilter(andChildren.get(i));
          andChildren.set(i, childWithoutNot);
        }
        return new OrFilter(andChildren);
      case Or:
        List<Filter> orChildren = ((OrFilter) filter).getChildren();
        for (int i = 0; i < orChildren.size(); i++) {
          Filter childWithoutNot = reverseFilter(orChildren.get(i));
          orChildren.set(i, childWithoutNot);
        }
        return new AndFilter(orChildren);
      case Not:
        return removeNot(((NotFilter) filter).getChild());
      default:
        throw new SQLParserException(String.format("Unknown token [%s] in reverse filter.", type));
    }
  }

  public static List<KeyRange> getKeyRangesFromFilter(Filter filter) {
    filter = toDNF(filter);
    List<KeyRange> keyRanges = new ArrayList<>();
    extractKeyRange(keyRanges, filter);
    return unionKeyRanges(keyRanges);
  }

  private static void extractKeyRange(List<KeyRange> keyRanges, Filter f) {
    FilterType type = f.getType();
    switch (type) {
      case Value:
      case Path:
        break;
      case Key:
        keyRanges.add(getKeyRangesFromKeyFilter((KeyFilter) f));
        break;
      case And:
        KeyRange range = getKeyRangeFromAndFilter((AndFilter) f);
        if (range != null) keyRanges.add(range);
        break;
      case Or:
        List<KeyRange> ranges = getKeyRangeFromOrFilter((OrFilter) f);
        if (!ranges.isEmpty()) {
          keyRanges.addAll(ranges);
        }
        break;
      default:
        throw new SQLParserException(String.format("Illegal token [%s] in extractKeyRange.", type));
    }
  }

  private static List<KeyRange> getKeyRangeFromOrFilter(OrFilter filter) {
    List<KeyRange> keyRanges = new ArrayList<>();
    filter.getChildren().forEach(f -> extractKeyRange(keyRanges, f));
    return unionKeyRanges(keyRanges);
  }

  private static KeyRange getKeyRangeFromAndFilter(AndFilter filter) {
    List<KeyRange> keyRanges = new ArrayList<>();
    filter.getChildren().forEach(f -> extractKeyRange(keyRanges, f));
    return intersectKeyRanges(keyRanges);
  }

  private static KeyRange getKeyRangesFromKeyFilter(KeyFilter filter) {
    switch (filter.getOp()) {
      case L:
        return new KeyRange(0, filter.getValue());
      case LE:
        return new KeyRange(0, filter.getValue() + 1);
      case G:
        return new KeyRange(filter.getValue() + 1, Long.MAX_VALUE);
      case GE:
        return new KeyRange(filter.getValue(), Long.MAX_VALUE);
      case E:
        return new KeyRange(filter.getValue(), filter.getValue() + 1);
      case NE:
        throw new SQLParserException("Not support [!=] in delete clause.");
      default:
        throw new SQLParserException(
            String.format("Unknown op [%s] in getKeyRangesFromKeyFilter.", filter.getOp()));
    }
  }

  private static List<KeyRange> unionKeyRanges(List<KeyRange> keyRanges) {
    if (keyRanges == null || keyRanges.isEmpty()) return new ArrayList<>();
    keyRanges.sort(
        (tr1, tr2) -> {
          long diff = tr1.getBeginKey() - tr2.getBeginKey();
          return diff == 0 ? 0 : diff > 0 ? 1 : -1;
        });

    List<KeyRange> res = new ArrayList<>();

    KeyRange cur = keyRanges.get(0);
    for (int i = 1; i < keyRanges.size(); i++) {
      KeyRange union = unionTwoKeyRanges(cur, keyRanges.get(i));
      if (union == null) {
        res.add(cur);
        cur = keyRanges.get(i);
      } else {
        cur = union;
      }
    }
    res.add(cur);
    return res;
  }

  private static KeyRange unionTwoKeyRanges(KeyRange first, KeyRange second) {
    if (first.getEndKey() < second.getBeginKey() || first.getBeginKey() > second.getEndKey()) {
      return null;
    }
    long begin = Math.min(first.getBeginKey(), second.getBeginKey());
    long end = Math.max(first.getEndKey(), second.getEndKey());
    return new KeyRange(begin, end);
  }

  private static KeyRange intersectKeyRanges(List<KeyRange> keyRanges) {
    if (keyRanges == null || keyRanges.isEmpty()) return null;
    KeyRange ret = keyRanges.get(0);
    for (int i = 1; i < keyRanges.size(); i++) {
      ret = intersectTwoKeyRanges(ret, keyRanges.get(i));
    }
    return ret;
  }

  private static KeyRange intersectTwoKeyRanges(KeyRange first, KeyRange second) {
    if (first == null || second == null) return null;
    if (first.getEndKey() < second.getBeginKey() || first.getBeginKey() > second.getEndKey())
      return null;

    long begin = Math.max(first.getBeginKey(), second.getBeginKey());
    long end = Math.min(first.getEndKey(), second.getEndKey());
    return new KeyRange(begin, end);
  }

  public static Filter getSubFilterFromFragment(Filter filter, ColumnsInterval columnsInterval) {
    Filter filterWithoutNot = removeNot(filter);
    Filter filterWithTrue = setTrue(filterWithoutNot, columnsInterval);
    return mergeTrue(filterWithTrue);
  }

  private static Filter setTrue(Filter filter, ColumnsInterval columnsInterval) {
    switch (filter.getType()) {
      case Or:
        List<Filter> orChildren = ((OrFilter) filter).getChildren();
        for (int i = 0; i < orChildren.size(); i++) {
          Filter childFilter = setTrue(orChildren.get(i), columnsInterval);
          orChildren.set(i, childFilter);
        }
        return new OrFilter(orChildren);
      case And:
        List<Filter> andChildren = ((AndFilter) filter).getChildren();
        for (int i = 0; i < andChildren.size(); i++) {
          Filter childFilter = setTrue(andChildren.get(i), columnsInterval);
          andChildren.set(i, childFilter);
        }
        return new AndFilter(andChildren);
      case Value:
        String path = ((ValueFilter) filter).getPath();
        if (columnsInterval.getStartColumn() != null
            && columnsInterval.getStartColumn().compareTo(path) > 0) {
          return new BoolFilter(true);
        }
        if (columnsInterval.getEndColumn() != null
            && columnsInterval.getEndColumn().compareTo(path) <= 0) {
          return new BoolFilter(true);
        }
        return filter;
      default:
        return filter;
    }
  }

  private static Filter mergeTrue(Filter filter) {
    switch (filter.getType()) {
      case Or:
        List<Filter> orChildren = ((OrFilter) filter).getChildren();
        for (int i = 0; i < orChildren.size(); i++) {
          Filter childFilter = mergeTrue(orChildren.get(i));
          orChildren.set(i, childFilter);
        }
        for (Filter childFilter : orChildren) {
          if (childFilter.getType() == FilterType.Bool && ((BoolFilter) childFilter).isTrue()) {
            return new BoolFilter(true);
          }
        }
        return new OrFilter(orChildren);
      case And:
        List<Filter> andChildren = ((AndFilter) filter).getChildren();
        for (int i = 0; i < andChildren.size(); i++) {
          Filter childFilter = mergeTrue(andChildren.get(i));
          andChildren.set(i, childFilter);
        }
        List<Filter> removedList = new ArrayList<>();
        for (Filter childFilter : andChildren) {
          if (childFilter.getType() == FilterType.Bool && ((BoolFilter) childFilter).isTrue()) {
            removedList.add(childFilter);
          }
        }
        for (Filter removed : removedList) {
          andChildren.remove(removed);
        }
        if (andChildren.size() == 0) {
          return new BoolFilter(true);
        } else if (andChildren.size() == 1) {
          return andChildren.get(0);
        } else {
          return new AndFilter(andChildren);
        }
      default:
        return filter;
    }
  }
}
