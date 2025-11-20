package com.auth;

import static org.junit.Assert.*;

import java.sql.Connection;

import org.junit.Test;

public class DbConnectionTest {

    @Test
    public void getEnvOrDefaultViaSystemProperty() {
        System.setProperty("DB_URL", "jdbc:h2:mem:propdb;DB_CLOSE_DELAY=-1");
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "");
        System.setProperty("DB_DRIVER", "org.h2.Driver");

        try (Connection c = DbConnection.getConnection()) {
            assertNotNull(c);
            assertFalse(c.isClosed());
        } catch (Exception ex) {
            fail("Expected connection with H2 driver: " + ex.getMessage());
        } finally {
            System.clearProperty("DB_URL");
            System.clearProperty("DB_USER");
            System.clearProperty("DB_PASSWORD");
            System.clearProperty("DB_DRIVER");
        }
    }
}
