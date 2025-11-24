package com.auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    public static Connection getConnection() throws SQLException {
        // Read DB settings from environment variables (required for security)
        // Use System properties as override mechanism (useful for tests)
        String dbUrl = getEnvOrRequired("DB_URL");
        String dbUser = getEnvOrRequired("DB_USER");
        String dbPassword = getEnvOrRequired("DB_PASSWORD");
        String driverClass = getEnvOrDefault("DB_DRIVER", "org.postgresql.Driver");
        
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new SQLException(driverClass + " JDBC driver not found on classpath. Make sure the driver jar is available.", e);
        }

        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    // Get required environment variable, throw exception if not set
    private static String getEnvOrRequired(String name) throws SQLException {
        String sys = System.getProperty(name);
        if (sys != null) {
            // System property is set (even if empty), use it - important for tests
            return sys;
        }
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new SQLException("Required environment variable " + name + " is not set. Please configure database settings.");
        }
        return value;
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
