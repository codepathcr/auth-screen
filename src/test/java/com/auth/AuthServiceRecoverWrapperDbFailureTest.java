package com.auth;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AuthServiceRecoverWrapperDbFailureTest {

    @Test
    public void recoverReturnsErrorWhenDbConnectionFails() {
        System.setProperty("DB_DRIVER", "com.nonexistent.Driver");
        System.setProperty("DB_URL", "jdbc:h2:mem:tmp;DB_CLOSE_DELAY=-1");
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "");
        try {
            AuthService svc = new AuthService();
            String res = svc.recoverPassword("noone@example.com");
            assertTrue(res.startsWith("Error de BD:"));
        } finally {
            System.clearProperty("DB_DRIVER");
            System.clearProperty("DB_URL");
            System.clearProperty("DB_USER");
            System.clearProperty("DB_PASSWORD");
        }
    }
}
