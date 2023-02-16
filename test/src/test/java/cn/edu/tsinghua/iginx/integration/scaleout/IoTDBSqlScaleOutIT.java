package cn.edu.tsinghua.iginx.integration.scaleout;

import cn.edu.tsinghua.iginx.exceptions.ExecutionException;
import cn.edu.tsinghua.iginx.exceptions.SessionException;
import cn.edu.tsinghua.iginx.integration.SQLSessionIT;
import cn.edu.tsinghua.iginx.integration.rest.RestIT;
import cn.edu.tsinghua.iginx.utils.FileReader;
import org.junit.Test;

public class IoTDBSqlScaleOutIT extends SQLSessionIT implements IoTDBBaseScaleOutIT{
    public IoTDBSqlScaleOutIT() {
        super();
    }

    public void DBConf() throws Exception {
        this.storageEngineType = FileReader.convertToString("./src/test/java/cn/edu/tsinghua/iginx/integration/DBConf.txt");
//        this.ifClearData = false;
    }

    @Test
    public void oriHasDataExpHasDataIT() throws Exception {
        DBConf();
        SQLSessionIT.session.executeSql("ADD STORAGEENGINE (\"127.0.0.1\", 6668, \"" + storageEngineType + "\", \"username:root, password:root, sessionPoolSize:20, has_data:true, is_read_only:true\");");
        capacityExpansion();
    }

    @Test
    public void oriHasDataExpNoDataIT() throws Exception {
        DBConf();
        SQLSessionIT.session.executeSql("ADD STORAGEENGINE (\"127.0.0.1\", 6668, \"" + storageEngineType + "\", \"username:root, password:root, sessionPoolSize:20, has_data:no, is_read_only:true\");");
        capacityExpansion();
    }

    @Test
    public void oriNoDataExpHasDataIT() throws Exception {
        DBConf();
        SQLSessionIT.session.executeSql("ADD STORAGEENGINE (\"127.0.0.1\", 6668, \"" + storageEngineType + "\", \"username:root, password:root, sessionPoolSize:20, has_data:true, is_read_only:true\");");
        capacityExpansion();
    }

    @Test
    public void oriNoDataExpNoDataIT() throws Exception {
        DBConf();
        SQLSessionIT.session.executeSql("ADD STORAGEENGINE (\"127.0.0.1\", 6668, \"" + storageEngineType + "\", \"username:root, password:root, sessionPoolSize:20, has_data:no, is_read_only:true\");");
        capacityExpansion();
    }

}
