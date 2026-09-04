package gui;

import database.DatabaseManager;
import models.*;
import javax.swing.*;
import java.awt.*;

public class MainGUI {
    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public MainGUI() {
        setupModernLookAndFeel();

        DatabaseManager.initializeDatabase();
        frame = new JFrame("Academic Organization Platform");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 650); // Increased default size for better spacing
        frame.setLocationRelativeTo(null); 

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Load the Auth views
        cardPanel.add(new AuthUI(this).getPanel(), "Auth");

        frame.add(cardPanel);
        frame.setVisible(true);
    }

    private void setupModernLookAndFeel() {
        try {
            // Attempt to load FlatLaf for a completely flat, modern design
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Button.arc", 8); // Rounded corners
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
        } catch (Exception ex) {
            // Fallback to the Native System Look and Feel if FlatLaf isn't in the classpath
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Apply a modern Sans-Serif font globally
        Font modernFont = new Font("Segoe UI", Font.PLAIN, 14);
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, new javax.swing.plaf.FontUIResource(modernFont));
            }
        }
    }

    public void loginUser(User u) {
        JPanel eventsPanel = null;
        if (u instanceof Admin) eventsPanel = new AdminUI((Admin) u, this).getPanel();
        else if (u instanceof Organizer) eventsPanel = new OrganizerUI((Organizer) u, this).getPanel();
        else if (u instanceof Participant) eventsPanel = new ParticipantUI((Participant) u, this).getPanel();
        
        if (eventsPanel != null) {
            cardPanel.add(new DashboardUI(u, eventsPanel, this).getPanel(), "Dash");
            cardLayout.show(cardPanel, "Dash");
        }
    }

    public void logout() {
        cardLayout.show(cardPanel, "Auth");
    }

    public JFrame getFrame() { return frame; }
}