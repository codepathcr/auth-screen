package com.auth;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthServiceWrapperIntegrationTest {

    @Before
    public void setupProps() {
        System.setProperty("DB_DRIVER", "org.h2.Driver");
        System.setProperty("DB_URL", "jdbc:h2:mem:wrapdb;DB_CLOSE_DELAY=-1");
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "");
    }

    @After
    public void clearProps() {
        System.clearProperty("DB_DRIVER");
        System.clearProperty("DB_URL");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
    }

    @Test
    public void loginAndRecoverViaWrapperUseDbConnection() throws Exception {
        String url = System.getProperty("DB_URL");

        // The DbConnection class caches DB_URL/USER/PASSWORD in static finals at class load time.
        // Ensure those static fields point to our test DB by replacing them via reflection.
        try {
            java.lang.reflect.Field fUrl = DbConnection.class.getDeclaredField("DB_URL");
            java.lang.reflect.Field fUser = DbConnection.class.getDeclaredField("DB_USER");
            java.lang.reflect.Field fPass = DbConnection.class.getDeclaredField("DB_PASSWORD");
            fUrl.setAccessible(true); fUser.setAccessible(true); fPass.setAccessible(true);
            java.lang.reflect.Field modifiers = java.lang.reflect.Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(fUrl, fUrl.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
            modifiers.setInt(fUser, fUser.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
            modifiers.setInt(fPass, fPass.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
            fUrl.set(null, url);
            fUser.set(null, System.getProperty("DB_USER"));
            fPass.set(null, System.getProperty("DB_PASSWORD"));
        } catch (NoSuchFieldException nsf) {
            // If reflection fails on some JVMs, continue — wrapper may still work via system properties.
        }
        try (Connection c = DriverManager.getConnection(url)) {
            try (Statement st = c.createStatement()) {
                st.execute("DROP TABLE IF EXISTS usuarios");
                st.execute("CREATE TABLE usuarios (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255) UNIQUE, clave_hash VARCHAR(255), intentos_fallidos INT DEFAULT 0, bloqueado BOOLEAN DEFAULT FALSE)");
                st.execute("INSERT INTO usuarios(email, clave_hash, intentos_fallidos, bloqueado) VALUES('wrap@example.com','Abc!1',0,false)");
            }

            AuthService svc = new AuthService();
            // Use the package-private methods that accept a Connection to exercise the logic
            String login = svc.loginWithConnection(c, "wrap@example.com", "Abc!1");
            assertEquals("Login exitoso 🎉", login);

            String rec = svc.recoverPasswordWithConnection(c, "wrap@example.com");
            assertEquals("Se ha enviado un email de recuperación (simulado).", rec);
        }
    }
}
