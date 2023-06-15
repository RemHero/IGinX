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
package cn.edu.tsinghua.iginx.rest.bean;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class QueryResultDataset {
private int size = 0;
private int sampleSize = 0;
private List<Long> keys = new ArrayList<>();
private List<Object> values = new ArrayList<>();
private List<List<Object>> valueLists = new ArrayList<>(); // 对应一个path的数据点序列值
private List<List<Long>> keyLists = new ArrayList<>(); // 数据点的对应时间序列
private List<String> paths = new ArrayList<>();
private List<String> titles = new ArrayList<>();
private List<String> descriptions = new ArrayList<>();
private List<List<String>> categoryLists = new ArrayList<>();

public void addPath(String path) {
    paths.add(path);
}

public void addTitle(String title) {
    titles.add(title);
}

public void addDescription(String description) {
    descriptions.add(description);
}

public void addCategory(List<String> categoryList) {
    categoryLists.add(categoryList);
}

private void addSize() {
    this.size++;
}

private void addKey(long key) {
    this.keys.add(key);
}

public void addValue(Object value) {
    this.values.add(value);
}

public void add(long key, Object value) {
    addKey(key);
    addValue(value);
    addSize();
}

public void addValueLists(List<Object> value) {
    valueLists.add(value);
}

public void addKeyLists(List<Long> keyList) {
    keyLists.add(keyList);
}

public void addPlus(
    String path,
    String description,
    List<String> categoryList,
    List<Object> valueList,
    List<Long> keyList) {
    paths.add(path);
    descriptions.add(description);
    categoryLists.add(categoryList);
    valueLists.add(valueList);
    keyLists.add(keyList);
}
}
