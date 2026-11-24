package com.edutrack.data;

/**
 * DataStoreFactory: Factory Design Pattern implementation that chooses
 * the optimal persistence implementation (MySQL Relational Database vs In-Memory/CSV fallback).
 */
public class DataStoreFactory {
    private static boolean usingMySql = false;

    public static DataStore createDataStore(String dataDir) {
        if (DatabaseConnection.testConnection()) {
            try {
                MySqlDataStore sqlStore = new MySqlDataStore();
                usingMySql = true;
                System.out.println("==========================================================");
                System.out.println("🗄️  [DATABASE] Connected to MySQL Server at localhost:3306");
                System.out.println("👉 Database: " + DatabaseConnection.getDatabaseName() + " | User: " + DatabaseConnection.getConnectedUser());
                System.out.println("👉 Live SQL mode is ACTIVE!");
                System.out.println("==========================================================");
                return sqlStore;
            } catch (Exception e) {
                System.err.println("[DATABASE WARNING] Could not initialize MySqlDataStore: " + e.getMessage());
            }
        } else {
            System.out.println("[DATABASE NOTICE] MySQL service not reachable or driver missing.");
            System.out.println("👉 Gracefully falling back to InMemoryDataStore with CSV file sync.");
        }

        usingMySql = false;
        return new InMemoryDataStore(dataDir);
    }

    public static boolean isUsingMySql() {
        return usingMySql;
    }
}
