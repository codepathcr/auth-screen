package com.auth;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Test;

public class DbConnectionEnvVarTest {

    @Test
    public void getEnvOrDefaultUsesEnvironmentWhenNoSystemProp() throws Exception {
        Method m = DbConnection.class.getDeclaredMethod("getEnvOrDefault", String.class, String.class);
        m.setAccessible(true);

        // Case 1: When a System property is set to a non-empty value, it should be returned.
        System.setProperty("TEST_ENV_OVERRIDE_1", "sysval");
        String r1 = (String) m.invoke(null, "TEST_ENV_OVERRIDE_1", "fallback");
        assertEquals("sysval", r1);
        System.clearProperty("TEST_ENV_OVERRIDE_1");

        // Case 2: When System property is empty but a real env var exists (like PATH), the env var must be returned.
        System.setProperty("TEST_ENV_OVERRIDE_1", "");
        String pathVal = System.getenv("PATH");
        if (pathVal != null && !pathVal.trim().isEmpty()) {
            String r2 = (String) m.invoke(null, "PATH", "fallback");
            assertEquals(pathVal, r2);
        }
        System.clearProperty("TEST_ENV_OVERRIDE_1");

        // Case 3: When neither System property nor environment variable exists/non-empty, fallback is returned.
        String r3 = (String) m.invoke(null, "SOME_RARE_ENV_NAME_99999", "fallback");
        assertEquals("fallback", r3);
    }
}
