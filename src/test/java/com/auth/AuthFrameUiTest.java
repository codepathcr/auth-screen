package com.auth;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class AuthFrameUiTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    protected void onSetUp() {
        // Provide a stub AuthService so the UI actions do not touch the real DB
        AuthService stub = new AuthService() {
            @Override
            public String login(String email, String password) {
                return "Login exitoso 🎉";
            }

            @Override
            public String recoverPassword(String email) {
                return "Se ha enviado un email de recuperación (simulado).";
            }
        };

        AuthFrame frame = GuiActionRunner.execute(() -> new AuthFrame(stub));
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Test
    public void loginButton_showsSuccessMessage() {
        window.textBox("emailField").enterText("usuario@ejemplo.com");
        window.textBox("passwordField").enterText("Abc!1");
        window.button("loginButton").click();
        window.label("statusLabel").requireText("Login exitoso 🎉");
    }

    @Test
    public void forgotButton_showsRecoverMessage() {
        window.textBox("emailField").setText("usuario@ejemplo.com");
        window.button("forgotButton").click();
        window.label("statusLabel").requireText("Se ha enviado un email de recuperación (simulado).");
    }
}
