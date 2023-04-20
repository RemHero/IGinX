package cn.edu.tsinghua.iginx.filesystem.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfLoader.class);
    private static final String confPath = "src/main/resources/conf/config.properties";
    private static final String ROOT = "root";

    public static String getRootPath() {
        String path = null;
        try {
            InputStream in = new FileInputStream(confPath);
            Properties properties = new Properties();
            properties.load(in);
            path = properties.getProperty(ROOT);
            if (path == null || path.length() == 0) return null;
        } catch (IOException e) {
            logger.error("get conf {} fail!", confPath);
        }
        return path;
    }

    public static File getRootFile() {
        String root = getRootPath();
        return new File(root);
    }
}
