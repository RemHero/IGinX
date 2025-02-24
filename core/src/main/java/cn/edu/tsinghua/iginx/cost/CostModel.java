package cn.edu.tsinghua.iginx.cost;

import cn.edu.tsinghua.iginx.engine.physical.storage.IStorage;
import cn.edu.tsinghua.iginx.engine.physical.storage.execute.StoragePhysicalTaskExecutor;
import cn.edu.tsinghua.iginx.utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

// 综合各种因子，计算组合成本，并计算最佳代价
public class CostModel {
    // 需要区分该iginx对应底层db的每个负载，应该是一个map
    private static Map<Long, IStorage> storageMap = new HashMap<>();
    private static Map<Long, Double> storageCostMap = new HashMap<>();

    public CostModel() {
        Map<Long, Pair<IStorage, ThreadPoolExecutor>> map = StoragePhysicalTaskExecutor.getInstance().getStorageManager().getStorageMap();
        for (Map.Entry<Long, Pair<IStorage, ThreadPoolExecutor>> entry : map.entrySet()) {
            storageMap.put(entry.getKey(), entry.getValue().k);
            storageCostMap.put(entry.getKey(), 0.0);
        }
    }

    public static double getCost(long storageId) {
        return storageCostMap.get(storageId);
    }

    public static Map<Long, Integer> getBestConnNum(int totalConnNum, Map<Long, Double> clusterStorageCostMap) {
        Map<Long, Integer> bestConnNum = new HashMap<>();
        double totalCost = 0;
        for (Map.Entry<Long, Double> entry : clusterStorageCostMap.entrySet()) {
            totalCost += entry.getValue();
        }
        if (totalConnNum > clusterStorageCostMap.size()) {
            for (Map.Entry<Long, Double> entry : clusterStorageCostMap.entrySet()) {
                bestConnNum.put(entry.getKey(), (int) (entry.getValue() / totalCost * totalConnNum));
            }
        }
        return bestConnNum;
    }

}
