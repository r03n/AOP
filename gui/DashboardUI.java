package gui;

import models.User;
import javax.swing.*;
import java.awt.*;

public class DashboardUI {
    private JPanel mainPanel;
    
    public DashboardUI(User u, JPanel eventsPanel, MainGUI app) {
        mainPanel = new JPanel(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 52, 54)); 
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel nameLabel = new JLabel(u.getUsername(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel roleLabel = new JLabel(u.getRole(), SwingConstants.CENTER);
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        roleLabel.setForeground(Color.LIGHT_GRAY);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel descLabel = new JLabel("<html><center>" + u.getRoleDescription() + "</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        descLabel.setForeground(new Color(150, 150, 150));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton eventsBtn = createSidebarButton("Dashboard");
        JButton profileBtn = createSidebarButton("Profile");
        JButton logoutBtn = createSidebarButton("Logout");

        sidebar.add(nameLabel);
        sidebar.add(roleLabel);
        sidebar.add(descLabel); 
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
        sidebar.add(eventsBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(profileBtn);
        sidebar.add(Box.createVerticalGlue()); 
        sidebar.add(logoutBtn);

        CardLayout contentLayout = new CardLayout();
        JPanel contentArea = new JPanel(contentLayout);
        
        contentArea.add(eventsPanel, "Events");
        contentArea.add(createProfilePanel(u, app), "Profile");

        eventsBtn.addActionListener(e -> contentLayout.show(contentArea, "Events"));
        profileBtn.addActionListener(e -> contentLayout.show(contentArea, "Profile"));
        logoutBtn.addActionListener(e -> app.logout());

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentArea, BorderLayout.CENTER);
    }

    public JPanel getPanel() { return mainPanel; }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(60, 65, 68));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createProfilePanel(User u, MainGUI app) {
        JPanel panel = new JPanel(new GridBagLayout());
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(null, "Edit Profile", 0, 0, new Font("SansSerif", Font.BOLD, 16)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JTextField nameF = new JTextField(u.getFullName() != null ? u.getFullName() : "");
        JTextField ageF = new JTextField(u.getAge() > 0 ? String.valueOf(u.getAge()) : "");
        JTextField deptF = new JTextField(u.getDepartment() != null ? u.getDepartment() : "");
        JTextField yearF = new JTextField(u.getYearLevel() != null ? u.getYearLevel() : "");

        form.add(new JLabel("Full Name:")); form.add(nameF);
        form.add(new JLabel("Age:")); form.add(ageF);
        form.add(new JLabel("Department:")); form.add(deptF);
        form.add(new JLabel("Year Level:")); form.add(yearF);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);

        saveBtn.addActionListener(e -> {
            try {
                int age = ageF.getText().isEmpty() ? 0 : Integer.parseInt(ageF.getText());
                String msg = u.updateProfile(nameF.getText(), age, deptF.getText(), yearF.getText());
                JOptionPane.showMessageDialog(app.getFrame(), msg);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(app.getFrame(), "Age must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(new JLabel()); form.add(saveBtn);
        panel.add(form);
        return panel;
    }
}