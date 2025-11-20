package com.auth;

import static org.junit.Assert.*;

import org.junit.Test;

public class DbConnectionFailureTest {

    @Test
    public void testInvalidDriverThrows() {
        System.setProperty("DB_DRIVER", "com.nonexistent.Driver");
        System.setProperty("DB_URL", "jdbc:h2:mem:tmp;DB_CLOSE_DELAY=-1");
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "");
        try {
            DbConnection.getConnection();
            fail("Expected SQLException due to missing driver class");
        } catch (Exception ex) {
            assertTrue(ex instanceof java.sql.SQLException);
        } finally {
            System.clearProperty("DB_DRIVER");
            System.clearProperty("DB_URL");
            System.clearProperty("DB_USER");
            System.clearProperty("DB_PASSWORD");
        }
    }
}
