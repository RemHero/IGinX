package cn.edu.tsinghua.iginx.resource;

import cn.edu.tsinghua.iginx.cost.CostCollector;
import cn.edu.tsinghua.iginx.cost.CostModel;
import cn.edu.tsinghua.iginx.cost.FactorCalculator;
import cn.edu.tsinghua.iginx.cost.entity.CostInfo;
import cn.edu.tsinghua.iginx.engine.physical.storage.IStorage;
import cn.edu.tsinghua.iginx.engine.physical.storage.execute.StoragePhysicalTaskExecutor;
import cn.edu.tsinghua.iginx.engine.shared.operator.Limit;
import cn.edu.tsinghua.iginx.metadata.DefaultMetaManager;
import cn.edu.tsinghua.iginx.metadata.IMetaManager;
import cn.edu.tsinghua.iginx.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class ConnResourceManager {
    // 需要区分该iginx对应底层db的每个负载，应该是一个map
    private static Map<Long, IStorage> storageMap = new HashMap<>();
    private static Map<Long, Integer> storageConnMap = new HashMap<>();
    private static final IMetaManager metaManager = DefaultMetaManager.getInstance();

    public ConnResourceManager() {
        Map<Long, Pair<IStorage, ThreadPoolExecutor>> map = StoragePhysicalTaskExecutor.getInstance().getStorageManager().getStorageMap();
        for (Map.Entry<Long, Pair<IStorage, ThreadPoolExecutor>> entry : map.entrySet()) {
            storageMap.put(entry.getKey(), entry.getValue().k);
            storageConnMap.put(entry.getKey(), 0);
        }
    }

    public static void updateConn(long storageId, Integer connNum) {
        storageConnMap.put(storageId, connNum);
    }

    public static Map<Long, Integer> getStorageConnMap() {
        return storageConnMap;
    }

    // 每隔一段时间按照Cost更新连接数量
    public static void start() {
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //TODO 从zk获取所有连接数量和其他Cost
            List<CostInfo> infos = metaManager.getCost();
            for (CostInfo info : infos) {
                System.out.println(info.toString());
            }
            int totalConnNum = 0;
            double totalCost = 0;
            for (CostInfo info : infos) {
                totalConnNum+=info.storageConnMap.values().stream().mapToInt(Integer::intValue).sum();
                totalCost+=info.costNow;
            }
            totalConnNum = (int) ((int) FactorCalculator.getCostNow()/totalCost * totalConnNum);
            Map<Long, Integer> bestConnNum = CostModel.getBestConnNum(totalConnNum, FactorCalculator.calculateStorageCostMap());
            System.out.println(bestConnNum);
            for (Map.Entry<Long, Integer> entry : bestConnNum.entrySet()) {
                if (storageMap.containsKey(entry.getKey())) {
                    storageMap.get(entry.getKey()).updateConnNum(entry.getValue());
                }
            }
        }
    }

    // TODO 记着在iginxwot中接地啊注释掉
    public static void work() {
        Thread thread = new Thread(ConnResourceManager::start);
        thread.start();
    }
}
