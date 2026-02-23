package com.socialmedia.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is =
                     DatabaseConfig.class
                             .getClassLoader()
                             .getResourceAsStream("application.properties")) {

            if (is == null) {
                throw new RuntimeException("application.properties not found");
            }
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DB config", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password")
            );
        } catch (Exception e) {
            throw new RuntimeException("DB connection failed", e);
        }
    }
}