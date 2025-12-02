package com.auth;

import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.properties.SystemProperties;
import uk.org.webcompere.systemstubs.rules.EnvironmentVariablesRule;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Tests to achieve 100% coverage of DbConnection class.
 * Covers the missing branches in getEnvOrRequired method using System Stubs
 * to properly mock environment variables.
 */
public class DbConnectionMissingCoverageTest {

    @Rule
    public EnvironmentVariablesRule environmentVariables = new EnvironmentVariablesRule();

    /**
     * Test that when DB_URL environment variable is null/missing, 
     * getConnection throws SQLException with appropriate message.
     * This covers the missing branch: if (value == null || value.trim().isEmpty())
     */
    @Test
    public void getConnection_missingDbUrl_throwsSQLException() throws Exception {
        // Clear all environment variables and system properties
        environmentVariables.set("DB_URL", null);
        environmentVariables.set("DB_USER", "testuser");
        environmentVariables.set("DB_PASSWORD", "testpass");
        
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        
        try {
            DbConnection.getConnection();
            fail("Expected SQLException for missing DB_URL");
        } catch (SQLException e) {
            assertTrue("Exception should mention missing environment variable",
                      e.getMessage().contains("Required environment variable DB_URL"));
        }
    }

    /**
     * Test that when DB_USER environment variable is null/missing,
     * getConnection throws SQLException.
     */
    @Test
    public void getConnection_missingDbUser_throwsSQLException() throws Exception {
        environmentVariables.set("DB_URL", "jdbc:h2:mem:test");
        environmentVariables.set("DB_USER", null);
        environmentVariables.set("DB_PASSWORD", "testpass");
        
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        
        try {
            DbConnection.getConnection();
            fail("Expected SQLException for missing DB_USER");
        } catch (SQLException e) {
            assertTrue("Exception should mention missing environment variable",
                      e.getMessage().contains("Required environment variable DB_USER"));
        }
    }

    /**
     * Test that when DB_PASSWORD environment variable is null/missing,
     * getConnection throws SQLException.
     */
    @Test
    public void getConnection_missingDbPassword_throwsSQLException() throws Exception {
        environmentVariables.set("DB_URL", "jdbc:h2:mem:test");
        environmentVariables.set("DB_USER", "testuser");
        environmentVariables.set("DB_PASSWORD", null);
        
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        
        try {
            DbConnection.getConnection();
            fail("Expected SQLException for missing DB_PASSWORD");
        } catch (SQLException e) {
            assertTrue("Exception should mention missing environment variable",
                      e.getMessage().contains("Required environment variable DB_PASSWORD"));
        }
    }

    /**
     * Test that when DB_URL environment variable is empty string,
     * getConnection throws SQLException.
     */
    @Test
    public void getConnection_emptyDbUrl_throwsSQLException() throws Exception {
        environmentVariables.set("DB_URL", "");
        environmentVariables.set("DB_USER", "testuser");
        environmentVariables.set("DB_PASSWORD", "testpass");
        
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        
        try {
            DbConnection.getConnection();
            fail("Expected SQLException for empty DB_URL");
        } catch (SQLException e) {
            assertTrue("Exception should mention missing environment variable",
                      e.getMessage().contains("Required environment variable DB_URL"));
        }
    }

    /**
     * Test that when DB_URL environment variable is whitespace only,
     * getConnection throws SQLException.
     */
    @Test
    public void getConnection_whitespaceDbUrl_throwsSQLException() throws Exception {
        environmentVariables.set("DB_URL", "   ");
        environmentVariables.set("DB_USER", "testuser");
        environmentVariables.set("DB_PASSWORD", "testpass");
        
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        
        try {
            DbConnection.getConnection();
            fail("Expected SQLException for whitespace DB_URL");
        } catch (SQLException e) {
            assertTrue("Exception should mention missing environment variable",
                      e.getMessage().contains("Required environment variable DB_URL"));
        }
    }

    /**
     * Test that system property takes precedence over environment variable,
     * even when system property is empty string.
     */
    @Test
    public void getConnection_emptySystemPropertyDbUrl_usesEmptyValue() throws Exception {
        // Set env var to valid value
        environmentVariables.set("DB_URL", "jdbc:h2:mem:test");
        environmentVariables.set("DB_USER", "testuser");
        environmentVariables.set("DB_PASSWORD", "testpass");
        
        // Override with empty system property
        System.setProperty("DB_URL", "");
        System.setProperty("DB_USER", "testuser");
        System.setProperty("DB_PASSWORD", "testpass");
        
        try {
            DbConnection.getConnection();
            fail("Expected SQLException due to invalid empty DB_URL from system property");
        } catch (SQLException e) {
            // The empty DB_URL will be passed to DriverManager which will fail
            // This verifies system property takes precedence
            assertFalse("Exception should NOT be about missing env var",
                       e.getMessage().contains("Required environment variable"));
        } finally {
            System.clearProperty("DB_URL");
            System.clearProperty("DB_USER");
            System.clearProperty("DB_PASSWORD");
        }
    }
}
