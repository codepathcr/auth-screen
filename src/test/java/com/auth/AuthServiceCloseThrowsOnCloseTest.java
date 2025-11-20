package com.auth;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

public class AuthServiceCloseThrowsOnCloseTest {

    @Test
    public void loginReturnsDbErrorWhenPreparedStatementCloseFails() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        // Make the query throw so we deterministically hit the catch
        when(ps.executeQuery()).thenThrow(new SQLException("query fail"));

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String result = svc.loginWithConnection(conn, "u@example.com", "Abcde!");
        assertTrue(result.startsWith("Error de BD:"));
    }

    @Test
    public void recoverReturnsDbErrorWhenPreparedStatementCloseFails() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        doThrow(new SQLException("close fail")).when(ps).close();

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String result = svc.recoverPasswordWithConnection(conn, "u@example.com");
        assertTrue(result.startsWith("Error de BD:"));
    }
}
