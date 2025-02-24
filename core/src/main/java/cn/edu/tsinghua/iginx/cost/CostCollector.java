package cn.edu.tsinghua.iginx.cost;

import cn.edu.tsinghua.iginx.conf.Config;
import cn.edu.tsinghua.iginx.conf.ConfigDescriptor;
import cn.edu.tsinghua.iginx.cost.entity.CostInfo;
import cn.edu.tsinghua.iginx.engine.shared.operator.Project;
import cn.edu.tsinghua.iginx.metadata.DefaultMetaManager;
import cn.edu.tsinghua.iginx.metadata.IMetaManager;
import cn.edu.tsinghua.iginx.resource.ConnResourceManager;

import java.util.HashMap;
import java.util.Map;

public class CostCollector {
    private static CostInfo costInfo;
    private static final Config config = ConfigDescriptor.getInstance().getConfig();
    private static final IMetaManager metaManager = DefaultMetaManager.getInstance();
    public static Map<Long, Double> storageCostMapNow = new HashMap<>();
    public static double costNow, incomingCost, inQueueMemOpCost = 0;

    static {
        costInfo = new CostInfo();
        costInfo.ip =  config.getIp();
        costInfo.port = config.getPort();
        Thread thread = new Thread(CostCollector::startCollect);
        thread.start();
    }

    public static void collectCost() {
        costInfo.costNow = FactorCalculator.calculateCost();
        costInfo.storageConnMap = ConnResourceManager.getStorageConnMap();
    }

    public static void collectStorageCost(long storageId, Project project, boolean isOptimized) {
        if (!isOptimized && project.getStatsInfo()!=null) {
            storageCostMapNow.put(storageId, (double)project.getStatsInfo().getCount());
        }
    }

    public static void updateInQueueCost(double cost, boolean isComing) {
        if (isComing) {
            inQueueMemOpCost += cost;
        } else {
            inQueueMemOpCost -= cost;
        }
    }

    // 同步到meta
    public static void startCollect() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            collectCost();
            metaManager.updateCost(costInfo);
        }
    }

    public static void work(){}
}
