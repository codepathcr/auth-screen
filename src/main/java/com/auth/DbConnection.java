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
        // Allow overriding the JDBC driver class via system property or env var (DB_DRIVER)
        String driverClass = getEnvOrDefault("DB_DRIVER", "org.postgresql.Driver");
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new SQLException(driverClass + " JDBC driver not found on classpath. Make sure the driver jar is available.", e);
        }

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Helper that treats null or empty env values as unset and returns the fallback.
    // First check System properties (useful for tests), then environment variables.
    private static String getEnvOrDefault(String name, String fallback) {
        // Delegate to the testable helper to avoid duplicating the
        // null/empty checks and to make these branches reachable from unit tests.
        return getEnvOrDefaultUsingValue(name, fallback, System.getenv(name));
    }

    // Package-private helper used by tests to simulate environment values without
    // attempting to modify the real process environment.
    static String getEnvOrDefaultUsingValue(String name, String fallback, String envValue) {
        String sys = System.getProperty(name);
        if (sys != null && !sys.trim().isEmpty()) {
            return sys;
        }
        String value = envValue;
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }
}
