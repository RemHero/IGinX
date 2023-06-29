/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.edu.tsinghua.iginx.utils;

import cn.edu.tsinghua.iginx.thrift.DataType;

public class DataTypeUtils {

  public static boolean isNumber(DataType dataType) {
    return dataType == DataType.INTEGER
        || dataType == DataType.LONG
        || dataType == DataType.FLOAT
        || dataType == DataType.DOUBLE;
  }

  public static boolean isFloatingNumber(DataType dataType) {
    return dataType == DataType.FLOAT || dataType == DataType.DOUBLE;
  }

  public static boolean isWholeNumber(DataType dataType) {
    return dataType == DataType.INTEGER || dataType == DataType.LONG;
  }

  public static DataType strToDataType(String type) {
    switch (type.toLowerCase()) {
      case "boolean":
        return DataType.BOOLEAN;
      case "integer":
        return DataType.INTEGER;
      case "long":
        return DataType.LONG;
      case "float":
        return DataType.FLOAT;
      case "double":
        return DataType.DOUBLE;
      case "binary":
        return DataType.BINARY;
      default:
        return null;
    }
  }

  public static Object parseStringByDataTyp(String val, DataType type) {
    switch (type) {
      case BOOLEAN:
        return Boolean.parseBoolean(val);
      case LONG:
        return Long.parseLong(val);
      case DOUBLE:
        return Double.parseDouble(val);
      case BINARY:
        return val.getBytes();
      case INTEGER:
        return Integer.parseInt(val);
      case FLOAT:
        return Float.parseFloat(val);
      default:
        return val;
    }
  }
}
