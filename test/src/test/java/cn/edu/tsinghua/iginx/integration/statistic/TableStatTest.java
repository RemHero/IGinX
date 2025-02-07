package cn.edu.tsinghua.iginx.integration.statistic;

import static cn.edu.tsinghua.iginx.integration.controller.Controller.SUPPORT_KEY;
import static cn.edu.tsinghua.iginx.integration.controller.Controller.clearAllData;
import static org.junit.Assert.fail;

import cn.edu.tsinghua.iginx.exception.SessionException;
import cn.edu.tsinghua.iginx.integration.controller.Controller;
import cn.edu.tsinghua.iginx.integration.func.session.InsertAPIType;
import cn.edu.tsinghua.iginx.integration.tool.ConfLoader;
import cn.edu.tsinghua.iginx.integration.tool.DBConf;
import cn.edu.tsinghua.iginx.integration.tool.MultiConnection;
import cn.edu.tsinghua.iginx.integration.tool.SQLExecutor;
import cn.edu.tsinghua.iginx.pool.IginxInfo;
import cn.edu.tsinghua.iginx.pool.SessionPool;
import cn.edu.tsinghua.iginx.session.Session;
import cn.edu.tsinghua.iginx.statistics.handler.StatsHandler;
import cn.edu.tsinghua.iginx.statistics.handler.TableStatHandler;
import cn.edu.tsinghua.iginx.thrift.DataType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableStatTest {
  protected static SQLExecutor executor;

  protected static boolean isForSession = true, isForSessionPool = false;
  protected static int MaxMultiThreadTaskNum = -1;

  // host info
  protected static String defaultTestHost = "127.0.0.1";
  protected static int defaultTestPort = 6888;
  protected static String defaultTestUser = "root";
  protected static String defaultTestPass = "root";
  protected static String runningEngine;

  protected static final Logger LOGGER = LoggerFactory.getLogger(TableStatTest.class);

  protected static final boolean isOnWin =
      System.getProperty("os.name").toLowerCase().contains("win");

  protected boolean isAbleToDelete;

  protected boolean isSupportChinesePath;

  protected boolean isSupportNumericalPath;

  protected boolean isSupportSpecialCharacterPath;

  protected boolean isAbleToShowColumns;

  protected boolean isScaling = false;

  private final long startKey = 0L;

  private final long endKey = 150L;

  private boolean isFilterPushDown;

  protected boolean isAbleToClearData = true;
  private static final int CONCURRENT_NUM = 5;

  private static MultiConnection session;

  private static boolean dummyNoData = true;

  protected static boolean needCompareResult = true;

  public TableStatTest() {
    ConfLoader conf = new ConfLoader(Controller.CONFIG_FILE);
    runningEngine = conf.getStorageType();
    DBConf dbConf = conf.loadDBConf(runningEngine);
    this.isScaling = conf.isScaling();
    if (!SUPPORT_KEY.get(conf.getStorageType()) && this.isScaling) {
      needCompareResult = false;
      executor.setNeedCompareResult(needCompareResult);
    }
    this.isAbleToClearData = dbConf.getEnumValue(DBConf.DBConfType.isAbleToClearData);
    this.isAbleToDelete = dbConf.getEnumValue(DBConf.DBConfType.isAbleToDelete);
    this.isAbleToShowColumns = dbConf.getEnumValue(DBConf.DBConfType.isAbleToShowColumns);
    this.isSupportChinesePath = dbConf.getEnumValue(DBConf.DBConfType.isSupportChinesePath);
    this.isSupportNumericalPath = dbConf.getEnumValue(DBConf.DBConfType.isSupportNumericalPath);
    this.isSupportSpecialCharacterPath =
        dbConf.getEnumValue(DBConf.DBConfType.isSupportSpecialCharacterPath);

    String rules = executor.execute("SHOW RULES;");
    this.isFilterPushDown = rules.contains("FilterPushDownRule|    ON|");
  }

  @BeforeClass
  public static void setUp() throws SessionException {
    dummyNoData = true;
    if (isForSession) {
      session =
          new MultiConnection(
              new Session(defaultTestHost, defaultTestPort, defaultTestUser, defaultTestPass));
    } else if (isForSessionPool) {
      session =
          new MultiConnection(
              new SessionPool(
                  new ArrayList<IginxInfo>() {
                    {
                      add(
                          new IginxInfo.Builder()
                              .host("0.0.0.0")
                              .port(6888)
                              .user("root")
                              .password("root")
                              .build());
                      add(
                          new IginxInfo.Builder()
                              .host("0.0.0.0")
                              .port(7888)
                              .user("root")
                              .password("root")
                              .build());
                    }
                  }));
    } else {
      LOGGER.error("isForSession=false, isForSessionPool=false");
      fail();
      return;
    }
    executor = new SQLExecutor(session);
    executor.open();
  }

  @AfterClass
  public static void tearDown() throws SessionException {
//    clearAllData(session);
    executor.close();
  }

  @Before
  public void insertData() {
    generateData(startKey, endKey);
    Controller.after(session);
  }

  private void generateData(long start, long end) {
    // construct insert statement
    List<String> pathList =
        new ArrayList<String>() {
          {
            add("us1.s1");
            add("us2.s1");
            add("us1.s2");
            add("us2.s2");
          }
        };
    List<DataType> dataTypeList =
        new ArrayList<DataType>() {
          {
            add(DataType.LONG);
            add(DataType.LONG);
            add(DataType.BINARY);
            add(DataType.DOUBLE);
          }
        };

    List<Long> keyList = new ArrayList<>();
    List<List<Object>> valuesList = new ArrayList<>();
    int size = (int) (end - start);
    for (int i = 0; i < size; i++) {
      keyList.add(start + i);
      valuesList.add(
          Arrays.asList(
              (long) i,
              (long) i + 1,
              ("\"" + RandomStringUtils.randomAlphanumeric(10) + "\"").getBytes(),
              (i + 0.1d)));
    }

    Controller.writeRowsData(
        session,
        pathList,
        keyList,
        dataTypeList,
        valuesList,
        new ArrayList<>(),
        InsertAPIType.Row,
        dummyNoData);
    dummyNoData = false;
  }

  private String generateDefaultInsertStatementByTimeRange(long start, long end) {
    String insertStrPrefix = "INSERT INTO us.d1 (key, s1, s2, s3, s4) values ";

    StringBuilder builder = new StringBuilder(insertStrPrefix);

    int size = (int) (end - start);
    for (int i = 0; i < size; i++) {
      builder.append(", ");
      builder.append("(");
      builder.append(start + i).append(", ");
      builder.append(i).append(", ");
      builder.append(i + 1).append(", ");
      builder
          .append("\"")
          .append(new String(RandomStringUtils.randomAlphanumeric(10).getBytes()))
          .append("\", ");
      builder.append((i + 0.1));
      builder.append(")");
    }
    builder.append(";");

    return builder.toString();
  }

  private int getTimeCostFromExplainPhysicalResult(String explainPhysicalResult) {
    String[] lines = explainPhysicalResult.split("\n");
    int executeTimeIndex = -1;
    int timeCost = 0;
    for (String line : lines) {
      String[] split = line.split("\\|");
      if (split.length > 1) {
        if (executeTimeIndex == -1) {
          for (int i = 0; i < split.length; i++) {
            if (split[i].trim().contains("Time")) {
              executeTimeIndex = i;
              break;
            }
          }
        } else {
          timeCost += Integer.parseInt(split[executeTimeIndex].trim().replace("ms", ""));
        }
      }
    }
    return timeCost;
  }

  @After
  public void clearData() {
//    String clearData = "CLEAR DATA;";
//    executor.execute(clearData);
  }

  @Test
  public void showTableStat() {
    TableStatHandler tableStatHandler = new TableStatHandler();
    tableStatHandler.update("us.d1.s1");
    System.out.println(tableStatHandler.getStatsTable("us.d1.s1").toString());
  }

  @Test
  public void showTreeStat() {
    TableStatHandler tableStatHandler = new TableStatHandler();
    tableStatHandler.update("us.d1.s1");
    tableStatHandler.update("us.d1.s2");
    StatsHandler statsHandler = new StatsHandler();

//    statsHandler.recursiveDeriveStats();



  }
}
