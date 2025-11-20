package com.auth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DbConnectionUsingValueTests {

    @Test
    public void prefersSystemPropertyWhenSet() {
        System.setProperty("DB_FOO", "sysval");
        try {
            String res = DbConnection.getEnvOrDefaultUsingValue("DB_FOO", "fallback", "envval");
            assertEquals("sysval", res);
        } finally {
            System.clearProperty("DB_FOO");
        }
    }

    @Test
    public void returnsEnvValueWhenNoSystemProperty() {
        System.clearProperty("DB_FOO");
        String res = DbConnection.getEnvOrDefaultUsingValue("DB_FOO", "fallback", "envval");
        assertEquals("envval", res);
    }
}
