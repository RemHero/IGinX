package cn.edu.tsinghua.iginx.integration.expansion.parquet;

import static cn.edu.tsinghua.iginx.integration.expansion.BaseCapacityExpansionIT.DBCE_PARQUET_FS_TEST_DIR;
import static cn.edu.tsinghua.iginx.integration.expansion.constant.Constant.*;

import cn.edu.tsinghua.iginx.integration.expansion.BaseHistoryDataGenerator;
import cn.edu.tsinghua.iginx.thrift.DataType;
import cn.edu.tsinghua.iginx.utils.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParquetHistoryDataGenerator extends BaseHistoryDataGenerator {

  private static final Logger logger = LoggerFactory.getLogger(ParquetHistoryDataGenerator.class);

  private static final char IGINX_SEPARATOR = '.';

  private static final char PARQUET_SEPARATOR = '*';

  public static final String IT_DATA_DIR = "IT_data";

  public static final String IT_DATA_FILENAME = "data.parquet";

  public ParquetHistoryDataGenerator() {}

  private static Connection getConnection() {
    try {
      Class.forName("org.duckdb.DuckDBDriver");
      return DriverManager.getConnection("jdbc:duckdb:");
    } catch (Exception e) {
      return null;
    }
  }

  public void writeHistoryData(
      int port,
      String dir,
      String filename,
      List<String> pathList,
      List<DataType> dataTypeList,
      List<Long> keyList,
      List<List<Object>> valuesList) {
    logger.info("check");
    logger.info("dir: {}, port: {}, filenam: {}, pathlist: {}, KetList: {}, valList:{} ", dir, port, filename, pathList, keyList.size(), valuesList.size());
//    if (!PARQUET_PARAMS.containsKey(port)) {
//      logger.error("writing to unknown port {}.", port);
//      return;
//    }
    logger.info("check");
    Connection conn = getConnection();
    if (conn == null) {
      logger.info("check");
      logger.error("can't get DuckDB connection.");
      return;
    }
    logger.info("check");
    Statement stmt;
    try {
      logger.info("check");
      stmt = conn.createStatement();
      if (stmt == null) {
        logger.info("check");
        logger.error("can't create statement.");
        return;
      }
    } catch (SQLException e) {
      logger.info("check");
      logger.error("statement creation error.");
      return;
    }

    logger.info("check");
    Path dirPath = Paths.get("../" + dir);
    if (Files.notExists(dirPath)) {
      logger.info("check");
      try {
        logger.info("check");
        Files.createDirectories(dirPath);
      } catch (IOException e) {
        logger.error("can't create data file path {}.", dir);
        return;
      }
    }

    logger.info("check");
    // <columnName, dataType>
    List<Pair<String, String>> columnList = new ArrayList<>();
    try {
      logger.info("check");
      int columnCount = pathList.size();
      // table name does not affect query
      String separator = System.getProperty("file.separator");
      String tableName;
      if (dir.endsWith(separator)) {
        logger.info("check");
        tableName = dir.substring(0, dir.lastIndexOf(separator));
        tableName = tableName.substring(tableName.lastIndexOf(separator) + 1);
      } else if (dir.contains(separator)) {
        logger.info("check");
        tableName = dir.substring(dir.lastIndexOf(separator) + 1);
      } else {
        logger.info("check");
        tableName = dir;
      }
      String columnName;
      String dataType;
      logger.info("check");
      for (int i = 0; i < columnCount; i++) {
        logger.info("check");
        columnName = pathList.get(i).replace(IGINX_SEPARATOR, PARQUET_SEPARATOR);
        columnName = columnName.substring(columnName.indexOf(PARQUET_SEPARATOR) + 1);
        dataType = dataTypeList.get(i).toString();

        columnList.add(new Pair<>(columnName, dataType));
      }

      logger.info("check");
      // create table
      StringBuilder typeListStr = new StringBuilder();
      StringBuilder insertStr;
      for (Pair<String, String> p : columnList) {
        logger.info("check");
        typeListStr
            .append("\"")
            .append(p.k)
            .append("\" ")
            .append(toParquetDataType(p.v))
            .append(", ");
      }

      logger.info("check");
      stmt.execute(
          String.format(
              "CREATE TABLE %s (time LONG, %s);",
              tableName, typeListStr.substring(0, typeListStr.length() - 2)));

      logger.info("check");
      // insert value
      insertStr = new StringBuilder();
      boolean hasKeys = !keyList.isEmpty();
      int keyCnt = 0;
      logger.info("check");
      for (int index = 0; index < valuesList.size(); index++) {
        List<Object> values = valuesList.get(index);
        if (hasKeys) {
          keyCnt = Math.toIntExact(keyList.get(index));
        }
        insertStr.append("(").append(keyCnt).append(", ");
        for (int i = 0; i < columnCount; i++) {
          if (dataTypeList.get(i) == DataType.BINARY) {
            insertStr
                .append("'")
                .append(new String((byte[]) values.get(i)))
                .append("'")
                .append(", ");
          } else {
            insertStr.append(values.get(i)).append(", ");
          }
        }
        insertStr = new StringBuilder(insertStr.substring(0, insertStr.length() - 2));
        insertStr.append("), ");
        keyCnt++;
      }

      logger.info("check");
      stmt.execute(
          String.format(
              "INSERT INTO %s VALUES %s;",
              tableName, insertStr.substring(0, insertStr.length() - 2)));

      Path parquetPath = Paths.get("../" + dir, filename);
      stmt.execute(
          String.format(
              "COPY (SELECT * FROM %s) TO '%s' (FORMAT 'parquet');", tableName, parquetPath));

      logger.info("check");
    } catch (SQLException e) {
      logger.error("write history data failed.");
    }
  }

  @Override
  public void writeHistoryData(
      int port,
      List<String> pathList,
      List<DataType> dataTypeList,
      List<Long> keyList,
      List<List<Object>> valuesList) {
    String dir = DBCE_PARQUET_FS_TEST_DIR + System.getProperty("file.separator") + PARQUET_PARAMS.get(port).get(0);
    String filename = PARQUET_PARAMS.get(port).get(1);
    writeHistoryData(port, dir, filename, pathList, dataTypeList, keyList, valuesList);
  }

  @Override
  public void writeHistoryData(
      int port, List<String> pathList, List<DataType> dataTypeList, List<List<Object>> valuesList) {
    writeHistoryData(port, pathList, dataTypeList, new ArrayList<>(), valuesList);
  }

  @Override
  public void clearHistoryDataForGivenPort(int port) {
    if (!PARQUET_PARAMS.containsKey(port)) {
      logger.error("delete from unknown port {}.", port);
      return;
    }

    String dir = DBCE_PARQUET_FS_TEST_DIR + System.getProperty("file.separator") + PARQUET_PARAMS.get(port).get(0);
    String filename = PARQUET_PARAMS.get(port).get(1);
    Path parquetPath = Paths.get("../" + dir, filename);
    File file = new File(parquetPath.toString());

    if (file.exists() && file.isFile()) {
      file.delete();
    } else {
      logger.warn("delete {}/{} error: does not exist or is not a file.", dir, filename);
    }

    // delete the normal IT data
    dir = IT_DATA_DIR + System.getProperty("file.separator");
//    dir = DBCE_PARQUET_FS_TEST_DIR + System.getProperty("file.separator");
    parquetPath = Paths.get("../" + dir);

    try {
      Files.walkFileTree(parquetPath, new DeleteFileVisitor());
    } catch (IOException e) {
      logger.warn("delete {} error: {}.", dir, e.getMessage());
    }
  }

  public static String toParquetDataType(String dataType) {
    switch (dataType) {
      case "BOOLEAN":
        return "BOOLEAN";
      case "INTEGER":
        return "INTEGER";
      case "LONG":
        return "BIGINT";
      case "FLOAT":
        return "FLOAT";
      case "DOUBLE":
        return "DOUBLE";
      case "BINARY":
      default:
        return "VARCHAR";
    }
  }

  static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      Files.delete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      Files.delete(dir);
      return FileVisitResult.CONTINUE;
    }
  }
}
