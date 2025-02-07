package cn.edu.tsinghua.iginx.statistics.data;

import java.io.*;
import java.util.Objects;

public class Bounds<T extends Comparable<T> & Serializable> implements Serializable {
  private static final long serialVersionUID = 1L;
  private final T lowerBound;
  private final T upperBound;
  boolean LowExclude;
  boolean HighExclude;
  private final Class<T> type;

  // Constructor to initialize the bounds.
  public Bounds(T lowerBound, T upperBound, Class<T> type) {
    if (lowerBound == null || upperBound == null) {
      throw new IllegalArgumentException("Bounds cannot be null");
    }
    if (lowerBound.compareTo(upperBound) > 0) {
      throw new IllegalArgumentException("Lower bound cannot be greater than upper bound");
    }
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.type = type;
  }

  public Class<T> getType() {
    return type;
  }

  // Getter for lower bound.
  public T getLowerBound() {
    return lowerBound;
  }

  // Getter for upper bound.
  public T getUpperBound() {
    return upperBound;
  }

  public boolean isSameToUpper(T value) {
    return upperBound.compareTo(value) == 0;
  }

  // Check if a value is within the bounds.
  public boolean contains(T value) {
    return value != null && lowerBound.compareTo(value) <= 0 && upperBound.compareTo(value) >= 0;
  }

  @Override
  public String toString() {
    return "Bounds{" + "lowerBound=" + lowerBound + ", upperBound=" + upperBound + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Bounds<?> bounds = (Bounds<?>) o;
    return Objects.equals(lowerBound, bounds.lowerBound)
        && Objects.equals(upperBound, bounds.upperBound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lowerBound, upperBound);
  }

  // Factory methods for creating bounds with specific types.
  public static Bounds<Integer> intBounds(int lower, int upper) {
    return new Bounds<>(lower, upper, Integer.class);
  }

  public static Bounds<Double> doubleBounds(double lower, double upper) {
    return new Bounds<>(lower, upper, Double.class);
  }

  public static Bounds<Float> floatBounds(float lower, float upper) {
    return new Bounds<>(lower, upper, Float.class);
  }

  public static Bounds<String> stringBounds(String lower, String upper) {
    return new Bounds<>(lower, upper, String.class);
  }

  // Serialize to byte array
  public byte[] serializeToBytes() throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(this);
      return baos.toByteArray();
    }
  }

  // Deserialize from byte array
  @SuppressWarnings("unchecked")
  public static <T extends Comparable<T> & Serializable> Bounds<T> deserializeFromBytes(
      byte[] bytes) throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais)) {
      return (Bounds<T>) ois.readObject();
    }
  }

  // Test method
  //    public static void main(String[] args) {
  //        try {
  //            // 创建一个 Bounds 实例
  //            Bounds<Integer> bounds = Bounds.intBounds(1, 10);
  //
  //            // 序列化为字节数组
  //            byte[] serializedBytes = bounds.serializeToBytes();
  //            System.out.println("Serialized to bytes.");
  //
  //            // 从字节数组反序列化
  //            Bounds<Integer> deserializedBounds = Bounds.deserializeFromBytes(serializedBytes);
  //            System.out.println("Deserialized: " + deserializedBounds);
  //
  //        } catch (IOException | ClassNotFoundException e) {
  //            e.printStackTrace();
  //        }
  //    }
}
