package com.edutrack.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection: Utility for managing MySQL JDBC database connections.
 * Reads configuration from db.properties with automatic fallback support.
 */
public class DatabaseConnection {
    private static String url = "jdbc:mysql://localhost:3306/edutrack_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static String user = "KU";
    private static String password = "25r11a05ku";
    private static String fallbackUser = "root";
    private static String fallbackPassword = "";
    private static boolean driverLoaded = false;

    static {
        loadConfiguration();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException ex) {
                driverLoaded = false;
                System.err.println("[DATABASE WARNING] MySQL JDBC Driver not found in classpath. Ensure lib/mysql-connector-j.jar is included.");
            }
        }
    }

    private static void loadConfiguration() {
        File[] candidateFiles = new File[]{
                new File("db.properties"),
                new File("data/db.properties"),
                new File("../db.properties")
        };

        for (File f : candidateFiles) {
            if (f.exists() && f.isFile()) {
                try (InputStream in = new FileInputStream(f)) {
                    Properties props = new Properties();
                    props.load(in);
                    if (props.getProperty("db.url") != null) url = props.getProperty("db.url").trim();
                    if (props.getProperty("db.user") != null) user = props.getProperty("db.user").trim();
                    if (props.getProperty("db.password") != null) password = props.getProperty("db.password").trim();
                    if (props.getProperty("db.fallback.user") != null) fallbackUser = props.getProperty("db.fallback.user").trim();
                    if (props.getProperty("db.fallback.password") != null) fallbackPassword = props.getProperty("db.fallback.password").trim();
                    break;
                } catch (Exception e) {
                    System.err.println("Notice: Could not read " + f.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public static boolean isDriverLoaded() {
        return driverLoaded;
    }

    /**
     * Gets a live database Connection, trying configured credentials first
     * and fallback credentials if necessary.
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("MySQL JDBC driver is not loaded.");
        }
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            // Try fallback credentials if primary fails
            if (fallbackUser != null && !fallbackUser.equals(user)) {
                try {
                    return DriverManager.getConnection(url, fallbackUser, fallbackPassword);
                } catch (SQLException ignored) {}
            }
            throw e;
        }
    }

    /**
     * Tests if the database is reachable.
     */
    public static boolean testConnection() {
        if (!driverLoaded) return false;
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getDatabaseName() {
        try {
            int idx = url.indexOf("3306/");
            if (idx != -1) {
                int q = url.indexOf("?", idx);
                return q != -1 ? url.substring(idx + 5, q) : url.substring(idx + 5);
            }
        } catch (Exception ignored) {}
        return "edutrack_db";
    }

    public static String getConnectedUser() {
        return user;
    }
}
