package gui;

import models.*;
import models.Event;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ParticipantUI {
    private JPanel panel;

    public ParticipantUI(Participant p, MainGUI app) {
        panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = { "ID", "Title", "Date", "Time", "Capacity", "Organizer" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        for (Event e : p.browseEvents())
            model.addRow(new Object[] { e.getId(), e.getTitle(), e.getDate(), e.getTime(), e.getCapacity(), e.getOrganizerName() });

        table.setToolTipText("Double-click an event to view details.");
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int eventId = (int) table.getValueAt(row, 0);
                        for (Event event : p.browseEvents()) {
                            if (event.getId() == eventId) { openEventDetails(app, event); break; }
                        }
                    }
                }
            }
        });

        JButton regBtn = new JButton("Register for Selected Event");
        regBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) JOptionPane.showMessageDialog(app.getFrame(), "Please select an event.");
            else JOptionPane.showMessageDialog(app.getFrame(), p.registerForEvent((int) table.getValueAt(row, 0)));
        });

        JButton unregBtn = new JButton("Unregister from Selected Event");
        unregBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) JOptionPane.showMessageDialog(app.getFrame(), "Please select an event.");
            else JOptionPane.showMessageDialog(app.getFrame(), p.unregisterFromEvent((int) table.getValueAt(row, 0)));
        });

        JPanel notifPanel = new JPanel(new BorderLayout());
        notifPanel.setBorder(BorderFactory.createTitledBorder("Notifications & Invites"));
        JTextArea notifs = new JTextArea(5, 20);
        notifs.setEditable(false);
        for (String n : p.checkNotifications()) notifs.append(n + "\n");
        notifPanel.add(new JScrollPane(notifs));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(regBtn); buttonPanel.add(unregBtn);

        JPanel bot = new JPanel(new BorderLayout(0, 10));
        bot.add(buttonPanel, BorderLayout.NORTH);
        bot.add(notifPanel, BorderLayout.CENTER);
        panel.add(bot, BorderLayout.SOUTH);
    }

    private void openEventDetails(MainGUI app, Event event) {
        JDialog dialog = new JDialog(app.getFrame(), "Event Details: " + event.getTitle(), true);
        dialog.setSize(600, 420); // Widened to make the description look better
        dialog.setLocationRelativeTo(app.getFrame());
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel topDetails = new JPanel(new GridLayout(4, 1, 5, 5));
        topDetails.add(new JLabel("<html><b>Title:</b> " + event.getTitle() + "</html>"));
        topDetails.add(new JLabel("<html><b>Date & Time:</b> " + event.getDate() + " @ " + event.getTime() + "</html>"));
        topDetails.add(new JLabel("<html><b>Capacity:</b> " + event.getCapacity() + "</html>"));
        topDetails.add(new JLabel("<html><b>Organizer:</b> " + (event.getOrganizerName() != null ? event.getOrganizerName() : "Unknown") + "</html>"));

        String description = event.getDescription() != null && !event.getDescription().trim().isEmpty() ? event.getDescription() : "No description provided.";
        JTextArea descArea = new JTextArea(description);
        descArea.setWrapStyleWord(true); descArea.setLineWrap(true);
        descArea.setEditable(false); descArea.setOpaque(false);
        descArea.setFont(new JLabel().getFont());
        
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createTitledBorder("Description"));

        JPanel detailsPanel = new JPanel(new BorderLayout(5, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        detailsPanel.add(topDetails, BorderLayout.NORTH);
        detailsPanel.add(descScroll, BorderLayout.CENTER);

        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    public JPanel getPanel() { return panel; }
}