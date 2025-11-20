package com.auth;

import static org.junit.Assert.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.junit.Test;

public class AuthFramePaintTest {

    @Test
    public void paintRoundedPanelsAndButtonStates() throws Exception {
        final AuthFrame[] holder = new AuthFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new AuthFrame());
        AuthFrame frame = holder[0];

        // Access private fields
        Field emailF = AuthFrame.class.getDeclaredField("emailField");
        Field passF = AuthFrame.class.getDeclaredField("passwordField");
        emailF.setAccessible(true); passF.setAccessible(true);

        Object emailField = emailF.get(frame);
        Object passField = passF.get(frame);

        // their parents are the rounded panels; paint them
        javax.swing.JComponent emailParent = (javax.swing.JComponent) ((javax.swing.JComponent) emailField).getParent();
        javax.swing.JComponent passParent = (javax.swing.JComponent) ((javax.swing.JComponent) passField).getParent();

        // Find login button by traversing components
        final JButton[] loginBtnHolder = new JButton[1];
        for (java.awt.Component c : frame.getContentPane().getComponents()) {
            // search recursively
            java.util.List<java.awt.Component> list = new java.util.ArrayList<>();
            list.add(c);
            for (int i = 0; i < list.size(); i++) {
                java.awt.Component comp = list.get(i);
                    if (comp instanceof JButton) {
                        JButton b = (JButton) comp;
                        if ("Ingresar".equals(b.getText())) {
                            loginBtnHolder[0] = b;
                            break;
                        }
                    }
                if (comp instanceof java.awt.Container) {
                    for (java.awt.Component cc : ((java.awt.Container) comp).getComponents()) list.add(cc);
                }
            }
            if (loginBtnHolder[0] != null) break;
        }

        assertNotNull("Login button should be found", loginBtnHolder[0]);

        // Parent of the button is the rounded button panel
        javax.swing.JComponent btnParent = (javax.swing.JComponent) loginBtnHolder[0].getParent();

        // Ensure components have size to paint
        SwingUtilities.invokeAndWait(() -> {
            emailParent.setSize(100, 40);
            passParent.setSize(100, 40);
            btnParent.setSize(100, 40);
        });

        BufferedImage img = new BufferedImage(120, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // paint default (button not armed)
        btnParent.paint(g2);
        emailParent.paint(g2);
        passParent.paint(g2);

        // now set the button model to armed and repaint to exercise the armed branch
        SwingUtilities.invokeAndWait(() -> loginBtnHolder[0].getModel().setArmed(true));
        btnParent.paint(g2);

        g2.dispose();

        // simple assertion: image has non-zero pixel (ensures paint executed)
        boolean nonZero = false;
        for (int x = 0; x < img.getWidth() && !nonZero; x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if ((img.getRGB(x, y) & 0xFF000000) != 0) { nonZero = true; break; }
            }
        }

        assertTrue("Paint should have drawn pixels", nonZero);

        SwingUtilities.invokeAndWait(() -> frame.dispose());
    }
}
