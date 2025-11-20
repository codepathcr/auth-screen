package com.auth;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;

public class AuthServicePrimaryAndCloseDifferentExceptionTest {

    @Test
    public void primaryAndCloseDifferentExceptionsAreChained() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        // executeQuery throws primary exception
        when(ps.executeQuery()).thenThrow(new SQLException("primary-exec"));
        // closing the prepared statement throws a different exception
        doThrow(new SQLException("close-ex"))
                .when(ps).close();

        AuthService svc = new AuthService();
        String res = svc.loginWithConnection(conn, "u@example.com", "Secret1!");
        assertTrue(res.contains("primary-exec"));
    }
}
