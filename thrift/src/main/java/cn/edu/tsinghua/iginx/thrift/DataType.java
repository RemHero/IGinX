/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package cn.edu.tsinghua.iginx.thrift;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2021-03-17")
public enum DataType implements org.apache.thrift.TEnum {
  BOOLEAN(0),
  INTEGER(1),
  LONG(2),
  FLOAT(3),
  DOUBLE(4),
  STRING(5);

  private final int value;

  private DataType(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  @org.apache.thrift.annotation.Nullable
  public static DataType findByValue(int value) { 
    switch (value) {
      case 0:
        return BOOLEAN;
      case 1:
        return INTEGER;
      case 2:
        return LONG;
      case 3:
        return FLOAT;
      case 4:
        return DOUBLE;
      case 5:
        return STRING;
      default:
        return null;
    }
  }
}
