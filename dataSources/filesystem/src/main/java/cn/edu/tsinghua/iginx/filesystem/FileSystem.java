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
package cn.edu.tsinghua.iginx.filesystem;

import cn.edu.tsinghua.iginx.engine.physical.exception.NonExecutablePhysicalTaskException;
import cn.edu.tsinghua.iginx.engine.physical.exception.PhysicalException;
import cn.edu.tsinghua.iginx.engine.physical.exception.StorageInitializationException;
import cn.edu.tsinghua.iginx.engine.physical.storage.IStorage;
import cn.edu.tsinghua.iginx.engine.physical.storage.domain.Timeseries;
import cn.edu.tsinghua.iginx.engine.physical.task.StoragePhysicalTask;
import cn.edu.tsinghua.iginx.engine.physical.task.TaskExecuteResult;
import cn.edu.tsinghua.iginx.engine.shared.operator.*;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.AndFilter;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Filter;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.KeyFilter;
import cn.edu.tsinghua.iginx.engine.shared.operator.filter.Op;
import cn.edu.tsinghua.iginx.engine.shared.operator.type.OperatorType;
import cn.edu.tsinghua.iginx.filesystem.exec.Executor;
import cn.edu.tsinghua.iginx.filesystem.exec.LocalExecutor;
import cn.edu.tsinghua.iginx.filesystem.exec.RemoteExecutor;
import cn.edu.tsinghua.iginx.filesystem.file.property.FilePath;
import cn.edu.tsinghua.iginx.filesystem.server.FileSystemServer;
import cn.edu.tsinghua.iginx.filesystem.tools.ConfLoader;
import cn.edu.tsinghua.iginx.filesystem.tools.FilterTransformer;
import cn.edu.tsinghua.iginx.metadata.entity.FragmentMeta;
import cn.edu.tsinghua.iginx.metadata.entity.StorageEngineMeta;
import cn.edu.tsinghua.iginx.metadata.entity.TimeInterval;
import cn.edu.tsinghua.iginx.metadata.entity.TimeSeriesRange;
import cn.edu.tsinghua.iginx.utils.Pair;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystem implements IStorage {
    private static final String STORAGE_ENGINE = "filesystem";
    public static final int MAXFILESIZE = 100_000_000;
    private static final Logger logger = LoggerFactory.getLogger(FileSystem.class);
    private final StorageEngineMeta meta;
    ExecutorService executorService = Executors.newFixedThreadPool(1);//为了更好的管理线程
    private Executor executor;
    private String root = ConfLoader.getRootPath();

    public FileSystem(StorageEngineMeta meta)
            throws StorageInitializationException, TTransportException {
        if (!meta.getStorageEngine().equals(STORAGE_ENGINE)) {
            throw new StorageInitializationException(
                    "unexpected database: " + meta.getStorageEngine());
        }

        boolean isLocal = ConfLoader.ifLocalFileSystem();
        if (isLocal) {
            initLocalExecutor(meta);
        } else {
            executor = new RemoteExecutor(meta.getIp(), meta.getPort());
        }
        this.meta = meta;
    }

    private void initLocalExecutor(StorageEngineMeta meta) {
        String argRoot = meta.getExtraParams().get("dir");
        root = argRoot == null ? root : FilePath.getRootFromArg(argRoot);

        this.executor = new LocalExecutor(root);

        executorService.submit(new Thread(new FileSystemServer(meta.getPort(), executor)));
    }

    @Override
    public TaskExecuteResult execute(StoragePhysicalTask task) {
        List<Operator> operators = task.getOperators();
        if (operators.size() < 1) {
            return new TaskExecuteResult(
                    new NonExecutablePhysicalTaskException(
                            "storage physical task should have one more operators"));
        }
        Operator op = operators.get(0);
        String storageUnit = task.getStorageUnit();
        boolean isDummyStorageUnit = task.isDummyStorageUnit();
        if (op.getType() == OperatorType.Project) {
            Project project = (Project) op;
            Filter filter;
            if (operators.size() == 2) {
                filter = ((Select) operators.get(1)).getFilter();
            } else {
                FragmentMeta fragment = task.getTargetFragment();
                filter =
                        new AndFilter(
                                Arrays.asList(
                                        new KeyFilter(
                                                Op.GE, fragment.getTimeInterval().getStartTime()),
                                        new KeyFilter(
                                                Op.L, fragment.getTimeInterval().getEndTime())));
            }
            return executor.executeProjectTask(
                    project, FilterTransformer.toBinary(filter), storageUnit, isDummyStorageUnit);
        } else if (op.getType() == OperatorType.Insert) {
            Insert insert = (Insert) op;
            return executor.executeInsertTask(insert, storageUnit);
        } else if (op.getType() == OperatorType.Delete) {
            Delete delete = (Delete) op;
            return executor.executeDeleteTask(delete, storageUnit);
        }
        return new TaskExecuteResult(
                new NonExecutablePhysicalTaskException("unsupported physical task"));
    }

    @Override
    public List<Timeseries> getTimeSeries() throws PhysicalException {
        return executor.getTimeSeriesOfStorageUnit("*");
    }

    @Override
    public Pair<TimeSeriesRange, TimeInterval> getBoundaryOfStorage(String prefix)
            throws PhysicalException {
        return executor.getBoundaryOfStorage(prefix);
    }

    @Override
    public void release() throws PhysicalException {
        executor.close();
        executorService.shutdown();
    }
}
