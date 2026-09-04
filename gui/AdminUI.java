package gui;

import models.*;
import models.Event;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminUI {
    private JTabbedPane adminTabs;
    private final Color SOFT_RED = new Color(220, 53, 69); 

    public AdminUI(Admin a, MainGUI app) {
        adminTabs = new JTabbedPane();
        adminTabs.addTab("Event Management", createAdminEventsPanel(a, app));
        adminTabs.addTab("User Management", createAdminUsersPanel(a, app));
    }

    public JPanel getPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(adminTabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createAdminEventsPanel(Admin a, MainGUI app) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = { "ID", "Title", "Date", "Time", "Capacity", "Organizer" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30); 
        table.setToolTipText("Double-click an event to view details, reports & participants.");
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int eventId = (int) table.getValueAt(row, 0);
                        for (Event ev : a.viewAllEvents()) {
                            if (ev.getId() == eventId) {
                                openEventReport(app, a, ev.getId(), ev.getTitle(), ev.getDescription(), ev.getDate(),
                                        ev.getTime(), ev.getCapacity());
                                break;
                            }
                        }
                    }
                }
            }
        });

        Runnable refreshTable = () -> {
            model.setRowCount(0);
            for (Event ev : a.viewAllEvents())
                model.addRow(new Object[] { ev.getId(), ev.getTitle(), ev.getDate(), ev.getTime(), ev.getCapacity(),
                        ev.getOrganizerName() });
        };
        refreshTable.run();

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton createBtn = new JButton("Create Event");
        JButton editBtn = new JButton("Edit Event");
        JButton delEvtBtn = new JButton("Delete Event");
        delEvtBtn.setForeground(SOFT_RED);

        createBtn.addActionListener(e -> {
            JTextField titleF = new JTextField();
            
            JTextArea descF = new JTextArea(6, 35);
            descF.setLineWrap(true);
            descF.setWrapStyleWord(true);
            
            JTextField capF = new JTextField(10);
            JPanel capPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            capPanel.add(capF);
            
            DateTimePicker dtPicker = new DateTimePicker(null, null);
            
            // Replaced dtPicker.getPanel() labels with "Date & Time:"
            Object[] msg = { "Title:", titleF, "Description:", new JScrollPane(descF), "Date & Time:", dtPicker.getPanel(), "Capacity:", capPanel };

            while (true) {
                if (JOptionPane.showConfirmDialog(app.getFrame(), msg, "Create Event",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (titleF.getText().trim().isEmpty() || capF.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(app.getFrame(), "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    try {
                        String res = a.createEvent(titleF.getText(), descF.getText(), dtPicker.getDateString(),
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
            for (Event ev : a.viewAllEvents()) {
                if (ev.getId() == eventId) { selectedEvent = ev; break; }
            }

            if (selectedEvent == null) {
                JOptionPane.showMessageDialog(app.getFrame(), "Event could not be found.");
                return;
            }

            JTextField titleF = new JTextField(selectedEvent.getTitle());
            JTextArea descF = new JTextArea(selectedEvent.getDescription() != null ? selectedEvent.getDescription() : "", 6, 35);
            descF.setLineWrap(true);
            descF.setWrapStyleWord(true);
            
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
                        String res = a.updateAnyEvent((int) table.getValueAt(row, 0), titleF.getText(), descF.getText(),
                                dtPicker.getDateString(), dtPicker.getTimeString(), Integer.parseInt(capF.getText()));
                        JOptionPane.showMessageDialog(app.getFrame(), res);
                        if (res.startsWith("Success")) { refreshTable.run(); break; }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(app.getFrame(), "Capacity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else break;
            }
        });

        delEvtBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1 && JOptionPane.showConfirmDialog(app.getFrame(), "Terminate event?", "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(app.getFrame(), a.deleteAnyEvent((int) table.getValueAt(row, 0)));
                refreshTable.run();
            }
        });

        bot.add(createBtn); bot.add(editBtn); bot.add(delEvtBtn);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAdminUsersPanel(Admin a, MainGUI app) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = { "ID", "Username", "Role", "Full Name" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setToolTipText("Double-click a user to view their full profile.");

        Runnable refreshTable = () -> {
            model.setRowCount(0);
            for (String[] u : a.viewAllUsers()) model.addRow(u);
        };
        refreshTable.run();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String username = (String) table.getValueAt(row, 1);
                        String[] profile = a.viewUserProfile(username);
                        if (profile != null) {
                            String msg = "<html><div style='padding: 10px; font-family: Segoe UI, sans-serif;'>"
                                    + "<b>Username:</b> " + profile[0]
                                    + "<br><b>Role:</b> " + profile[1]
                                    + "<br><b>Full Name:</b> " + profile[2]
                                    + "<br><b>Age:</b> " + profile[3]
                                    + "<br><b>Department:</b> " + profile[4]
                                    + "<br><b>Year Level:</b> " + profile[5] + "</div></html>";
                            JOptionPane.showMessageDialog(app.getFrame(), msg, "User Profile", JOptionPane.PLAIN_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(app.getFrame(), "Could not load profile.");
                        }
                    }
                }
            }
        });

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton createBtn = new JButton("Create User");
        JButton promoteBtn = new JButton("Promote to Organizer");
        JButton demoteBtn = new JButton("Demote to Participant");
        JButton purgeBtn = new JButton("Purge Account");
        purgeBtn.setForeground(SOFT_RED);

        createBtn.addActionListener(e -> {
            JTextField userF = new JTextField();
            JPasswordField passF = new JPasswordField();
            Object[] msg = { "Username:", userF, "Password:", passF };

            while (true) {
                if (JOptionPane.showConfirmDialog(app.getFrame(), msg, "Create Participant",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (userF.getText().trim().isEmpty() || new String(passF.getPassword()).trim().isEmpty()) {
                        JOptionPane.showMessageDialog(app.getFrame(), "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    String res = User.register(userF.getText(), new String(passF.getPassword()), "Participant");
                    JOptionPane.showMessageDialog(app.getFrame(), res);
                    if (res.startsWith("Success")) { refreshTable.run(); break; }
                } else break;
            }
        });

        promoteBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                JOptionPane.showMessageDialog(app.getFrame(), a.changeUserRole((String) table.getValueAt(r, 1), "Organizer"));
                refreshTable.run();
            }
        });

        demoteBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                JOptionPane.showMessageDialog(app.getFrame(), a.changeUserRole((String) table.getValueAt(r, 1), "Participant"));
                refreshTable.run();
            }
        });

        purgeBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1 && JOptionPane.showConfirmDialog(app.getFrame(), "Purge this account permanently?", "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(app.getFrame(), a.deleteAccount((String) table.getValueAt(r, 1)));
                refreshTable.run();
            }
        });

        bot.add(createBtn); bot.add(promoteBtn); bot.add(demoteBtn); bot.add(purgeBtn);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    private void openEventReport(MainGUI app, Admin a, int eventId, String title, String description, String date, String time, int capacity) {
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
        viewPartsBtn.addActionListener(e -> openParticipantsDialog(app, a, eventId, title));

        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botPanel.add(viewPartsBtn);
        detailsPanel.add(botPanel, BorderLayout.SOUTH);

        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void openParticipantsDialog(MainGUI app, Admin a, int eventId, String title) {
        JDialog dialog = new JDialog(app.getFrame(), "Participants: " + title, true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(app.getFrame());
        dialog.setLayout(new BorderLayout(10, 10));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        DefaultListModel<String> invitedModel = new DefaultListModel<>();
        Runnable refreshList = () -> {
            listModel.clear();
            for (String p : a.generateReport(eventId)) listModel.addElement(p);
            invitedModel.clear();
            for (String p : a.getInvitedParticipants(eventId)) invitedModel.addElement(p);
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
                JOptionPane.showMessageDialog(dialog, a.inviteParticipant(eventId, uname.trim()));
                refreshList.run();
            }
        });

        removeBtn.addActionListener(e -> {
            String sel = partList.getSelectedValue();
            if (sel != null) {
                String uname = sel.contains(" (") ? sel.substring(0, sel.indexOf(" (")) : sel;
                if (JOptionPane.showConfirmDialog(dialog, "Remove " + uname + "?", "Confirm",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(dialog, a.forceParticipantUpdate(eventId, uname, false));
                    refreshList.run();
                }
            } else JOptionPane.showMessageDialog(dialog, "Select a participant.");
        });

        actions.add(inviteBtn); actions.add(removeBtn);
        dialog.add(listsPanel, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}