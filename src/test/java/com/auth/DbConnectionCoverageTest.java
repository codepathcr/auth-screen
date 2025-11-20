package com.auth;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DbConnectionCoverageTest {

    @After
    public void tearDown() {
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        System.clearProperty("DB_DRIVER");
    }

    @Test
    public void getEnvOrDefaultUsingValue_emptyEnv_returnsFallback() {
        System.clearProperty("DB_URL");
        String res = DbConnection.getEnvOrDefaultUsingValue("DB_URL", "fallback", "   ");
        assertEquals("fallback", res);
    }

    @Test
    public void getEnvOrDefaultUsingValue_systemPropertyWins() {
        System.setProperty("DB_URL", "fromProp");
        try {
            String res = DbConnection.getEnvOrDefaultUsingValue("DB_URL", "fallback", "envVal");
            assertEquals("fromProp", res);
        } finally {
            System.clearProperty("DB_URL");
        }
    }

    @Test
    public void getEnvOrDefaultUsingValue_envValueReturnedWhenNoSysProp() {
        System.clearProperty("DB_URL");
        String res = DbConnection.getEnvOrDefaultUsingValue("DB_URL", "fallback", "envVal");
        assertEquals("envVal", res);
    }

    @Test
    public void getEnvOrDefaultUsingValue_nullEnv_returnsFallback() {
        System.clearProperty("DB_URL");
        String res = DbConnection.getEnvOrDefaultUsingValue("DB_URL", "fallback", null);
        assertEquals("fallback", res);
    }

    @Test
    public void getEnvOrDefaultUsingValue_sysPropertyEmpty_usesEnv() {
        System.setProperty("DB_URL", "   ");
        try {
            String res = DbConnection.getEnvOrDefaultUsingValue("DB_URL", "fallback", "envVal");
            assertEquals("envVal", res);
        } finally {
            System.clearProperty("DB_URL");
        }
    }
}
