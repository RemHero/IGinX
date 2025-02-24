package cn.edu.tsinghua.iginx.cost.entity;

import java.util.HashMap;
import java.util.Map;

// TODO cost要包含最终的行数以及project读取的行数
public class CostInfo {
    public String ip;
    public int port;
    // 总开销，按照总开销的比例分配连接数量
    public Double costNow;
    // TODO 主要是为了考虑是否需要调增连接数量，如果调整连接的开销很大，收益很小，则不调整
    public Map<Long, Integer> storageConnMap = new HashMap<>();

    public String getIpAndPort() {
        return ip + "-" + port;
    }

    @Override
    public String toString() {
        return "CostInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", costNow=" + costNow +
                ", storageConnMap=" + storageConnMap +
                '}';
    }
}
