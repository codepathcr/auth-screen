package com.auth;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

public class AuthFrame extends JFrame {

    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JLabel statusLabel;
    private final AuthService authService;

    public AuthFrame() {
        this.authService = new AuthService();

        setTitle("Pantalla de Autenticación");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);

        // Background that centers the card
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(8, 12, 20));

        // Main form panel (will be wrapped in a rounded card)
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Inicio de Sesión");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy++;
        // labels should not expand horizontally
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(label("Email:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        emailField.setOpaque(false);
        emailField.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        emailField.setBackground(new Color(30, 41, 59));
        emailField.setForeground(Color.WHITE);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel emailPanel = createRoundedPanel(emailField);
        emailPanel.setPreferredSize(new Dimension(250, 50));
        // inputs should take remaining horizontal space
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(emailPanel, gbc);
        // reset weight for next row
        gbc.weightx = 0.0;

        gbc.gridx = 0;
        gbc.gridy++;
        // label for password - do not expand
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(label("Clave:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setOpaque(false);
        passwordField.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        passwordField.setBackground(new Color(30, 41, 59));
        passwordField.setForeground(Color.WHITE);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel passwordPanel = createRoundedPanel(passwordField);
        passwordPanel.setPreferredSize(new Dimension(250, 50));
        // make password input expand like the email input
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(passwordPanel, gbc);
        gbc.weightx = 0.0;

        gbc.gridx = 0;
        gbc.gridy++;
        // Create both buttons but add them inside a centered container that spans 2 columns
        GridBagConstraints btnGbc = (GridBagConstraints) gbc.clone();
        btnGbc.fill = GridBagConstraints.NONE;
        btnGbc.anchor = GridBagConstraints.CENTER;
        btnGbc.gridwidth = 2;
        btnGbc.gridx = 0;

        JButton loginButton = new JButton("Ingresar");
        loginButton.addActionListener(this::handleLogin);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setContentAreaFilled(false);
        loginButton.setBorder(null);
        loginButton.setFocusPainted(false);
        // enforce size so button fills the rounded panel
        Dimension btnSize = new Dimension(200, 40);
        loginButton.setPreferredSize(btnSize);
        loginButton.setMinimumSize(btnSize);
        loginButton.setMaximumSize(btnSize);
        JPanel loginPanel = createRoundedButtonPanel(loginButton);
        loginPanel.setPreferredSize(btnSize);
        loginPanel.setMinimumSize(btnSize);

        JButton forgotButton = new JButton("Olvidé mi clave");
        forgotButton.addActionListener(this::handleForgotPassword);
        forgotButton.setForeground(Color.WHITE);
        forgotButton.setFont(new Font("Arial", Font.BOLD, 16));
        forgotButton.setContentAreaFilled(false);
        forgotButton.setBorder(null);
        forgotButton.setFocusPainted(false);
        forgotButton.setPreferredSize(btnSize);
        forgotButton.setMinimumSize(btnSize);
        forgotButton.setMaximumSize(btnSize);
        JPanel forgotPanel = createRoundedButtonPanel(forgotButton);
        forgotPanel.setPreferredSize(btnSize);
        forgotPanel.setMinimumSize(btnSize);
        forgotPanel.setMaximumSize(btnSize);

        // Use a GridLayout with 2 columns so both buttons are always visible
        JPanel buttonsContainer = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonsContainer.setOpaque(false);
        buttonsContainer.add(loginPanel);
        buttonsContainer.add(forgotPanel);

        panel.add(buttonsContainer, btnGbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.WHITE);
        panel.add(statusLabel, gbc);

        // wrap the form panel in a rounded card and add to background
        JPanel card = createRoundedPanel(panel);
        card.setPreferredSize(new Dimension(420, 320));
        GridBagConstraints gbcBg = new GridBagConstraints();
        gbcBg.gridx = 0;
        gbcBg.gridy = 0;
        background.add(card, gbcBg);

        add(background);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        return l;
    }

    private JPanel createRoundedButtonPanel(JButton button) {
        JPanel roundedPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (button.getModel().isArmed()) {
                    g2d.setColor(new Color(59, 130, 246));
                } else {
                    g2d.setColor(new Color(79, 158, 255));
                }
                
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2d.setColor(new Color(100, 150, 255));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                super.paintComponent(g);
            }
        };
        roundedPanel.setOpaque(false);
        roundedPanel.add(button, BorderLayout.CENTER);
        return roundedPanel;
    }

    private JPanel createRoundedPanel(JComponent component) {
        JPanel roundedPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 41, 59));
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2d.setColor(new Color(71, 85, 105));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                super.paintComponent(g);
            }
        };
        roundedPanel.setOpaque(false);
        roundedPanel.setBackground(new Color(30, 41, 59));
        roundedPanel.add(component, BorderLayout.CENTER);
        return roundedPanel;
    }

    private void handleLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String mensaje = authService.login(email, password);
        statusLabel.setText(mensaje);
    }

    private void handleForgotPassword(ActionEvent e) {
        String email = emailField.getText().trim();
        String mensaje = authService.recoverPassword(email);
        statusLabel.setText(mensaje);
    }
}

