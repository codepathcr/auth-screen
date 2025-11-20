package com.auth;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthServiceBlockedUserTest {

    private Connection conn;
    private AuthService svc;

    @Before
    public void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:blockeddb;DB_CLOSE_DELAY=-1");
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS usuarios");
            st.execute("CREATE TABLE usuarios (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255) UNIQUE, clave_hash VARCHAR(255), intentos_fallidos INT DEFAULT 0, bloqueado BOOLEAN DEFAULT FALSE)");
            st.execute("INSERT INTO usuarios(email, clave_hash, intentos_fallidos, bloqueado) VALUES('blocked@example.com','Abc!1',5,true)");
        }
        svc = new AuthService();
    }

    @After
    public void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    @Test
    public void blockedUserReturnsAccountLockedMessage() {
        String res = svc.loginWithConnection(conn, "blocked@example.com", "Abc!1");
        assertEquals("Cuenta bloqueada por intentos fallidos", res);
    }
}
