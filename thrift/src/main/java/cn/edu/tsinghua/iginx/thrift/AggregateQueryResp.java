/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package cn.edu.tsinghua.iginx.thrift;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2021-03-18")
public class AggregateQueryResp implements org.apache.thrift.TBase<AggregateQueryResp, AggregateQueryResp._Fields>, java.io.Serializable, Cloneable, Comparable<AggregateQueryResp> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("AggregateQueryResp");

  private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField PATHS_FIELD_DESC = new org.apache.thrift.protocol.TField("paths", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField DATA_TYPE_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("dataTypeList", org.apache.thrift.protocol.TType.LIST, (short)3);
  private static final org.apache.thrift.protocol.TField TIMESTAMPS_FIELD_DESC = new org.apache.thrift.protocol.TField("timestamps", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField VALUES_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("valuesList", org.apache.thrift.protocol.TType.STRING, (short)5);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new AggregateQueryRespStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new AggregateQueryRespTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable Status status; // required
  public @org.apache.thrift.annotation.Nullable java.util.List<java.lang.String> paths; // optional
  public @org.apache.thrift.annotation.Nullable java.util.List<DataType> dataTypeList; // optional
  public @org.apache.thrift.annotation.Nullable java.nio.ByteBuffer timestamps; // optional
  public @org.apache.thrift.annotation.Nullable java.nio.ByteBuffer valuesList; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    STATUS((short)1, "status"),
    PATHS((short)2, "paths"),
    DATA_TYPE_LIST((short)3, "dataTypeList"),
    TIMESTAMPS((short)4, "timestamps"),
    VALUES_LIST((short)5, "valuesList");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // STATUS
          return STATUS;
        case 2: // PATHS
          return PATHS;
        case 3: // DATA_TYPE_LIST
          return DATA_TYPE_LIST;
        case 4: // TIMESTAMPS
          return TIMESTAMPS;
        case 5: // VALUES_LIST
          return VALUES_LIST;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final _Fields optionals[] = {_Fields.PATHS,_Fields.DATA_TYPE_LIST,_Fields.TIMESTAMPS,_Fields.VALUES_LIST};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Status.class)));
    tmpMap.put(_Fields.PATHS, new org.apache.thrift.meta_data.FieldMetaData("paths", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.DATA_TYPE_LIST, new org.apache.thrift.meta_data.FieldMetaData("dataTypeList", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, DataType.class))));
    tmpMap.put(_Fields.TIMESTAMPS, new org.apache.thrift.meta_data.FieldMetaData("timestamps", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.VALUES_LIST, new org.apache.thrift.meta_data.FieldMetaData("valuesList", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(AggregateQueryResp.class, metaDataMap);
  }

  public AggregateQueryResp() {
  }

  public AggregateQueryResp(
    Status status)
  {
    this();
    this.status = status;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public AggregateQueryResp(AggregateQueryResp other) {
    if (other.isSetStatus()) {
      this.status = new Status(other.status);
    }
    if (other.isSetPaths()) {
      java.util.List<java.lang.String> __this__paths = new java.util.ArrayList<java.lang.String>(other.paths);
      this.paths = __this__paths;
    }
    if (other.isSetDataTypeList()) {
      java.util.List<DataType> __this__dataTypeList = new java.util.ArrayList<DataType>(other.dataTypeList.size());
      for (DataType other_element : other.dataTypeList) {
        __this__dataTypeList.add(other_element);
      }
      this.dataTypeList = __this__dataTypeList;
    }
    if (other.isSetTimestamps()) {
      this.timestamps = org.apache.thrift.TBaseHelper.copyBinary(other.timestamps);
    }
    if (other.isSetValuesList()) {
      this.valuesList = org.apache.thrift.TBaseHelper.copyBinary(other.valuesList);
    }
  }

  public AggregateQueryResp deepCopy() {
    return new AggregateQueryResp(this);
  }

  @Override
  public void clear() {
    this.status = null;
    this.paths = null;
    this.dataTypeList = null;
    this.timestamps = null;
    this.valuesList = null;
  }

  @org.apache.thrift.annotation.Nullable
  public Status getStatus() {
    return this.status;
  }

  public AggregateQueryResp setStatus(@org.apache.thrift.annotation.Nullable Status status) {
    this.status = status;
    return this;
  }

  public void unsetStatus() {
    this.status = null;
  }

  /** Returns true if field status is set (has been assigned a value) and false otherwise */
  public boolean isSetStatus() {
    return this.status != null;
  }

  public void setStatusIsSet(boolean value) {
    if (!value) {
      this.status = null;
    }
  }

  public int getPathsSize() {
    return (this.paths == null) ? 0 : this.paths.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<java.lang.String> getPathsIterator() {
    return (this.paths == null) ? null : this.paths.iterator();
  }

  public void addToPaths(java.lang.String elem) {
    if (this.paths == null) {
      this.paths = new java.util.ArrayList<java.lang.String>();
    }
    this.paths.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<java.lang.String> getPaths() {
    return this.paths;
  }

  public AggregateQueryResp setPaths(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.String> paths) {
    this.paths = paths;
    return this;
  }

  public void unsetPaths() {
    this.paths = null;
  }

  /** Returns true if field paths is set (has been assigned a value) and false otherwise */
  public boolean isSetPaths() {
    return this.paths != null;
  }

  public void setPathsIsSet(boolean value) {
    if (!value) {
      this.paths = null;
    }
  }

  public int getDataTypeListSize() {
    return (this.dataTypeList == null) ? 0 : this.dataTypeList.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<DataType> getDataTypeListIterator() {
    return (this.dataTypeList == null) ? null : this.dataTypeList.iterator();
  }

  public void addToDataTypeList(DataType elem) {
    if (this.dataTypeList == null) {
      this.dataTypeList = new java.util.ArrayList<DataType>();
    }
    this.dataTypeList.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<DataType> getDataTypeList() {
    return this.dataTypeList;
  }

  public AggregateQueryResp setDataTypeList(@org.apache.thrift.annotation.Nullable java.util.List<DataType> dataTypeList) {
    this.dataTypeList = dataTypeList;
    return this;
  }

  public void unsetDataTypeList() {
    this.dataTypeList = null;
  }

  /** Returns true if field dataTypeList is set (has been assigned a value) and false otherwise */
  public boolean isSetDataTypeList() {
    return this.dataTypeList != null;
  }

  public void setDataTypeListIsSet(boolean value) {
    if (!value) {
      this.dataTypeList = null;
    }
  }

  public byte[] getTimestamps() {
    setTimestamps(org.apache.thrift.TBaseHelper.rightSize(timestamps));
    return timestamps == null ? null : timestamps.array();
  }

  public java.nio.ByteBuffer bufferForTimestamps() {
    return org.apache.thrift.TBaseHelper.copyBinary(timestamps);
  }

  public AggregateQueryResp setTimestamps(byte[] timestamps) {
    this.timestamps = timestamps == null ? (java.nio.ByteBuffer)null   : java.nio.ByteBuffer.wrap(timestamps.clone());
    return this;
  }

  public AggregateQueryResp setTimestamps(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer timestamps) {
    this.timestamps = org.apache.thrift.TBaseHelper.copyBinary(timestamps);
    return this;
  }

  public void unsetTimestamps() {
    this.timestamps = null;
  }

  /** Returns true if field timestamps is set (has been assigned a value) and false otherwise */
  public boolean isSetTimestamps() {
    return this.timestamps != null;
  }

  public void setTimestampsIsSet(boolean value) {
    if (!value) {
      this.timestamps = null;
    }
  }

  public byte[] getValuesList() {
    setValuesList(org.apache.thrift.TBaseHelper.rightSize(valuesList));
    return valuesList == null ? null : valuesList.array();
  }

  public java.nio.ByteBuffer bufferForValuesList() {
    return org.apache.thrift.TBaseHelper.copyBinary(valuesList);
  }

  public AggregateQueryResp setValuesList(byte[] valuesList) {
    this.valuesList = valuesList == null ? (java.nio.ByteBuffer)null   : java.nio.ByteBuffer.wrap(valuesList.clone());
    return this;
  }

  public AggregateQueryResp setValuesList(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer valuesList) {
    this.valuesList = org.apache.thrift.TBaseHelper.copyBinary(valuesList);
    return this;
  }

  public void unsetValuesList() {
    this.valuesList = null;
  }

  /** Returns true if field valuesList is set (has been assigned a value) and false otherwise */
  public boolean isSetValuesList() {
    return this.valuesList != null;
  }

  public void setValuesListIsSet(boolean value) {
    if (!value) {
      this.valuesList = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case STATUS:
      if (value == null) {
        unsetStatus();
      } else {
        setStatus((Status)value);
      }
      break;

    case PATHS:
      if (value == null) {
        unsetPaths();
      } else {
        setPaths((java.util.List<java.lang.String>)value);
      }
      break;

    case DATA_TYPE_LIST:
      if (value == null) {
        unsetDataTypeList();
      } else {
        setDataTypeList((java.util.List<DataType>)value);
      }
      break;

    case TIMESTAMPS:
      if (value == null) {
        unsetTimestamps();
      } else {
        if (value instanceof byte[]) {
          setTimestamps((byte[])value);
        } else {
          setTimestamps((java.nio.ByteBuffer)value);
        }
      }
      break;

    case VALUES_LIST:
      if (value == null) {
        unsetValuesList();
      } else {
        if (value instanceof byte[]) {
          setValuesList((byte[])value);
        } else {
          setValuesList((java.nio.ByteBuffer)value);
        }
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case STATUS:
      return getStatus();

    case PATHS:
      return getPaths();

    case DATA_TYPE_LIST:
      return getDataTypeList();

    case TIMESTAMPS:
      return getTimestamps();

    case VALUES_LIST:
      return getValuesList();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case STATUS:
      return isSetStatus();
    case PATHS:
      return isSetPaths();
    case DATA_TYPE_LIST:
      return isSetDataTypeList();
    case TIMESTAMPS:
      return isSetTimestamps();
    case VALUES_LIST:
      return isSetValuesList();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof AggregateQueryResp)
      return this.equals((AggregateQueryResp)that);
    return false;
  }

  public boolean equals(AggregateQueryResp that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_status = true && this.isSetStatus();
    boolean that_present_status = true && that.isSetStatus();
    if (this_present_status || that_present_status) {
      if (!(this_present_status && that_present_status))
        return false;
      if (!this.status.equals(that.status))
        return false;
    }

    boolean this_present_paths = true && this.isSetPaths();
    boolean that_present_paths = true && that.isSetPaths();
    if (this_present_paths || that_present_paths) {
      if (!(this_present_paths && that_present_paths))
        return false;
      if (!this.paths.equals(that.paths))
        return false;
    }

    boolean this_present_dataTypeList = true && this.isSetDataTypeList();
    boolean that_present_dataTypeList = true && that.isSetDataTypeList();
    if (this_present_dataTypeList || that_present_dataTypeList) {
      if (!(this_present_dataTypeList && that_present_dataTypeList))
        return false;
      if (!this.dataTypeList.equals(that.dataTypeList))
        return false;
    }

    boolean this_present_timestamps = true && this.isSetTimestamps();
    boolean that_present_timestamps = true && that.isSetTimestamps();
    if (this_present_timestamps || that_present_timestamps) {
      if (!(this_present_timestamps && that_present_timestamps))
        return false;
      if (!this.timestamps.equals(that.timestamps))
        return false;
    }

    boolean this_present_valuesList = true && this.isSetValuesList();
    boolean that_present_valuesList = true && that.isSetValuesList();
    if (this_present_valuesList || that_present_valuesList) {
      if (!(this_present_valuesList && that_present_valuesList))
        return false;
      if (!this.valuesList.equals(that.valuesList))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetStatus()) ? 131071 : 524287);
    if (isSetStatus())
      hashCode = hashCode * 8191 + status.hashCode();

    hashCode = hashCode * 8191 + ((isSetPaths()) ? 131071 : 524287);
    if (isSetPaths())
      hashCode = hashCode * 8191 + paths.hashCode();

    hashCode = hashCode * 8191 + ((isSetDataTypeList()) ? 131071 : 524287);
    if (isSetDataTypeList())
      hashCode = hashCode * 8191 + dataTypeList.hashCode();

    hashCode = hashCode * 8191 + ((isSetTimestamps()) ? 131071 : 524287);
    if (isSetTimestamps())
      hashCode = hashCode * 8191 + timestamps.hashCode();

    hashCode = hashCode * 8191 + ((isSetValuesList()) ? 131071 : 524287);
    if (isSetValuesList())
      hashCode = hashCode * 8191 + valuesList.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(AggregateQueryResp other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetStatus()).compareTo(other.isSetStatus());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStatus()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, other.status);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetPaths()).compareTo(other.isSetPaths());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPaths()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.paths, other.paths);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetDataTypeList()).compareTo(other.isSetDataTypeList());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDataTypeList()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.dataTypeList, other.dataTypeList);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetTimestamps()).compareTo(other.isSetTimestamps());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTimestamps()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timestamps, other.timestamps);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetValuesList()).compareTo(other.isSetValuesList());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValuesList()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.valuesList, other.valuesList);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("AggregateQueryResp(");
    boolean first = true;

    sb.append("status:");
    if (this.status == null) {
      sb.append("null");
    } else {
      sb.append(this.status);
    }
    first = false;
    if (isSetPaths()) {
      if (!first) sb.append(", ");
      sb.append("paths:");
      if (this.paths == null) {
        sb.append("null");
      } else {
        sb.append(this.paths);
      }
      first = false;
    }
    if (isSetDataTypeList()) {
      if (!first) sb.append(", ");
      sb.append("dataTypeList:");
      if (this.dataTypeList == null) {
        sb.append("null");
      } else {
        sb.append(this.dataTypeList);
      }
      first = false;
    }
    if (isSetTimestamps()) {
      if (!first) sb.append(", ");
      sb.append("timestamps:");
      if (this.timestamps == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.timestamps, sb);
      }
      first = false;
    }
    if (isSetValuesList()) {
      if (!first) sb.append(", ");
      sb.append("valuesList:");
      if (this.valuesList == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.valuesList, sb);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (status == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'status' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (status != null) {
      status.validate();
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class AggregateQueryRespStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public AggregateQueryRespStandardScheme getScheme() {
      return new AggregateQueryRespStandardScheme();
    }
  }

  private static class AggregateQueryRespStandardScheme extends org.apache.thrift.scheme.StandardScheme<AggregateQueryResp> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, AggregateQueryResp struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // STATUS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.status = new Status();
              struct.status.read(iprot);
              struct.setStatusIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // PATHS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list150 = iprot.readListBegin();
                struct.paths = new java.util.ArrayList<java.lang.String>(_list150.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _elem151;
                for (int _i152 = 0; _i152 < _list150.size; ++_i152)
                {
                  _elem151 = iprot.readString();
                  struct.paths.add(_elem151);
                }
                iprot.readListEnd();
              }
              struct.setPathsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // DATA_TYPE_LIST
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list153 = iprot.readListBegin();
                struct.dataTypeList = new java.util.ArrayList<DataType>(_list153.size);
                @org.apache.thrift.annotation.Nullable DataType _elem154;
                for (int _i155 = 0; _i155 < _list153.size; ++_i155)
                {
                  _elem154 = cn.edu.tsinghua.iginx.thrift.DataType.findByValue(iprot.readI32());
                  if (_elem154 != null)
                  {
                    struct.dataTypeList.add(_elem154);
                  }
                }
                iprot.readListEnd();
              }
              struct.setDataTypeListIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // TIMESTAMPS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.timestamps = iprot.readBinary();
              struct.setTimestampsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // VALUES_LIST
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.valuesList = iprot.readBinary();
              struct.setValuesListIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, AggregateQueryResp struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.status != null) {
        oprot.writeFieldBegin(STATUS_FIELD_DESC);
        struct.status.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.paths != null) {
        if (struct.isSetPaths()) {
          oprot.writeFieldBegin(PATHS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.paths.size()));
            for (java.lang.String _iter156 : struct.paths)
            {
              oprot.writeString(_iter156);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.dataTypeList != null) {
        if (struct.isSetDataTypeList()) {
          oprot.writeFieldBegin(DATA_TYPE_LIST_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, struct.dataTypeList.size()));
            for (DataType _iter157 : struct.dataTypeList)
            {
              oprot.writeI32(_iter157.getValue());
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.timestamps != null) {
        if (struct.isSetTimestamps()) {
          oprot.writeFieldBegin(TIMESTAMPS_FIELD_DESC);
          oprot.writeBinary(struct.timestamps);
          oprot.writeFieldEnd();
        }
      }
      if (struct.valuesList != null) {
        if (struct.isSetValuesList()) {
          oprot.writeFieldBegin(VALUES_LIST_FIELD_DESC);
          oprot.writeBinary(struct.valuesList);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class AggregateQueryRespTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public AggregateQueryRespTupleScheme getScheme() {
      return new AggregateQueryRespTupleScheme();
    }
  }

  private static class AggregateQueryRespTupleScheme extends org.apache.thrift.scheme.TupleScheme<AggregateQueryResp> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, AggregateQueryResp struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.status.write(oprot);
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetPaths()) {
        optionals.set(0);
      }
      if (struct.isSetDataTypeList()) {
        optionals.set(1);
      }
      if (struct.isSetTimestamps()) {
        optionals.set(2);
      }
      if (struct.isSetValuesList()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetPaths()) {
        {
          oprot.writeI32(struct.paths.size());
          for (java.lang.String _iter158 : struct.paths)
          {
            oprot.writeString(_iter158);
          }
        }
      }
      if (struct.isSetDataTypeList()) {
        {
          oprot.writeI32(struct.dataTypeList.size());
          for (DataType _iter159 : struct.dataTypeList)
          {
            oprot.writeI32(_iter159.getValue());
          }
        }
      }
      if (struct.isSetTimestamps()) {
        oprot.writeBinary(struct.timestamps);
      }
      if (struct.isSetValuesList()) {
        oprot.writeBinary(struct.valuesList);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, AggregateQueryResp struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.status = new Status();
      struct.status.read(iprot);
      struct.setStatusIsSet(true);
      java.util.BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list160 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.paths = new java.util.ArrayList<java.lang.String>(_list160.size);
          @org.apache.thrift.annotation.Nullable java.lang.String _elem161;
          for (int _i162 = 0; _i162 < _list160.size; ++_i162)
          {
            _elem161 = iprot.readString();
            struct.paths.add(_elem161);
          }
        }
        struct.setPathsIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list163 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, iprot.readI32());
          struct.dataTypeList = new java.util.ArrayList<DataType>(_list163.size);
          @org.apache.thrift.annotation.Nullable DataType _elem164;
          for (int _i165 = 0; _i165 < _list163.size; ++_i165)
          {
            _elem164 = cn.edu.tsinghua.iginx.thrift.DataType.findByValue(iprot.readI32());
            if (_elem164 != null)
            {
              struct.dataTypeList.add(_elem164);
            }
          }
        }
        struct.setDataTypeListIsSet(true);
      }
      if (incoming.get(2)) {
        struct.timestamps = iprot.readBinary();
        struct.setTimestampsIsSet(true);
      }
      if (incoming.get(3)) {
        struct.valuesList = iprot.readBinary();
        struct.setValuesListIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

