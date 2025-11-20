package com.auth;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

public class DbConnectionPrivateMethodTest {

    @Test
    public void testGetEnvOrDefaultVarieties() throws Exception {
        Method m = DbConnection.class.getDeclaredMethod("getEnvOrDefault", String.class, String.class);
        m.setAccessible(true);

        // When system property set -> returns it
        System.setProperty("X_TEST_PROP", "value1");
        String r1 = (String) m.invoke(null, "X_TEST_PROP", "fallback");
        assertEquals("value1", r1);
        System.clearProperty("X_TEST_PROP");

        // When system property is empty -> should return fallback
        System.setProperty("X_TEST_PROP", "   ");
        String r2 = (String) m.invoke(null, "X_TEST_PROP", "fallback");
        assertEquals("fallback", r2);
        System.clearProperty("X_TEST_PROP");

        // When property not set -> rely on env (usually absent in test), expect fallback
        String r3 = (String) m.invoke(null, "THIS_SHOULD_NOT_EXIST_123", "fb");
        assertEquals("fb", r3);
    }
}
