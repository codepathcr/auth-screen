package com.auth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DbConnectionEnvEmptyTest {
    @Test
    public void emptyEnvReturnsFallback() {
        String res = DbConnection.getEnvOrDefaultUsingValue("DB_FOO", "fallback", "");
        assertEquals("fallback", res);
    }
}
