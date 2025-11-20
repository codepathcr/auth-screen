package com.auth;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthServiceTest {

    private Connection conn;
    private AuthService service;

    @Before
    public void setUp() throws Exception {
        // In-memory H2 DB
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS usuarios");
            st.execute("CREATE TABLE usuarios (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255) UNIQUE, clave_hash VARCHAR(255), intentos_fallidos INT DEFAULT 0, bloqueado BOOLEAN DEFAULT FALSE)");
        }
        service = new AuthService();
    }

    @After
    public void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    @Test
    public void testLoginInvalidEmail() {
        String res = service.loginWithConnection(conn, "bad-email", "Abc!23");
        assertEquals("Email no válido", res);
    }

    @Test
    public void testLoginInvalidPassword() {
        String res = service.loginWithConnection(conn, "user@example.com", "short");
        assertEquals("Clave inválida: 5-10 chars, 1 mayúscula, 1 carácter especial", res);
    }

    @Test
    public void testLoginUserNotFound() {
        String res = service.loginWithConnection(conn, "user@example.com", "Abc!23");
        assertEquals("Usuario no encontrado", res);
    }

    @Test
    public void testLoginSuccessAndResetAttempts() throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("INSERT INTO usuarios(email, clave_hash, intentos_fallidos, bloqueado) VALUES('u1@example.com','Abc!1',3,false)");
        }
        String res = service.loginWithConnection(conn, "u1@example.com", "Abc!1");
        assertEquals("Login exitoso 🎉", res);

        // verify attempts reset
        try (var rs = conn.createStatement().executeQuery("SELECT intentos_fallidos FROM usuarios WHERE email='u1@example.com'")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt("intentos_fallidos"));
        }
    }

    @Test
    public void testLoginIncorrectAttemptsAndLock() throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("INSERT INTO usuarios(email, clave_hash, intentos_fallidos, bloqueado) VALUES('u2@example.com','Abc!1',0,false)");
        }

        // 1st wrong (use valid-format wrong password)
        String r1 = service.loginWithConnection(conn, "u2@example.com", "Xyz!2");
        assertEquals("Clave incorrecta. Intentos: 1/5", r1);

        // 2nd-4th wrong
        service.loginWithConnection(conn, "u2@example.com", "Xyz!3");
        service.loginWithConnection(conn, "u2@example.com", "Xyz!4");
        service.loginWithConnection(conn, "u2@example.com", "Xyz!5");

        // 5th wrong -> should lock
        String r5 = service.loginWithConnection(conn, "u2@example.com", "Xyz!6");
        assertEquals("Cuenta bloqueada. Excedió los 5 intentos.", r5);

        try (var rs = conn.createStatement().executeQuery("SELECT bloqueado FROM usuarios WHERE email='u2@example.com'")) {
            assertTrue(rs.next());
            assertTrue(rs.getBoolean("bloqueado"));
        }
    }

    @Test
    public void testRecoverPasswordInvalidEmail() {
        String res = service.recoverPasswordWithConnection(conn, "bad-email");
        assertEquals("Ingrese un email válido para recuperar clave", res);
    }

    @Test
    public void testRecoverPasswordUserNotFound() {
        String res = service.recoverPasswordWithConnection(conn, "noone@example.com");
        assertEquals("No existe un usuario con ese email", res);
    }

    @Test
    public void testRecoverPasswordSuccess() throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("INSERT INTO usuarios(email, clave_hash) VALUES('u3@example.com','h')");
        }
        String res = service.recoverPasswordWithConnection(conn, "u3@example.com");
        assertEquals("Se ha enviado un email de recuperación (simulado).", res);
    }
}
