package com.auth;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mockStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.Test;
import org.mockito.MockedStatic;

public class AuthServiceStaticDbMockTest {

    @Test
    public void loginAndRecoverWithMockedStaticDbConnection() throws Exception {
        // prepare H2 in-memory DB
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:statdb;DB_CLOSE_DELAY=-1");
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS usuarios");
            st.execute("CREATE TABLE usuarios (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255) UNIQUE, clave_hash VARCHAR(255), intentos_fallidos INT DEFAULT 0, bloqueado BOOLEAN DEFAULT FALSE)");
            st.execute("INSERT INTO usuarios(email, clave_hash, intentos_fallidos, bloqueado) VALUES('stat@example.com','Abc!1',0,false)");
        }

        final String url = "jdbc:h2:mem:statdb;DB_CLOSE_DELAY=-1";
        // mock the static method to return a fresh connection per call
        try (MockedStatic<DbConnection> mocked = mockStatic(DbConnection.class)) {
            mocked.when(DbConnection::getConnection).thenAnswer(inv -> DriverManager.getConnection(url));

            AuthService svc = new AuthService();
            String login = svc.login("stat@example.com", "Abc!1");
            assertEquals("Login exitoso 🎉", login);

            String rec = svc.recoverPassword("stat@example.com");
            assertEquals("Se ha enviado un email de recuperación (simulado).", rec);
        } finally {
            conn.close();
        }
    }
}
