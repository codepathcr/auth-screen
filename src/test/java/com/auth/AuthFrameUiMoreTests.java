package com.auth;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import java.awt.event.KeyEvent;
import javax.swing.JFrame;

import static org.assertj.swing.timing.Pause.pause;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AuthFrameUiMoreTests extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    protected void onSetUp() {
        // default stub that returns generic success; individual tests override when needed
        AuthService defaultStub = new AuthService() {
            @Override
            public String login(String email, String password) {
                return "Login exitoso 🎉";
            }

            @Override
            public String recoverPassword(String email) {
                return "Se ha enviado un email de recuperación (simulado).";
            }
        };

        AuthFrame frame = GuiActionRunner.execute(() -> {
            AuthFrame f = new AuthFrame(defaultStub);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            return f;
        });
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Test
    public void invalidEmail_showsValidationMessage() {
        // Use a stub that mimics validation behavior from production
        AuthService stub = new AuthService() {
            @Override
            public String login(String email, String password) {
                // simulate server-side validation consistent with EmailValidator
                if (email == null || !email.contains("@") || !email.contains(".")) {
                    return "Email no válido";
                }
                return "Login exitoso 🎉";
            }

            @Override
            public String recoverPassword(String email) {
                return "Se ha enviado un email de recuperación (simulado).";
            }
        };

        // recreate window with the validation stub
        window.close();
        AuthFrame frame = GuiActionRunner.execute(() -> {
            AuthFrame f = new AuthFrame(stub);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            return f;
        });
        window = new FrameFixture(robot(), frame);
        window.show();

        window.textBox("emailField").setText("bademail");
        window.textBox("passwordField").setText("Abc!1");
        window.button("loginButton").click();
        pause(200, MILLISECONDS);
        window.label("statusLabel").requireText("Email no válido");
    }

    @Test
    public void emptyFields_showsAppropriateMessages() {
        // stub that returns specific messages when inputs are empty
        AuthService stub = new AuthService() {
            @Override
            public String login(String email, String password) {
                if (email == null || email.trim().isEmpty()) return "Email no válido";
                if (password == null || password.trim().isEmpty()) return "Clave inválida: 5-10 chars, 1 mayúscula, 1 carácter especial";
                return "Login exitoso 🎉";
            }

            @Override
            public String recoverPassword(String email) {
                if (email == null || email.trim().isEmpty()) return "Ingrese un email válido para recuperar clave";
                return "Se ha enviado un email de recuperación (simulado).";
            }
        };

        window.close();
        AuthFrame frame = GuiActionRunner.execute(() -> {
            AuthFrame f = new AuthFrame(stub);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            return f;
        });
        window = new FrameFixture(robot(), frame);
        window.show();

        // Empty email -> login should show email invalid
        window.textBox("emailField").setText("");
        window.textBox("passwordField").setText("Abc!1");
        window.button("loginButton").click();
        window.label("statusLabel").requireText("Email no válido");

        // Empty password -> set a valid email then empty password
        window.textBox("emailField").setText("user@example.com");
        window.textBox("passwordField").setText("");
        window.button("loginButton").click();
        pause(200, MILLISECONDS);
        window.label("statusLabel").requireText("Clave inválida: 5-10 chars, 1 mayúscula, 1 carácter especial");

        // Recover with empty email
        window.textBox("emailField").setText("");
        window.button("forgotButton").click();
        pause(200, MILLISECONDS);
        window.label("statusLabel").requireText("Ingrese un email válido para recuperar clave");
    }

    @Test
    public void enterKey_triggersLogin() {
        // stub that notes when login is invoked
        AuthService stub = new AuthService() {
            @Override
            public String login(String email, String password) {
                // provide deterministic response
                return "Login exitoso 🎉";
            }
        };

        window.close();
        AuthFrame frame = GuiActionRunner.execute(() -> {
            AuthFrame f = new AuthFrame(stub);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            return f;
        });
        window = new FrameFixture(robot(), frame);
        window.show();

        window.textBox("emailField").setText("user@example.com");
        window.textBox("passwordField").setText("Abc!1");

        // press Enter while focus is on the password field
        window.textBox("passwordField").pressAndReleaseKeys(KeyEvent.VK_ENTER);

        // give the EDT a moment to process
        pause(200, MILLISECONDS);

        window.label("statusLabel").requireText("Login exitoso 🎉");
    }
}
