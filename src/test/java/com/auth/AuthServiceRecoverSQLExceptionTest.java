package com.auth;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Test;

public class AuthServiceRecoverSQLExceptionTest {

    @Test
    public void recoverPasswordQueryThrowsSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException("query fail"));

        AuthService svc = new AuthService();
        String res = svc.recoverPasswordWithConnection(conn, "u@e.com");
        assertTrue(res.startsWith("Error de BD:"));
    }
}
