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
        DatabaseManager.initializeDatabase();
        frame = new JFrame("Academic Organization Platform");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 600); 
        frame.setLocationRelativeTo(null); 

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Load the Auth views
        cardPanel.add(new AuthUI(this).getPanel(), "Auth");

        frame.add(cardPanel);
        frame.setVisible(true);
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