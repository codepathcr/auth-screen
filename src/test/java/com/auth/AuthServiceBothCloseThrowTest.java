package com.auth;

import org.junit.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthServiceBothCloseThrowTest {

    @Test
    public void bothCloseThrow_rsFirst_psSecond_elseBranchExecuted() throws SQLException {
        AuthService svc = new AuthService();

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        // Make the query (rs.next) throw a primary SQLException. Then both
        // rs.close() and ps.close() will throw; this creates the situation
        // where primaryEx != null and closeEx is set, so the code will add the
        // close exception as suppressed on the primary exception and not
        // rethrow the close exception.
        when(rs.next()).thenThrow(new SQLException("primary-exec"));

        doThrow(new SQLException("rsClose")).when(rs).close();
        doThrow(new SQLException("psClose")).when(ps).close();

        String res = svc.recoverPasswordWithConnection(conn, "a@b.com");
        // The outer method should catch the primary SQLException and return
        // an error string containing the primary message.
        assertTrue(res.contains("primary-exec"));
    }
}
