package com.auth;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Test;

public class AuthServiceCloseExtraTest {

    @Test
    public void loginPsCloseThrowsAfterSuccessfulQuery() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("clave_hash")).thenReturn("Secret1!");
        when(rs.getInt("intentos_fallidos")).thenReturn(0);
        when(rs.getBoolean("bloqueado")).thenReturn(false);

        // ps.close will throw, rs.close will succeed
        doThrow(new java.sql.SQLException("ps close after success")).when(ps).close();

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Secret1!");
        assertTrue(r.startsWith("Error de BD:") && r.contains("ps close after success"));
    }

    @Test
    public void recoverPsCloseThrowsAfterSuccessfulQuery() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        doThrow(new java.sql.SQLException("ps close after recover success")).when(ps).close();

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.recoverPasswordWithConnection(conn, "u@example.com");
        assertTrue(r.startsWith("Error de BD:") && r.contains("ps close after recover success"));
    }

    @Test
    public void getEnvOrDefaultReturnsFallbackWhenNoSysOrEnv() throws Exception {
        String key = "SOME_UNLIKELY_ENV_KEY_12345";
        // Ensure no system property is set for the key
        System.clearProperty(key);

        Method m = DbConnection.class.getDeclaredMethod("getEnvOrDefault", String.class, String.class);
        m.setAccessible(true);
        String res = (String) m.invoke(null, key, "fallback-value-xyz");
        assertTrue("Expected fallback when no env or sysprop", "fallback-value-xyz".equals(res));
    }

    @Test
    public void loginWhenPrepareStatementThrows() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new java.sql.SQLException("prepare fail login"));

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Secret1!");
        assertTrue(r.startsWith("Error de BD:") && r.contains("prepare fail login"));
    }

    @Test
    public void recoverWhenPrepareStatementThrows() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new java.sql.SQLException("prepare fail recover"));

        AuthService svc = new AuthService();
        String r = svc.recoverPasswordWithConnection(conn, "u@example.com");
        assertTrue(r.startsWith("Error de BD:") && r.contains("prepare fail recover"));
    }
}
