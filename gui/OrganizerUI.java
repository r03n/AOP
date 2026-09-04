package gui;

import models.*;
import models.Event;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrganizerUI {
    private JPanel panel;
    private final Color SOFT_RED = new Color(220, 53, 69);

    public OrganizerUI(Organizer o, MainGUI app) {
        panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = { "ID", "Title", "Date", "Time", "Capacity" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setToolTipText("Double-click an event to view details, reports & participants.");

        Runnable refreshTable = () -> {
            model.setRowCount(0);
            for (Event ev : o.viewMyEvents()) {
                model.addRow(new Object[] { ev.getId(), ev.getTitle(), ev.getDate(), ev.getTime(), ev.getCapacity() });
            }
        };
        refreshTable.run();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int eventId = (int) table.getValueAt(row, 0);
                        Event selectedEvent = null;
                        for (Event ev : o.viewMyEvents()) {
                            if (ev.getId() == eventId) { selectedEvent = ev; break; }
                        }
                        if (selectedEvent != null) {
                            openEventReport(app, o, selectedEvent.getId(), selectedEvent.getTitle(),
                                    selectedEvent.getDescription(), selectedEvent.getDate(),
                                    selectedEvent.getTime(), selectedEvent.getCapacity());
                        }
                    }
                }
            }
        });

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton createBtn = new JButton("Create Event");
        JButton editBtn = new JButton("Edit Event");
        JButton deleteBtn = new JButton("Delete Event");
        deleteBtn.setForeground(SOFT_RED);

        createBtn.addActionListener(e -> {
            JTextField titleF = new JTextField();
            
            JTextArea descF = new JTextArea(6, 35);
            descF.setLineWrap(true); descF.setWrapStyleWord(true);
            
            JTextField capF = new JTextField(10);
            JPanel capPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            capPanel.add(capF);

            DateTimePicker dtPicker = new DateTimePicker(null, null);

            Object[] msg = { "Title:", titleF, "Description:", new JScrollPane(descF), "Date & Time:", dtPicker.getPanel(), "Capacity:", capPanel };

            while (true) {
                if (JOptionPane.showConfirmDialog(app.getFrame(), msg, "Create New Event",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (titleF.getText().trim().isEmpty() || capF.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(app.getFrame(), "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    try {
                        String res = o.createEvent(titleF.getText(), descF.getText(), dtPicker.getDateString(),
                                dtPicker.getTimeString(), Integer.parseInt(capF.getText()));
                        JOptionPane.showMessageDialog(app.getFrame(), res);
                        if (res.startsWith("Success")) { refreshTable.run(); break; }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(app.getFrame(), "Capacity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else break;
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(app.getFrame(), "Select an event to edit.");
                return;
            }

            int eventId = (int) table.getValueAt(row, 0);
            Event selectedEvent = null;
            for (Event ev : o.viewMyEvents()) {
                if (ev.getId() == eventId) { selectedEvent = ev; break; }
            }

            if (selectedEvent == null) {
                JOptionPane.showMessageDialog(app.getFrame(), "Event could not be found.");
                return;
            }

            JTextField titleF = new JTextField(selectedEvent.getTitle());
            JTextArea descF = new JTextArea(selectedEvent.getDescription() != null ? selectedEvent.getDescription() : "", 6, 35);
            descF.setLineWrap(true); descF.setWrapStyleWord(true);
            
            JTextField capF = new JTextField(String.valueOf(selectedEvent.getCapacity()), 10);
            JPanel capPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            capPanel.add(capF);

            DateTimePicker dtPicker = new DateTimePicker(selectedEvent.getDate(), selectedEvent.getTime());

            Object[] msg = { "Title:", titleF, "Description:", new JScrollPane(descF), "Date & Time:", dtPicker.getPanel(), "Capacity:", capPanel };

            while (true) {
                if (JOptionPane.showConfirmDialog(app.getFrame(), msg, "Edit Event",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (titleF.getText().trim().isEmpty() || capF.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(app.getFrame(), "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    try {
                        String res = o.updateEvent(eventId, titleF.getText(), descF.getText(), dtPicker.getDateString(),
                                dtPicker.getTimeString(), Integer.parseInt(capF.getText()));
                        JOptionPane.showMessageDialog(app.getFrame(), res);
                        if (res.startsWith("Success")) { refreshTable.run(); break; }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(app.getFrame(), "Capacity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else break;
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1 && JOptionPane.showConfirmDialog(app.getFrame(), "Delete this event?", "Confirm",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(app.getFrame(), o.deleteEvent((int) table.getValueAt(row, 0)));
                refreshTable.run();
            }
        });

        bot.add(createBtn); bot.add(editBtn); bot.add(deleteBtn);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bot, BorderLayout.SOUTH);
    }

    private void openEventReport(MainGUI app, Organizer o, int eventId, String title, String description, String date, String time, int capacity) {
        JDialog dialog = new JDialog(app.getFrame(), "Event Details: " + title, true);
        dialog.setSize(600, 420); 
        dialog.setLocationRelativeTo(app.getFrame());
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel topDetails = new JPanel(new GridLayout(3, 1, 5, 5));
        topDetails.add(new JLabel("<html><b>Title:</b> " + title + "</html>"));
        topDetails.add(new JLabel("<html><b>Date & Time:</b> " + date + " @ " + time + "</html>"));
        topDetails.add(new JLabel("<html><b>Capacity:</b> " + capacity + "</html>"));

        String safeDescription = description != null && !description.trim().isEmpty() ? description : "No description provided.";
        JTextArea descArea = new JTextArea(safeDescription);
        descArea.setWrapStyleWord(true); descArea.setLineWrap(true);
        descArea.setEditable(false); descArea.setOpaque(false);
        descArea.setFont(new JLabel().getFont()); 
        
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createTitledBorder("Description"));

        JPanel detailsPanel = new JPanel(new BorderLayout(5, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        detailsPanel.add(topDetails, BorderLayout.NORTH);
        detailsPanel.add(descScroll, BorderLayout.CENTER);

        JButton viewPartsBtn = new JButton("View Participants");
        viewPartsBtn.setFocusPainted(false);
        viewPartsBtn.addActionListener(e -> openParticipantsDialog(app, o, eventId, title));

        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botPanel.add(viewPartsBtn);
        detailsPanel.add(botPanel, BorderLayout.SOUTH);

        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void openParticipantsDialog(MainGUI app, Organizer o, int eventId, String title) {
        JDialog dialog = new JDialog(app.getFrame(), "Participants: " + title, true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(app.getFrame());
        dialog.setLayout(new BorderLayout(10, 10));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        DefaultListModel<String> invitedModel = new DefaultListModel<>();

        Runnable refreshList = () -> {
            listModel.clear();
            for (String p : o.generateReport(eventId)) listModel.addElement(p);
            invitedModel.clear();
            for (String p : o.getInvitedParticipants(eventId)) invitedModel.addElement(p);
        };
        refreshList.run();

        JList<String> partList = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane(partList);
        scroll.setBorder(BorderFactory.createTitledBorder("Participants"));

        JList<String> invitedList = new JList<>(invitedModel);
        JScrollPane invitedScroll = new JScrollPane(invitedList);
        invitedScroll.setBorder(BorderFactory.createTitledBorder("Invited (Pending)"));
        invitedScroll.setPreferredSize(new Dimension(0, 120));

        JPanel listsPanel = new JPanel(new BorderLayout(0, 10));
        listsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        listsPanel.add(scroll, BorderLayout.CENTER);
        listsPanel.add(invitedScroll, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton inviteBtn = new JButton("Invite Participant");
        JButton removeBtn = new JButton("Remove Selected");

        inviteBtn.addActionListener(e -> {
            String uname = JOptionPane.showInputDialog(dialog, "Username to invite:");
            if (uname != null && !uname.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, o.inviteParticipant(eventId, uname.trim()));
                refreshList.run();
            }
        });

        removeBtn.addActionListener(e -> {
            String sel = partList.getSelectedValue();
            if (sel != null) {
                String fullName = sel.contains(" (") ? sel.substring(0, sel.indexOf(" (")) : sel;
                if (JOptionPane.showConfirmDialog(dialog, "Remove " + fullName + "?", "Confirm",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(dialog, o.forceParticipantUpdate(eventId, fullName, false));
                    refreshList.run();
                }
            } else JOptionPane.showMessageDialog(dialog, "Select a participant.");
        });

        actions.add(inviteBtn); actions.add(removeBtn);
        dialog.add(listsPanel, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    public JPanel getPanel() { 
        return panel; 
    }
}