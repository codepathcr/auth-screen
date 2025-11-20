package com.auth;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

import org.junit.Test;

public class AuthFrameTest {

    @Test
    public void testHandleLoginUpdatesStatus() throws Exception {
        // Create frame on EDT
        final AuthFrame[] holder = new AuthFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new AuthFrame());
        AuthFrame frame = holder[0];

        // Replace authService with a stub that returns a known message
        AuthService stub = new AuthService() {
            @Override
            public String login(String email, String password) {
                return "STUB_LOGIN_OK";
            }
        };
        Field svcField = AuthFrame.class.getDeclaredField("authService");
        svcField.setAccessible(true);
        svcField.set(frame, stub);

        // Set email and password
        Field emailF = AuthFrame.class.getDeclaredField("emailField");
        Field passF = AuthFrame.class.getDeclaredField("passwordField");
        Field statusF = AuthFrame.class.getDeclaredField("statusLabel");
        emailF.setAccessible(true); passF.setAccessible(true); statusF.setAccessible(true);
        ((javax.swing.JTextField) emailF.get(frame)).setText("u@e.com");
        ((javax.swing.JPasswordField) passF.get(frame)).setText("Abc!1");

        // Invoke private handleLogin
        Method m = AuthFrame.class.getDeclaredMethod("handleLogin", java.awt.event.ActionEvent.class);
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                m.invoke(frame, new java.awt.event.ActionEvent(this, 0, "cmd"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        String status = ((javax.swing.JLabel) statusF.get(frame)).getText();
        assertEquals("STUB_LOGIN_OK", status);

        // dispose frame
        SwingUtilities.invokeAndWait(() -> frame.dispose());
    }

    @Test
    public void testHandleForgotPasswordUpdatesStatus() throws Exception {
        final AuthFrame[] holder = new AuthFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new AuthFrame());
        AuthFrame frame = holder[0];

        AuthService stub = new AuthService() {
            @Override
            public String recoverPassword(String email) {
                return "STUB_RECOVERED";
            }
        };
        Field svcField = AuthFrame.class.getDeclaredField("authService");
        svcField.setAccessible(true);
        svcField.set(frame, stub);

        Field emailF = AuthFrame.class.getDeclaredField("emailField");
        Field statusF = AuthFrame.class.getDeclaredField("statusLabel");
        emailF.setAccessible(true); statusF.setAccessible(true);
        ((javax.swing.JTextField) emailF.get(frame)).setText("u@e.com");

        Method m = AuthFrame.class.getDeclaredMethod("handleForgotPassword", java.awt.event.ActionEvent.class);
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                m.invoke(frame, new java.awt.event.ActionEvent(this, 0, "cmd"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        String status = ((javax.swing.JLabel) statusF.get(frame)).getText();
        assertEquals("STUB_RECOVERED", status);

        SwingUtilities.invokeAndWait(() -> frame.dispose());
    }
}
