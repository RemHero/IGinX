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
package cn.edu.tsinghua.iginx.engine.shared.exception;

import cn.edu.tsinghua.iginx.exception.StatusCode;
import cn.edu.tsinghua.iginx.thrift.Status;

public class StatementExecutionException extends EngineException {

  private static final long serialVersionUID = -7769482614133326007L;

  protected int errorCode;

  public StatementExecutionException(Status status) {
    super(status.message);
    errorCode = status.code;
  }

  public StatementExecutionException(String message) {
    super(message);
    errorCode = StatusCode.STATEMENT_EXECUTION_ERROR.getStatusCode();
  }

  public StatementExecutionException(Throwable cause) {
    super(cause);
    errorCode = StatusCode.STATEMENT_EXECUTION_ERROR.getStatusCode();
  }

  public StatementExecutionException(String message, Throwable cause) {
    super(message, cause);
    errorCode = StatusCode.STATEMENT_EXECUTION_ERROR.getStatusCode();
  }
}