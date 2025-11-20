package com.auth;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class AuthServiceCloseSameExceptionTest {

    @Test
    public void closeSameExceptionThrown() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("clave_hash")).thenReturn("Secret1!");
        when(rs.getInt("intentos_fallidos")).thenReturn(0);
        when(rs.getBoolean("bloqueado")).thenReturn(false);

        SQLException e = new SQLException("both close");
        doThrow(e).when(rs).close();
        doThrow(e).when(ps).close();

        AuthService svc = new AuthService();
        String res = svc.loginWithConnection(conn, "u@example.com", "Secret1!");
        org.junit.Assert.assertTrue(res.contains("both close"));
    }
}
