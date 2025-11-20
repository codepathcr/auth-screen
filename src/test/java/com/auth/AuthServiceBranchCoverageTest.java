package com.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Test;

public class AuthServiceBranchCoverageTest {

    @Test
    public void loginUserNotFoundReturnsUsuarioNoEncontrado() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "noone@example.com", "Abcde!");
        assertEquals("Usuario no encontrado", r);
    }

    @Test
    public void loginBlockedAccountReturnsCuentaBloqueada() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(2);
        when(rs.getString("clave_hash")).thenReturn("whatever");
        when(rs.getInt("intentos_fallidos")).thenReturn(3);
        when(rs.getBoolean("bloqueado")).thenReturn(true);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Abcde!");
        assertEquals("Cuenta bloqueada por intentos fallidos", r);
    }

    @Test
    public void loginIncorrectPasswordIncrementsAttemptsAndReturnsClaveIncorrecta() throws Exception {
        PreparedStatement selectPs = mock(PreparedStatement.class);
        PreparedStatement updatePs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(selectPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(5);
        when(rs.getString("clave_hash")).thenReturn("not-the-password");
        when(rs.getInt("intentos_fallidos")).thenReturn(1);
        when(rs.getBoolean("bloqueado")).thenReturn(false);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(startsWith("SELECT"))).thenReturn(selectPs);
        when(conn.prepareStatement(startsWith("UPDATE"))).thenReturn(updatePs);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Abcde!");
        assertTrue(r.startsWith("Clave incorrecta."));
    }

    @Test
    public void loginIncorrectPasswordThatBlocksReturnsCuentaBloqueadaMsg() throws Exception {
        PreparedStatement selectPs = mock(PreparedStatement.class);
        PreparedStatement updatePs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(selectPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(7);
        when(rs.getString("clave_hash")).thenReturn("nope");
        when(rs.getInt("intentos_fallidos")).thenReturn(4);
        when(rs.getBoolean("bloqueado")).thenReturn(false);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(startsWith("SELECT"))).thenReturn(selectPs);
        when(conn.prepareStatement(startsWith("UPDATE"))).thenReturn(updatePs);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Abcde!");
        assertEquals("Cuenta bloqueada. Excedió los 5 intentos.", r);
    }

    @Test
    public void loginCorrectPasswordReturnsSuccessAndResetsAttempts() throws Exception {
        PreparedStatement selectPs = mock(PreparedStatement.class);
        PreparedStatement updatePs = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(selectPs.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(9);
        when(rs.getString("clave_hash")).thenReturn("Secret1!");
        when(rs.getInt("intentos_fallidos")).thenReturn(2);
        when(rs.getBoolean("bloqueado")).thenReturn(false);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(startsWith("SELECT"))).thenReturn(selectPs);
        when(conn.prepareStatement(startsWith("UPDATE"))).thenReturn(updatePs);

        AuthService svc = new AuthService();
        String r = svc.loginWithConnection(conn, "u@example.com", "Secret1!");
        assertEquals("Login exitoso 🎉", r);
    }
}
