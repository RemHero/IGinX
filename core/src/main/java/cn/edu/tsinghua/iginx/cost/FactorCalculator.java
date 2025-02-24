package cn.edu.tsinghua.iginx.cost;

import cn.edu.tsinghua.iginx.engine.shared.operator.*;

import java.util.*;

// 从CostCollecter中获取统计raw数据，通过采样或者某种方式获取cost因子
public class FactorCalculator {
    public static double costNow, incomingCost, inQueueMemOpCost = 0;
    private static int RECORDE_SIZE = 50;
    private static List<Double> costList = new ArrayList<>();
    public static Map<Long, List<Double>> storageCostMap = new HashMap<>();


    static {
        Thread thread = new Thread(FactorCalculator::startCollectCost);
        thread.start();
    }

    public static double calculateCost() {
        double allCost = 0;
        for (double cost : costList) {
            allCost += cost;
        }
        if (costList.size()!=0) {
            return allCost / costList.size();
        }
        return -1;
    }

    public static double getCostNow() {
        return CostCollector.inQueueMemOpCost;
    }

    public static Map<Long, Double> calculateStorageCostMap() {
        Map<Long, Double> ret = new HashMap<>();
        for (Map.Entry<Long, List<Double>> entry : storageCostMap.entrySet()) {
            double allCost = 0;
            for (double cost : entry.getValue()) {
                allCost += cost;
            }
            if (entry.getValue().size()!=0) {
                ret.put(entry.getKey(), allCost / entry.getValue().size());
            }
        }
        return ret;
    }

    public static double getInQueueMemOpCost() {
        return inQueueMemOpCost;
    }

    // 每隔一段时间按照Cost更新连接数量
    public static void startCollectCost() {
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (costList.size() >= RECORDE_SIZE) {
                costList.remove(0);
            }
            // cost
            double cost = CostCollector.inQueueMemOpCost;
            if (cost != -1) {
                costList.add(cost);
            }

            // storage cost
            Map<Long, Double> storageCostMapNow = CostCollector.storageCostMapNow;
            for (Map.Entry<Long, Double> entry : storageCostMapNow.entrySet()) {
                if (storageCostMap.containsKey(entry.getKey())) {
                    if (storageCostMap.get(entry.getKey()).size() < RECORDE_SIZE) {
                        storageCostMap.get(entry.getKey()).add(entry.getValue());
                    } else {
                        storageCostMap.get(entry.getKey()).remove(0);
                        storageCostMap.get(entry.getKey()).add(entry.getValue());
                    }
                } else {
                    storageCostMap.put(entry.getKey(), new ArrayList<>());
                    storageCostMap.get(entry.getKey()).add(entry.getValue());
                }
            }
        }
    }
}
