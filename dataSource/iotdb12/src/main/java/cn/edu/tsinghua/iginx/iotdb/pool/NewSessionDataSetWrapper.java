package cn.edu.tsinghua.iginx.iotdb.pool;

import java.util.List;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import org.apache.iotdb.session.pool.SessionDataSetWrapper;
import org.apache.iotdb.session.pool.SessionPool;
import org.apache.iotdb.tsfile.read.common.RowRecord;

public class NewSessionDataSetWrapper extends SessionDataSetWrapper implements AutoCloseable {
    SessionDataSet sessionDataSet;
    Session session;
    SessionPool pool;

    public NewSessionDataSetWrapper(SessionDataSet sessionDataSet, Session session, SessionPool pool) {
        super(sessionDataSet, session, pool);
        this.sessionDataSet = sessionDataSet;
        this.session = session;
        this.pool = pool;
    }

    protected Session getSession() {
        return this.session;
    }

    public int getBatchSize() {
        return this.sessionDataSet.getFetchSize();
    }

    public void setBatchSize(int batchSize) {
        this.sessionDataSet.setFetchSize(batchSize);
    }

    public boolean hasNext() throws IoTDBConnectionException, StatementExecutionException {
        boolean next = this.sessionDataSet.hasNext();
        if (!next) {
            this.pool.closeResultSet(this);
        }

        return next;
    }

    public RowRecord next() throws IoTDBConnectionException, StatementExecutionException {
        return this.sessionDataSet.next();
    }

    public SessionDataSet.DataIterator iterator() {
        return this.sessionDataSet.iterator();
    }

    public List<String> getColumnNames() {
        return this.sessionDataSet.getColumnNames();
    }

    public List<String> getColumnTypes() {
        return this.sessionDataSet.getColumnTypes();
    }

    public void close() {
        this.pool.closeResultSet(this);
    }
}

