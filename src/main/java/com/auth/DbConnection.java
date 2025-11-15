package com.auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    // Allow overriding DB settings via environment variables for safer configuration.
    // Treat empty environment variable values as "not set" so defaults are used.
    private static final String DB_URL = getEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/pswe06");
    private static final String DB_USER = getEnvOrDefault("DB_USER", "postgres");
    private static final String DB_PASSWORD = getEnvOrDefault("DB_PASSWORD", "test");

    public static Connection getConnection() throws SQLException {
        // Ensure the PostgreSQL driver class is loaded so DriverManager can find it
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found on classpath. Make sure the driver jar is available (e.g. put it in lib/ and run with lib/*).", e);
        }

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Helper that treats null or empty env values as unset and returns the fallback.
    private static String getEnvOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }
}
