package com.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

public class AuthServiceCloseAndDbEnvTest {

    @Test
    public void loginReturnsDbErrorWhenResultSetCloseFails() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("clave_hash")).thenReturn("Secret1!");
        when(rs.getInt("intentos_fallidos")).thenReturn(0);
        when(rs.getBoolean("bloqueado")).thenReturn(false);

        // Make close() throw so try-with-resources close path is exercised
        doThrow(new SQLException("close fail")).when(rs).close();

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Secret1!");
        assertTrue(r.startsWith("Error de BD:"));
        assertTrue(r.contains("close fail"));
    }

    @Test
    public void loginPreparedStatementCloseThrows() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        doThrow(new SQLException("ps close fail login")).when(ps).close();

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Secret1!");
        assertTrue(r.startsWith("Error de BD:") && r.contains("ps close fail login"));
    }

    @Test
    public void loginBothCloseThrow() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        doThrow(new SQLException("rs close fail both")).when(rs).close();
        doThrow(new SQLException("ps close fail both")).when(ps).close();

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Secret1!");
        assertTrue(r.startsWith("Error de BD:"));
    }

    @Test
    public void loginQueryAndPreparedStatementCloseThrow() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        // executeQuery throws
        when(ps.executeQuery()).thenThrow(new SQLException("exec fail"));
        doThrow(new SQLException("ps close after exec fail")).when(ps).close();

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Secret1!");
        assertTrue(r.startsWith("Error de BD:"));
    }

    @Test
    public void recoverQueryAndPreparedStatementCloseThrow() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenThrow(new SQLException("exec fail recover"));
        doThrow(new SQLException("ps close after exec fail recover")).when(ps).close();

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.recoverPasswordWithConnection(conn, "u@example.com");
        assertTrue(r.startsWith("Error de BD:"));
    }

    @Test
    public void recoverReturnsDbErrorWhenPreparedStatementCloseFails() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        // Throw on ps.close()
        doThrow(new SQLException("ps close fail")).when(ps).close();

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.recoverPasswordWithConnection(conn, "u@example.com");
        assertTrue(r.startsWith("Error de BD:"));
        assertTrue(r.contains("ps close fail"));
    }

    @Test
    public void getEnvOrDefaultReturnsEnvironmentValueWhenPresent() throws Exception {
        // Use an environment variable that is always present on the OS to exercise the
        // `value != null && !value.trim().isEmpty()` branch (avoid modifying env via reflection).
        String name = "PATH";
        Method m = DbConnection.class.getDeclaredMethod("getEnvOrDefault", String.class, String.class);
        m.setAccessible(true);
        String res = (String) m.invoke(null, name, "fallback");
        // PATH should exist on the environment for the running process; ensure we didn't get the fallback.
        assertTrue(res != null && !res.equals("fallback") && res.length() > 0);
    }

    // Reflection helper to set/unset environment variables for the running JVM process (test-only)
    // No-op: we avoid modifying the process environment in tests on modern JVMs.
    @SuppressWarnings("unused")
    private static void setEnv(String key, String value) throws Exception {
        // intentionally left empty — not used on modern JVMs in this test.
    }
}
