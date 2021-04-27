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
package cn.edu.tsinghua.iginx.plan.downsample;

import cn.edu.tsinghua.iginx.metadata.entity.StorageUnitMeta;
import cn.edu.tsinghua.iginx.plan.DataPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DownsampleQueryPlan extends DataPlan {

    private static final Logger logger = LoggerFactory.getLogger(DownsampleQueryPlan.class);

    private final long precision;

    public DownsampleQueryPlan(List<String> paths, long startTime, long endTime, long precision) {
        this(paths, startTime, endTime, precision, null);
    }

    public DownsampleQueryPlan(List<String> paths, long startTime, long endTime, long precision, StorageUnitMeta storageUnit) {
        super(true, paths, startTime, endTime, storageUnit);
        this.setIginxPlanType(IginxPlanType.DOWNSAMPLE_QUERY);
        this.setSync(true);
        this.precision = precision;
    }

    public long getPrecision() {
        return precision;
    }

}
