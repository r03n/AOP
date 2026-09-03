package gui;

import models.User;
import javax.swing.*;
import java.awt.*;

public class AuthUI {
    private JPanel mainPanel;
    private CardLayout layout;
    private MainGUI app;

    public AuthUI(MainGUI app) {
        this.app = app;
        layout = new CardLayout();
        mainPanel = new JPanel(layout);
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createRegisterPanel(), "Register");
    }

    public JPanel getPanel() { return mainPanel; }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); 
        JPanel form = new JPanel(new GridLayout(4, 1, 10, 10));
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true), BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        JLabel title = new JLabel("AOP Login", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        
        JPanel fields = new JPanel(new GridLayout(2, 2, 5, 5));
        fields.add(new JLabel("Username:")); fields.add(userField);
        fields.add(new JLabel("Password:")); fields.add(passField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(60, 130, 240)); loginBtn.setForeground(Color.WHITE); loginBtn.setFocusPainted(false);

        JButton regLinkBtn = new JButton("Don't have an account? Register");
        regLinkBtn.setContentAreaFilled(false); regLinkBtn.setBorderPainted(false); regLinkBtn.setForeground(Color.BLUE); regLinkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginBtn.addActionListener(e -> {
            User u = User.login(userField.getText(), new String(passField.getPassword()));
            if (u != null) {
                app.loginUser(u);
                userField.setText(""); passField.setText(""); 
            } else {
                JOptionPane.showMessageDialog(app.getFrame(), "Invalid Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        regLinkBtn.addActionListener(e -> layout.show(mainPanel, "Register"));
        form.add(title); form.add(fields); form.add(loginBtn); form.add(regLinkBtn);
        panel.add(form);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JPanel form = new JPanel(new GridLayout(4, 1, 10, 10));
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true), BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JTextField userField = new JTextField(15); JPasswordField passField = new JPasswordField(15);
        JPanel fields = new JPanel(new GridLayout(2, 2, 5, 5));
        fields.add(new JLabel("Username:")); fields.add(userField);
        fields.add(new JLabel("Password:")); fields.add(passField);

        JButton regBtn = new JButton("Register");
        regBtn.setBackground(new Color(40, 167, 69)); regBtn.setForeground(Color.WHITE); regBtn.setFocusPainted(false);

        JButton backLinkBtn = new JButton("Already have an account? Login");
        backLinkBtn.setContentAreaFilled(false); backLinkBtn.setBorderPainted(false); backLinkBtn.setForeground(Color.BLUE); backLinkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        regBtn.addActionListener(e -> {
            String msg = User.register(userField.getText(), new String(passField.getPassword()), "Participant");
            JOptionPane.showMessageDialog(app.getFrame(), msg);
            if (msg.contains("Success")) {
                layout.show(mainPanel, "Login");
                userField.setText(""); passField.setText("");
            }
        });

        backLinkBtn.addActionListener(e -> layout.show(mainPanel, "Login"));
        form.add(title); form.add(fields); form.add(regBtn); form.add(backLinkBtn);
        panel.add(form);
        return panel;
    }
}