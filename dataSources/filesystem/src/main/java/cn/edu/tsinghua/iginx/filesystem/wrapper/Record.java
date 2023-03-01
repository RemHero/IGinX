package cn.edu.tsinghua.iginx.filesystem.wrapper;

import cn.edu.tsinghua.iginx.thrift.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Record {
    private static final Logger logger = LoggerFactory.getLogger(Record.class);
    private int MAXDATASETLEN = 100;
    private long timestamp;
    private DataType dataType;
    private final byte[] rawData = new byte[MAXDATASETLEN];
    private int index =  0;

//    public void addRawDataRecord(List<Byte> oriData, int beg, int end) {
//        for(int i = beg; i<end; i++) {
//            rawData[index++] = oriData.get(i);
//            if(index >= MAXDATASETLEN) {
//                logger.error("load the data size large than max size {}, had loaded at {} ", MAXDATASETLEN, i);
//                break;
//            }
//        }
//    }

    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < index; i++) {
            res.append(rawData[i] + ",");
        }
        res.deleteCharAt(res.length() - 1);
        return res.toString();
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setMAXDATASETLEN(int length) {
        MAXDATASETLEN = length;
    }
}
