package com.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;

import org.junit.Test;

public class DbConnectionCtorAndEnvEdgeTest {

    @Test
    public void canInstantiateDbConnectionConstructor() {
        // ensure the default constructor is executed to cover synthetic constructor
        DbConnection c = new DbConnection();
        assertNotNull(c);
    }

    @Test
    public void getEnvOrDefaultTreatsEmptySystemPropertyAsUnset() throws Exception {
        Method m = DbConnection.class.getDeclaredMethod("getEnvOrDefault", String.class, String.class);
        m.setAccessible(true);

        System.setProperty("EMPTY_SYS_TEST_42", "   ");
        String r = (String) m.invoke(null, "EMPTY_SYS_TEST_42", "fallback42");
        assertEquals("fallback42", r);
        System.clearProperty("EMPTY_SYS_TEST_42");
    }
}
