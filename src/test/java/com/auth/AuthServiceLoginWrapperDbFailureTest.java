package com.auth;

import static org.junit.Assert.*;

import org.junit.Test;

public class AuthServiceLoginWrapperDbFailureTest {

    @Test
    public void loginReturnsErrorWhenDbConnectionFails() {
        // Force DbConnection to use a missing driver
        System.setProperty("DB_DRIVER", "com.nonexistent.Driver");

        try {
            AuthService svc = new AuthService();
            String res = svc.login("a@b.com", "Abc!1");
            assertTrue(res.startsWith("Error de BD:"));
        } finally {
            System.clearProperty("DB_DRIVER");
        }
    }
}
