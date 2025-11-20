package com.auth;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

public class AuthServiceSQLExceptionTest {

    @Test
    public void testLoginSQLExceptionDuringQuery() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenThrow(new SQLException("boom"));

        AuthService svc = new AuthService();
        String res = svc.loginWithConnection(conn, "u@e.com", "Abc!1");
        assertTrue(res.startsWith("Error de BD:"));
    }

    @Test
    public void testLoginSQLExceptionDuringUpdate() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement psQuery = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(startsWith("SELECT"))).thenReturn(psQuery);
        when(psQuery.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("clave_hash")).thenReturn("Abc!1");
        when(rs.getInt("intentos_fallidos")).thenReturn(0);
        when(rs.getBoolean("bloqueado")).thenReturn(false);

        // For updates, throw SQLException to exercise catch
        when(conn.prepareStatement(startsWith("UPDATE"))).thenThrow(new SQLException("update failed"));

        AuthService svc = new AuthService();
        String res = svc.loginWithConnection(conn, "u@e.com", "Abc!1");
        // Because update threw, the catch around loginWithConnection should return Error de BD
        assertTrue(res.startsWith("Error de BD:"));
    }
}
