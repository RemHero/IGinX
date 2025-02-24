//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cn.edu.tsinghua.iginx.iotdb.pool;

import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.session.SessionDataSet;
import org.apache.iotdb.session.pool.SessionDataSetWrapper;
import org.apache.iotdb.session.pool.SessionPool;
import org.apache.iotdb.tsfile.file.metadata.enums.CompressionType;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.file.metadata.enums.TSEncoding;
import org.apache.iotdb.tsfile.write.record.Tablet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewSessionPool extends SessionPool {
    private static final Logger logger = LoggerFactory.getLogger(NewSessionPool.class);
    public static final String SESSION_POOL_IS_CLOSED = "Session pool is closed";
    public static final String CLOSE_THE_SESSION_FAILED = "close the session failed.";
    private static final int RETRY = 3;
    private static final int FINAL_RETRY = 2;
    private final ConcurrentLinkedDeque<Session> queue;
    private final ConcurrentMap<Session, Session> occupied;
    private int size;
    private int inUseSize= 0;
    public int maxSize;
    private final long waitToGetSessionTimeoutInMs;
    private final String ip;
    private final int port;
    private final String user;
    private final String password;
    private final int fetchSize;
    private final ZoneId zoneId;
    private final boolean enableCacheLeader;
    private final int connectionTimeoutInMs;
    private final boolean enableCompression;
    private boolean closed;

    public Integer getSize() {
        return size;
    }

    public Integer getInUseSize() {
        return inUseSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }



    public NewSessionPool(String ip, int port, String user, String password, int maxSize) {
        this(ip, port, user, password, maxSize, 5000, 60000L, false, (ZoneId)null, true, 0);
    }

    public NewSessionPool(String ip, int port, String user, String password, int maxSize, boolean enableCompression) {
        this(ip, port, user, password, maxSize, 5000, 60000L, enableCompression, (ZoneId)null, true, 0);
    }

    public NewSessionPool(String ip, int port, String user, String password, int maxSize, boolean enableCompression, boolean enableCacheLeader) {
        this(ip, port, user, password, maxSize, 5000, 60000L, enableCompression, (ZoneId)null, enableCacheLeader, 0);
    }

    public NewSessionPool(String ip, int port, String user, String password, int maxSize, ZoneId zoneId) {
        this(ip, port, user, password, maxSize, 5000, 60000L, false, zoneId, true, 0);
    }

    public NewSessionPool(String ip, int port, String user, String password, int maxSize, int fetchSize, long waitToGetSessionTimeoutInMs, boolean enableCompression, ZoneId zoneId, boolean enableCacheLeader, int connectionTimeoutInMs) {
        super(ip, port, user, password, fetchSize);
        this.queue = new ConcurrentLinkedDeque();
        this.occupied = new ConcurrentHashMap();
        this.size = 0;
        this.maxSize = 0;
        this.maxSize = maxSize;
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
        this.fetchSize = fetchSize;
        this.waitToGetSessionTimeoutInMs = waitToGetSessionTimeoutInMs;
        this.enableCompression = enableCompression;
        this.zoneId = zoneId;
        this.enableCacheLeader = enableCacheLeader;
        this.connectionTimeoutInMs = connectionTimeoutInMs;
    }

    private Session getSession() throws IoTDBConnectionException {
        Session session = (Session)this.queue.poll();
        if (this.closed) {
            throw new IoTDBConnectionException("Session pool is closed");
        } else if (session != null) {
            this.inUseSize++;
            return session;
        } else {
            boolean shouldCreate = false;
            long start = System.currentTimeMillis();

            while(session == null) {
                synchronized(this) {
                    if (this.size < this.maxSize) {
                        ++this.size;
                        shouldCreate = true;
                        break;
                    }

                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug("no more sessions can be created, wait... queue.size={}", this.queue.size());
                        }

                        this.wait(1000L);
                        long timeOut = Math.min(this.waitToGetSessionTimeoutInMs, 60000L);
                        if (System.currentTimeMillis() - start > timeOut) {
                            logger.warn("the NewSessionPool has wait for {} seconds to get a new connection: {}:{} with {}, {}", new Object[]{(System.currentTimeMillis() - start) / 1000L, this.ip, this.port, this.user, this.password});
                            logger.warn("current occupied size {}, queue size {}, considered size {} ", new Object[]{this.occupied.size(), this.queue.size(), this.size});
                            if (System.currentTimeMillis() - start > this.waitToGetSessionTimeoutInMs) {
                                throw new IoTDBConnectionException(String.format("timeout to get a connection from %s:%s", this.ip, this.port));
                            }
                        }
                    } catch (InterruptedException var12) {
                    }

                    while (this.queue.size()  + this.inUseSize > this.maxSize) {
                        session = (Session)this.queue.poll();
                        try {
                            if (session != null) {
                                session.close();
                            } else {
                                this.wait(1000L);
                            }
                        } catch (IoTDBConnectionException var5) {
                            logger.warn("close the session failed.", var5);
                        } catch (InterruptedException var12) {
                        }
                    }
                    session = (Session)this.queue.poll();
                    if (this.closed) {
                        throw new IoTDBConnectionException("Session pool is closed");
                    }
                }
            }

            if (shouldCreate) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Create a new Session {}, {}, {}, {}", new Object[]{this.ip, this.port, this.user, this.password});
                }

                session = new Session(this.ip, this.port, this.user, this.password, this.fetchSize, this.zoneId, this.enableCacheLeader);

                try {
                    session.open(this.enableCompression, this.connectionTimeoutInMs);
                    synchronized(this) {
                        if (this.closed) {
                            session.close();
                            throw new IoTDBConnectionException("Session pool is closed");
                        }
                    }
                } catch (IoTDBConnectionException var14) {
                    synchronized(this) {
                        --this.size;
                        this.notify();
                        if (logger.isDebugEnabled()) {
                            logger.debug("open session failed, reduce the count and notify others...");
                        }
                    }

                    throw var14;
                }
            }

            return session;
        }
    }

    public int currentAvailableSize() {
        return this.queue.size();
    }

    public int currentOccupiedSize() {
        return this.occupied.size();
    }

    private void putBack(Session session) {
        this.inUseSize--;
        this.queue.push(session);
        synchronized(this) {
            this.notify();
        }
    }

    private void occupy(Session session) {
        this.occupied.put(session, session);
    }

    public synchronized void close() {
        Iterator var1 = this.queue.iterator();

        Session session;
        while(var1.hasNext()) {
            session = (Session)var1.next();

            try {
                session.close();
            } catch (IoTDBConnectionException var5) {
                logger.warn("close the session failed.", var5);
            }
        }

        var1 = this.occupied.keySet().iterator();

        while(var1.hasNext()) {
            session = (Session)var1.next();

            try {
                session.close();
            } catch (IoTDBConnectionException var4) {
                logger.warn("close the session failed.", var4);
            }
        }

        logger.info("closing the session pool, cleaning queues...");
        this.closed = true;
        this.queue.clear();
        this.occupied.clear();
    }

    public void closeResultSet(NewSessionDataSetWrapper wrapper) {
        boolean putback = true;
        boolean var8 = false;

        Session session;
        label85: {
            try {
                var8 = true;
                wrapper.sessionDataSet.closeOperationHandle();
                var8 = false;
                break label85;
            } catch (StatementExecutionException | IoTDBConnectionException var9) {
                this.tryConstructNewSession();
                putback = false;
                var8 = false;
            } finally {
                if (var8) {
                    Session session2 = (Session)this.occupied.remove(wrapper.session);
                    if (putback && session2 != null) {
                        this.putBack(wrapper.session);
                    }

                }
            }

            session = (Session)this.occupied.remove(wrapper.session);
            if (putback && session != null) {
                this.putBack(wrapper.session);
            }

            return;
        }

        session = (Session)this.occupied.remove(wrapper.session);
        if (putback && session != null) {
            this.putBack(wrapper.session);
        }

    }

    private void tryConstructNewSession() {
        Session session = new Session(this.ip, this.port, this.user, this.password, this.fetchSize, this.zoneId, this.enableCacheLeader);

        try {
            session.open(this.enableCompression, this.connectionTimeoutInMs);
            synchronized(this) {
                if (this.closed) {
                    session.close();
                    throw new IoTDBConnectionException("Session pool is closed");
                }

                this.queue.push(session);
                this.notify();
            }
        } catch (IoTDBConnectionException var7) {
            synchronized(this) {
                --this.size;
                this.notify();
                if (logger.isDebugEnabled()) {
                    logger.debug("open session failed, reduce the count and notify others...");
                }
            }
        }

    }

    private void closeSession(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (Exception var3) {
                logger.warn("close the session failed.", var3);
            }
        }

    }

    private void cleanSessionAndMayThrowConnectionException(Session session, int times, IoTDBConnectionException e) throws IoTDBConnectionException {
        this.closeSession(session);
        this.tryConstructNewSession();
        if (times == 2) {
            throw new IoTDBConnectionException(String.format("retry to execute statement on %s:%s failed %d times: %s", this.ip, this.port, 3, e.getMessage()), e);
        }
    }

    public void insertTablet(Tablet tablet) throws IoTDBConnectionException, StatementExecutionException {
        this.insertTablet(tablet, false);
    }

    public void insertTablet(Tablet tablet, boolean sorted) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.insertTablet(tablet, sorted);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var6) {
                logger.warn("insertTablet failed", var6);
                this.cleanSessionAndMayThrowConnectionException(session, i, var6);
                ++i;
            } catch (RuntimeException | StatementExecutionException var7) {
                this.putBack(session);
                throw var7;
            }
        }

    }

    public void insertTablets(Map<String, Tablet> tablets) throws IoTDBConnectionException, StatementExecutionException {
        this.insertTablets(tablets, false);
    }

    public void insertTablets(Map<String, Tablet> tablets, boolean sorted) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.insertTablets(tablets, sorted);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var6) {
                logger.warn("insertTablets failed", var6);
                this.cleanSessionAndMayThrowConnectionException(session, i, var6);
                ++i;
            } catch (RuntimeException | StatementExecutionException var7) {
                this.putBack(session);
                throw var7;
            }
        }

    }

    public void insertRecords(List<String> deviceIds, List<Long> times, List<List<String>> measurementsList, List<List<TSDataType>> typesList, List<List<Object>> valuesList) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.insertRecords(deviceIds, times, measurementsList, typesList, valuesList);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var9) {
                logger.warn("insertRecords failed", var9);
                this.cleanSessionAndMayThrowConnectionException(session, i, var9);
                ++i;
            } catch (RuntimeException | StatementExecutionException var10) {
                this.putBack(session);
                throw var10;
            }
        }

    }

    public void insertOneDeviceRecords(String deviceId, List<Long> times, List<List<String>> measurementsList, List<List<TSDataType>> typesList, List<List<Object>> valuesList) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.insertRecordsOfOneDevice(deviceId, times, measurementsList, typesList, valuesList, false);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var9) {
                logger.warn("insertRecordsOfOneDevice failed", var9);
                this.cleanSessionAndMayThrowConnectionException(session, i, var9);
                ++i;
            } catch (RuntimeException | StatementExecutionException var10) {
                this.putBack(session);
                throw var10;
            }
        }

    }

    public void insertOneDeviceRecords(String deviceId, List<Long> times, List<List<String>> measurementsList, List<List<TSDataType>> typesList, List<List<Object>> valuesList, boolean haveSorted) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.insertRecordsOfOneDevice(deviceId, times, measurementsList, typesList, valuesList, haveSorted);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var10) {
                logger.warn("insertRecordsOfOneDevice failed", var10);
                this.cleanSessionAndMayThrowConnectionException(session, i, var10);
                ++i;
            } catch (RuntimeException | StatementExecutionException var11) {
                this.putBack(session);
                throw var11;
            }
        }

    }

    public void insertRecords(List<String> deviceIds, List<Long> times, List<List<String>> measurementsList, List<List<String>> valuesList) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.insertRecords(deviceIds, times, measurementsList, valuesList);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var8) {
                logger.warn("insertRecords failed", var8);
                this.cleanSessionAndMayThrowConnectionException(session, i, var8);
                ++i;
            } catch (RuntimeException | StatementExecutionException var9) {
                this.putBack(session);
                throw var9;
            }
        }

    }

    public void insertRecord(String deviceId, long time, List<String> measurements, List<TSDataType> types, List<Object> values) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.insertRecord(deviceId, time, measurements, types, values);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var10) {
                logger.warn("insertRecord failed", var10);
                this.cleanSessionAndMayThrowConnectionException(session, i, var10);
                ++i;
            } catch (RuntimeException | StatementExecutionException var11) {
                this.putBack(session);
                throw var11;
            }
        }

    }

    public void insertRecord(String deviceId, long time, List<String> measurements, List<String> values) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.insertRecord(deviceId, time, measurements, values);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var9) {
                logger.warn("insertRecord failed", var9);
                this.cleanSessionAndMayThrowConnectionException(session, i, var9);
                ++i;
            } catch (RuntimeException | StatementExecutionException var10) {
                this.putBack(session);
                throw var10;
            }
        }

    }

    public void testInsertTablet(Tablet tablet) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.testInsertTablet(tablet);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var5) {
                logger.warn("testInsertTablet failed", var5);
                this.cleanSessionAndMayThrowConnectionException(session, i, var5);
                ++i;
            } catch (RuntimeException | StatementExecutionException var6) {
                this.putBack(session);
                throw var6;
            }
        }

    }

    public void testInsertTablets(Map<String, Tablet> tablets) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.testInsertTablets(tablets);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var5) {
                logger.warn("testInsertTablets failed", var5);
                this.cleanSessionAndMayThrowConnectionException(session, i, var5);
                ++i;
            } catch (RuntimeException | StatementExecutionException var6) {
                this.putBack(session);
                throw var6;
            }
        }

    }

    public void testInsertRecords(List<String> deviceIds, List<Long> times, List<List<String>> measurementsList, List<List<String>> valuesList) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.testInsertRecords(deviceIds, times, measurementsList, valuesList);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var8) {
                logger.warn("testInsertRecords failed", var8);
                this.cleanSessionAndMayThrowConnectionException(session, i, var8);
                ++i;
            } catch (RuntimeException | StatementExecutionException var9) {
                this.putBack(session);
                throw var9;
            }
        }

    }

    public void testInsertRecords(List<String> deviceIds, List<Long> times, List<List<String>> measurementsList, List<List<TSDataType>> typesList, List<List<Object>> valuesList) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.testInsertRecords(deviceIds, times, measurementsList, typesList, valuesList);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var9) {
                logger.warn("testInsertRecords failed", var9);
                this.cleanSessionAndMayThrowConnectionException(session, i, var9);
                ++i;
            } catch (RuntimeException | StatementExecutionException var10) {
                this.putBack(session);
                throw var10;
            }
        }

    }

    public void testInsertRecord(String deviceId, long time, List<String> measurements, List<String> values) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.testInsertRecord(deviceId, time, measurements, values);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var9) {
                logger.warn("testInsertRecord failed", var9);
                this.cleanSessionAndMayThrowConnectionException(session, i, var9);
                ++i;
            } catch (RuntimeException | StatementExecutionException var10) {
                this.putBack(session);
                throw var10;
            }
        }

    }

    public void testInsertRecord(String deviceId, long time, List<String> measurements, List<TSDataType> types, List<Object> values) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.testInsertRecord(deviceId, time, measurements, types, values);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var10) {
                logger.warn("testInsertRecord failed", var10);
                this.cleanSessionAndMayThrowConnectionException(session, i, var10);
                ++i;
            } catch (RuntimeException | StatementExecutionException var11) {
                this.putBack(session);
                throw var11;
            }
        }

    }

    public void deleteTimeseries(String path) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.deleteTimeseries(path);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var5) {
                logger.warn("deleteTimeseries failed", var5);
                this.cleanSessionAndMayThrowConnectionException(session, i, var5);
                ++i;
            } catch (RuntimeException | StatementExecutionException var6) {
                this.putBack(session);
                throw var6;
            }
        }

    }

    public void deleteTimeseries(List<String> paths) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.deleteTimeseries(paths);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var5) {
                logger.warn("deleteTimeseries failed", var5);
                this.cleanSessionAndMayThrowConnectionException(session, i, var5);
                ++i;
            } catch (RuntimeException | StatementExecutionException var6) {
                this.putBack(session);
                throw var6;
            }
        }

    }

    public void deleteData(String path, long time) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.deleteData(path, time);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var7) {
                logger.warn("deleteData failed", var7);
                this.cleanSessionAndMayThrowConnectionException(session, i, var7);
                ++i;
            } catch (RuntimeException | StatementExecutionException var8) {
                this.putBack(session);
                throw var8;
            }
        }

    }

    public void deleteData(List<String> paths, long time) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.deleteData(paths, time);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var7) {
                logger.warn("deleteData failed", var7);
                this.cleanSessionAndMayThrowConnectionException(session, i, var7);
                ++i;
            } catch (RuntimeException | StatementExecutionException var8) {
                this.putBack(session);
                throw var8;
            }
        }

    }

    public void deleteData(List<String> paths, long startTime, long endTime) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.deleteData(paths, startTime, endTime);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var9) {
                logger.warn("deleteData failed", var9);
                this.cleanSessionAndMayThrowConnectionException(session, i, var9);
                ++i;
            } catch (RuntimeException | StatementExecutionException var10) {
                this.putBack(session);
                throw var10;
            }
        }

    }

    public void setStorageGroup(String storageGroupId) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.setStorageGroup(storageGroupId);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var5) {
                logger.warn("setStorageGroup failed", var5);
                this.cleanSessionAndMayThrowConnectionException(session, i, var5);
                ++i;
            } catch (RuntimeException | StatementExecutionException var6) {
                this.putBack(session);
                throw var6;
            }
        }

    }

    public void deleteStorageGroup(String storageGroup) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.deleteStorageGroup(storageGroup);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var5) {
                logger.warn("deleteStorageGroup failed", var5);
                this.cleanSessionAndMayThrowConnectionException(session, i, var5);
                ++i;
            } catch (RuntimeException | StatementExecutionException var6) {
                this.putBack(session);
                throw var6;
            }
        }

    }

    public void deleteStorageGroups(List<String> storageGroup) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.deleteStorageGroups(storageGroup);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var5) {
                logger.warn("deleteStorageGroups failed", var5);
                this.cleanSessionAndMayThrowConnectionException(session, i, var5);
                ++i;
            } catch (RuntimeException | StatementExecutionException var6) {
                this.putBack(session);
                throw var6;
            }
        }

    }

    public void createTimeseries(String path, TSDataType dataType, TSEncoding encoding, CompressionType compressor) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.createTimeseries(path, dataType, encoding, compressor);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var8) {
                logger.warn("createTimeseries failed", var8);
                this.cleanSessionAndMayThrowConnectionException(session, i, var8);
                ++i;
            } catch (RuntimeException | StatementExecutionException var9) {
                this.putBack(session);
                throw var9;
            }
        }

    }

    public void createTimeseries(String path, TSDataType dataType, TSEncoding encoding, CompressionType compressor, Map<String, String> props, Map<String, String> tags, Map<String, String> attributes, String measurementAlias) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.createTimeseries(path, dataType, encoding, compressor, props, tags, attributes, measurementAlias);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var12) {
                logger.warn("createTimeseries failed", var12);
                this.cleanSessionAndMayThrowConnectionException(session, i, var12);
                ++i;
            } catch (RuntimeException | StatementExecutionException var13) {
                this.putBack(session);
                throw var13;
            }
        }

    }

    public void createMultiTimeseries(List<String> paths, List<TSDataType> dataTypes, List<TSEncoding> encodings, List<CompressionType> compressors, List<Map<String, String>> propsList, List<Map<String, String>> tagsList, List<Map<String, String>> attributesList, List<String> measurementAliasList) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.createMultiTimeseries(paths, dataTypes, encodings, compressors, propsList, tagsList, attributesList, measurementAliasList);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var12) {
                logger.warn("createMultiTimeseries failed", var12);
                this.cleanSessionAndMayThrowConnectionException(session, i, var12);
                ++i;
            } catch (RuntimeException | StatementExecutionException var13) {
                this.putBack(session);
                throw var13;
            }
        }

    }

    public boolean checkTimeseriesExists(String path) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                boolean resp = session.checkTimeseriesExists(path);
                this.putBack(session);
                return resp;
            } catch (IoTDBConnectionException var5) {
                logger.warn("checkTimeseriesExists failed", var5);
                this.cleanSessionAndMayThrowConnectionException(session, i, var5);
                ++i;
            } catch (RuntimeException | StatementExecutionException var6) {
                this.putBack(session);
                throw var6;
            }
        }

        return false;
    }

    public SessionDataSetWrapper executeQueryStatement(String sql) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                SessionDataSet resp = session.executeQueryStatement(sql);
                SessionDataSetWrapper wrapper = new SessionDataSetWrapper(resp, session, this);
                this.occupy(session);
                return wrapper;
            } catch (IoTDBConnectionException var6) {
                logger.warn("executeQueryStatement failed", var6);
                this.cleanSessionAndMayThrowConnectionException(session, i, var6);
                ++i;
            } catch (RuntimeException | StatementExecutionException var7) {
                this.putBack(session);
                throw var7;
            }
        }

        return null;
    }

    public void executeNonQueryStatement(String sql) throws StatementExecutionException, IoTDBConnectionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                session.executeNonQueryStatement(sql);
                this.putBack(session);
                return;
            } catch (IoTDBConnectionException var5) {
                logger.warn("executeNonQueryStatement failed", var5);
                this.cleanSessionAndMayThrowConnectionException(session, i, var5);
                ++i;
            } catch (RuntimeException | StatementExecutionException var6) {
                this.putBack(session);
                throw var6;
            }
        }

    }

    public SessionDataSetWrapper executeRawDataQuery(List<String> paths, long startTime, long endTime) throws IoTDBConnectionException, StatementExecutionException {
        int i = 0;

        while(i < 3) {
            Session session = this.getSession();

            try {
                SessionDataSet resp = session.executeRawDataQuery(paths, startTime, endTime);
                SessionDataSetWrapper wrapper = new SessionDataSetWrapper(resp, session, this);
                this.occupy(session);
                return wrapper;
            } catch (IoTDBConnectionException var10) {
                logger.warn("executeRawDataQuery failed", var10);
                this.cleanSessionAndMayThrowConnectionException(session, i, var10);
                ++i;
            } catch (RuntimeException | StatementExecutionException var11) {
                this.putBack(session);
                throw var11;
            }
        }

        return null;
    }
}
