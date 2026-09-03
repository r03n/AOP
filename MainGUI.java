import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
        
        cardPanel.add(createLoginPanel(), "Login");
        cardPanel.add(createRegisterPanel(), "Register");

        frame.add(cardPanel);
        frame.setVisible(true);
    }

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
                JPanel eventsPanel = null;
                if (u instanceof Admin) eventsPanel = createAdminDashboard((Admin) u);
                else if (u instanceof Organizer) eventsPanel = createOrganizerEvents((Organizer) u);
                else if (u instanceof Participant) eventsPanel = createParticipantEvents((Participant) u);
                
                if (eventsPanel != null) {
                    cardPanel.add(createDashboardTemplate(u, eventsPanel), "Dash");
                    cardLayout.show(cardPanel, "Dash");
                }
                userField.setText(""); passField.setText(""); 
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        regLinkBtn.addActionListener(e -> cardLayout.show(cardPanel, "Register"));

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
        fields.add(new JLabel("New Username:")); fields.add(userField);
        fields.add(new JLabel("New Password:")); fields.add(passField);

        JButton regBtn = new JButton("Register");
        regBtn.setBackground(new Color(40, 167, 69)); regBtn.setForeground(Color.WHITE); regBtn.setFocusPainted(false);

        JButton backLinkBtn = new JButton("Already have an account? Login");
        backLinkBtn.setContentAreaFilled(false); backLinkBtn.setBorderPainted(false); backLinkBtn.setForeground(Color.BLUE); backLinkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        regBtn.addActionListener(e -> {
            String msg = User.register(userField.getText(), new String(passField.getPassword()), "Participant");
            JOptionPane.showMessageDialog(frame, msg);
            if (msg.contains("Success")) {
                cardLayout.show(cardPanel, "Login");
                userField.setText(""); passField.setText("");
            }
        });

        backLinkBtn.addActionListener(e -> cardLayout.show(cardPanel, "Login"));
        form.add(title); form.add(fields); form.add(regBtn); form.add(backLinkBtn);
        panel.add(form);
        return panel;
    }

    private JPanel createDashboardTemplate(User u, JPanel eventsPanel) {
        JPanel main = new JPanel(new BorderLayout());

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
        contentArea.add(createProfilePanel(u), "Profile");

        eventsBtn.addActionListener(e -> contentLayout.show(contentArea, "Events"));
        profileBtn.addActionListener(e -> contentLayout.show(contentArea, "Profile"));
        logoutBtn.addActionListener(e -> cardLayout.show(cardPanel, "Login"));

        main.add(sidebar, BorderLayout.WEST);
        main.add(contentArea, BorderLayout.CENTER);
        
        return main;
    }

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

    private JPanel createProfilePanel(User u) {
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
                JOptionPane.showMessageDialog(frame, msg);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Age must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(new JLabel()); form.add(saveBtn);
        panel.add(form);
        return panel;
    }

    class DateTimePicker {
        private JComboBox<String> monthBox, dayBox, yearBox, hourBox, minBox, ampmBox;
        private JPanel mainPanel;

        public DateTimePicker(String existingDate, String existingTime) {
            String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            String[] days = new String[31]; for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);
            String[] years = {"2026", "2027", "2028", "2029", "2030"};
            String[] hours = new String[12]; for (int i = 0; i < 12; i++) hours[i] = String.format("%02d", i + 1);
            String[] mins = {"00", "15", "30", "45"};
            
            monthBox = new JComboBox<>(months); dayBox = new JComboBox<>(days); yearBox = new JComboBox<>(years);
            hourBox = new JComboBox<>(hours); minBox = new JComboBox<>(mins); ampmBox = new JComboBox<>(new String[]{"AM", "PM"});

            if (existingDate != null && !existingDate.isEmpty()) {
                String[] d = existingDate.split(" ");
                if (d.length == 3) { monthBox.setSelectedItem(d[0]); dayBox.setSelectedItem(d[1]); yearBox.setSelectedItem(d[2]); }
            }
            if (existingTime != null && !existingTime.isEmpty()) {
                String[] t1 = existingTime.split(" ");
                if (t1.length == 2) {
                    String[] t2 = t1[0].split(":");
                    if (t2.length == 2) { hourBox.setSelectedItem(t2[0]); minBox.setSelectedItem(t2[1]); ampmBox.setSelectedItem(t1[1]); }
                }
            }

            JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            datePanel.add(monthBox); datePanel.add(dayBox); datePanel.add(new JLabel(", ")); datePanel.add(yearBox);
            
            JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            timePanel.add(hourBox); timePanel.add(new JLabel(":")); timePanel.add(minBox); timePanel.add(new JLabel(" ")); timePanel.add(ampmBox);

            mainPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            mainPanel.add(new JLabel("Date:")); mainPanel.add(datePanel);
            mainPanel.add(new JLabel("Time:")); mainPanel.add(timePanel);
        }

        public JPanel getPanel() { return mainPanel; }
        public String getDateString() { return monthBox.getSelectedItem() + " " + dayBox.getSelectedItem() + " " + yearBox.getSelectedItem(); }
        public String getTimeString() { return hourBox.getSelectedItem() + ":" + minBox.getSelectedItem() + " " + ampmBox.getSelectedItem(); }
    }

    private JPanel createParticipantEvents(Participant p) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] cols = {"ID", "Title", "Description", "Date", "Time", "Capacity"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        for (Event e : p.browseEvents()) model.addRow(new Object[]{e.getId(), e.getTitle(), e.getDescription(), e.getDate(), e.getTime(), e.getCapacity()});
        
        JButton regBtn = new JButton("Register for Selected Event");
        regBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) JOptionPane.showMessageDialog(frame, "Please select an event.");
            else JOptionPane.showMessageDialog(frame, p.registerForEvent((int) table.getValueAt(row, 0)));
        });

        JPanel notifPanel = new JPanel(new BorderLayout());
        notifPanel.setBorder(BorderFactory.createTitledBorder("Notifications & Invites"));
        JTextArea notifs = new JTextArea(5, 20); notifs.setEditable(false);
        for (String n : p.checkNotifications()) notifs.append(n + "\n");
        notifPanel.add(new JScrollPane(notifs));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bot = new JPanel(new BorderLayout(0, 10));
        bot.add(regBtn, BorderLayout.NORTH); bot.add(notifPanel, BorderLayout.CENTER);
        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createOrganizerEvents(Organizer o) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Title", "Description", "Date", "Time", "Capacity"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        table.setToolTipText("Double-click an event to view reports & participants.");

        Runnable refreshTable = () -> {
            model.setRowCount(0);
            for (Event ev : o.viewMyEvents()) model.addRow(new Object[]{ev.getId(), ev.getTitle(), ev.getDescription(), ev.getDate(), ev.getTime(), ev.getCapacity()});
        };
        refreshTable.run();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { 
                    int row = table.getSelectedRow();
                    if (row != -1) openEventReport(o, (int)table.getValueAt(row, 0), (String)table.getValueAt(row, 1), (String)table.getValueAt(row, 3), (String)table.getValueAt(row, 4), (int)table.getValueAt(row, 5));
                }
            }
        });

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton createBtn = new JButton("Create Event");
        JButton editBtn = new JButton("Edit Event");
        JButton deleteBtn = new JButton("Delete Event"); deleteBtn.setForeground(Color.RED);

        createBtn.addActionListener(e -> {
            JTextField titleF = new JTextField(); JTextArea descF = new JTextArea(3, 20); JTextField capF = new JTextField();
            DateTimePicker dtPicker = new DateTimePicker(null, null);
            Object[] msg = {"Title:", titleF, "Description:", new JScrollPane(descF), dtPicker.getPanel(), "Capacity:", capF};
            
            // Validation Loop
            while (true) {
                if (JOptionPane.showConfirmDialog(frame, msg, "Create New Event", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (titleF.getText().trim().isEmpty() || capF.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    try {
                        String res = o.createEvent(titleF.getText(), descF.getText(), dtPicker.getDateString(), dtPicker.getTimeString(), Integer.parseInt(capF.getText()));
                        JOptionPane.showMessageDialog(frame, res);
                        if (res.startsWith("Success")) { refreshTable.run(); break; }
                    } catch (NumberFormatException ex) { 
                        JOptionPane.showMessageDialog(frame, "Capacity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE); 
                    }
                } else break; // User hit cancel
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select an event to edit."); return; }
            
            JTextField titleF = new JTextField((String) table.getValueAt(row, 1));
            JTextArea descF = new JTextArea(table.getValueAt(row, 2) != null ? (String) table.getValueAt(row, 2) : "", 3, 20);
            JTextField capF = new JTextField(String.valueOf(table.getValueAt(row, 5)));
            DateTimePicker dtPicker = new DateTimePicker((String) table.getValueAt(row, 3), (String) table.getValueAt(row, 4));
            Object[] msg = {"Title:", titleF, "Description:", new JScrollPane(descF), dtPicker.getPanel(), "Capacity:", capF};
            
            // Validation Loop
            while (true) {
                if (JOptionPane.showConfirmDialog(frame, msg, "Edit Event", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (titleF.getText().trim().isEmpty() || capF.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    try {
                        String res = o.updateEvent((int)table.getValueAt(row, 0), titleF.getText(), descF.getText(), dtPicker.getDateString(), dtPicker.getTimeString(), Integer.parseInt(capF.getText()));
                        JOptionPane.showMessageDialog(frame, res);
                        if (res.startsWith("Success")) { refreshTable.run(); break; }
                    } catch (NumberFormatException ex) { 
                        JOptionPane.showMessageDialog(frame, "Capacity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE); 
                    }
                } else break;
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1 && JOptionPane.showConfirmDialog(frame, "Delete this event?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(frame, o.deleteEvent((int)table.getValueAt(row, 0))); refreshTable.run();
            }
        });

        bot.add(createBtn); bot.add(editBtn); bot.add(deleteBtn);
        panel.add(new JScrollPane(table), BorderLayout.CENTER); panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    private void openEventReport(Organizer o, int eventId, String title, String date, String time, int capacity) {
        JDialog dialog = new JDialog(frame, "Management: " + title, true);
        dialog.setSize(450, 400); dialog.setLocationRelativeTo(frame); dialog.setLayout(new BorderLayout(10, 10));

        JPanel details = new JPanel(new GridLayout(3, 1)); details.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        details.add(new JLabel("<html><b>Title:</b> " + title + "</html>")); 
        details.add(new JLabel("<html><b>Date & Time:</b> " + date + " @ " + time + "</html>")); 
        details.add(new JLabel("<html><b>Capacity:</b> " + capacity + "</html>"));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        Runnable refreshList = () -> { listModel.clear(); for (String p : o.generateReport(eventId)) listModel.addElement(p); };
        refreshList.run();

        JList<String> partList = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane(partList); scroll.setBorder(BorderFactory.createTitledBorder("Participants"));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton inviteBtn = new JButton("Invite Participant"); JButton removeBtn = new JButton("Remove Selected");

        inviteBtn.addActionListener(e -> {
            String uname = JOptionPane.showInputDialog(dialog, "Username to invite:");
            if (uname != null && !uname.trim().isEmpty()) JOptionPane.showMessageDialog(dialog, o.inviteParticipant(eventId, uname.trim()));
        });

        removeBtn.addActionListener(e -> {
            String sel = partList.getSelectedValue();
            if (sel != null) {
                String uname = sel.contains(" (") ? sel.substring(0, sel.indexOf(" (")) : sel;
                if (JOptionPane.showConfirmDialog(dialog, "Remove " + uname + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(dialog, o.forceParticipantUpdate(eventId, uname, false)); refreshList.run(); 
                }
            } else JOptionPane.showMessageDialog(dialog, "Select a participant.");
        });

        actions.add(inviteBtn); actions.add(removeBtn);
        dialog.add(details, BorderLayout.NORTH); dialog.add(scroll, BorderLayout.CENTER); dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openEventReport(Admin a, int eventId, String title, String date, String time, int capacity) {
        JDialog dialog = new JDialog(frame, "Admin Management: " + title, true);
        dialog.setSize(450, 400); dialog.setLocationRelativeTo(frame); dialog.setLayout(new BorderLayout(10, 10));

        JPanel details = new JPanel(new GridLayout(3, 1)); details.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        details.add(new JLabel("<html><b>Title:</b> " + title + "</html>")); 
        details.add(new JLabel("<html><b>Date & Time:</b> " + date + " @ " + time + "</html>")); 
        details.add(new JLabel("<html><b>Capacity:</b> " + capacity + "</html>"));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        Runnable refreshList = () -> { listModel.clear(); for (String p : a.generateReport(eventId)) listModel.addElement(p); };
        refreshList.run();

        JList<String> partList = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane(partList); scroll.setBorder(BorderFactory.createTitledBorder("Participants"));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton inviteBtn = new JButton("Invite Participant"); JButton removeBtn = new JButton("Remove Selected");

        inviteBtn.addActionListener(e -> {
            String uname = JOptionPane.showInputDialog(dialog, "Username to invite:");
            if (uname != null && !uname.trim().isEmpty()) JOptionPane.showMessageDialog(dialog, a.inviteParticipant(eventId, uname.trim()));
        });

        removeBtn.addActionListener(e -> {
            String sel = partList.getSelectedValue();
            if (sel != null) {
                String uname = sel.contains(" (") ? sel.substring(0, sel.indexOf(" (")) : sel;
                if (JOptionPane.showConfirmDialog(dialog, "Remove " + uname + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(dialog, a.forceParticipantUpdate(eventId, uname, false)); refreshList.run(); 
                }
            } else JOptionPane.showMessageDialog(dialog, "Select a participant.");
        });

        actions.add(inviteBtn); actions.add(removeBtn);
        dialog.add(details, BorderLayout.NORTH); dialog.add(scroll, BorderLayout.CENTER); dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createAdminDashboard(Admin a) {
        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.addTab("Event Management", createAdminEventsPanel(a));
        adminTabs.addTab("User Management", createAdminUsersPanel(a));
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(adminTabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createAdminEventsPanel(Admin a) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Title", "Description", "Date", "Time", "Capacity", "Organizer"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        
        table.setToolTipText("Double-click an event to view reports & participants.");
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { 
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        openEventReport(a, (int)table.getValueAt(row, 0), (String)table.getValueAt(row, 1), (String)table.getValueAt(row, 3), (String)table.getValueAt(row, 4), (int)table.getValueAt(row, 5));
                    }
                }
            }
        });

        Runnable refreshTable = () -> {
            model.setRowCount(0);
            for (Event ev : a.viewAllEvents()) model.addRow(new Object[]{ev.getId(), ev.getTitle(), ev.getDescription(), ev.getDate(), ev.getTime(), ev.getCapacity(), ev.getOrganizerName()});
        };
        refreshTable.run();

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton createBtn = new JButton("Create Event");
        JButton editBtn = new JButton("Edit Event");
        JButton delEvtBtn = new JButton("Delete Event"); delEvtBtn.setForeground(Color.RED);

        createBtn.addActionListener(e -> {
            JTextField titleF = new JTextField(); JTextArea descF = new JTextArea(3, 20); JTextField capF = new JTextField();
            DateTimePicker dtPicker = new DateTimePicker(null, null);
            Object[] msg = {"Title:", titleF, "Description:", new JScrollPane(descF), dtPicker.getPanel(), "Capacity:", capF};
            
            // Validation Loop
            while (true) {
                if (JOptionPane.showConfirmDialog(frame, msg, "Create Event", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (titleF.getText().trim().isEmpty() || capF.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    try {
                        String res = a.createEvent(titleF.getText(), descF.getText(), dtPicker.getDateString(), dtPicker.getTimeString(), Integer.parseInt(capF.getText()));
                        JOptionPane.showMessageDialog(frame, res);
                        if (res.startsWith("Success")) { refreshTable.run(); break; }
                    } catch (NumberFormatException ex) { 
                        JOptionPane.showMessageDialog(frame, "Capacity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE); 
                    }
                } else break; // User hit cancel
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(frame, "Select an event to edit."); return; }
            
            JTextField titleF = new JTextField((String) table.getValueAt(row, 1));
            JTextArea descF = new JTextArea(table.getValueAt(row, 2) != null ? (String) table.getValueAt(row, 2) : "", 3, 20);
            JTextField capF = new JTextField(String.valueOf(table.getValueAt(row, 5)));
            DateTimePicker dtPicker = new DateTimePicker((String) table.getValueAt(row, 3), (String) table.getValueAt(row, 4));
            Object[] msg = {"Title:", titleF, "Description:", new JScrollPane(descF), dtPicker.getPanel(), "Capacity:", capF};
            
            // Validation Loop
            while (true) {
                if (JOptionPane.showConfirmDialog(frame, msg, "Edit Event", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (titleF.getText().trim().isEmpty() || capF.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    try {
                        String res = a.updateAnyEvent((int)table.getValueAt(row, 0), titleF.getText(), descF.getText(), dtPicker.getDateString(), dtPicker.getTimeString(), Integer.parseInt(capF.getText()));
                        JOptionPane.showMessageDialog(frame, res);
                        if (res.startsWith("Success")) { refreshTable.run(); break; }
                    } catch (NumberFormatException ex) { 
                        JOptionPane.showMessageDialog(frame, "Capacity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE); 
                    }
                } else break;
            }
        });

        delEvtBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1 && JOptionPane.showConfirmDialog(frame, "Terminate event?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(frame, a.deleteAnyEvent((int) table.getValueAt(row, 0))); refreshTable.run();
            }
        });

        bot.add(createBtn); bot.add(editBtn); bot.add(delEvtBtn); 
        panel.add(new JScrollPane(table), BorderLayout.CENTER); panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAdminUsersPanel(Admin a) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Username", "Role", "Full Name"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        
        Runnable refreshTable = () -> {
            model.setRowCount(0);
            for (String[] u : a.viewAllUsers()) model.addRow(u);
        };
        refreshTable.run();

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton createBtn = new JButton("Create User");
        JButton promoteBtn = new JButton("Promote to Organizer");
        JButton demoteBtn = new JButton("Demote to Participant");
        JButton purgeBtn = new JButton("Purge Account"); purgeBtn.setForeground(Color.RED);

        createBtn.addActionListener(e -> {
            JTextField userF = new JTextField(); JPasswordField passF = new JPasswordField();
            Object[] msg = {"Username:", userF, "Password:", passF};
            
            // Validation Loop
            while(true) {
                if (JOptionPane.showConfirmDialog(frame, msg, "Create Participant", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    if (userF.getText().trim().isEmpty() || new String(passF.getPassword()).trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    String res = User.register(userF.getText(), new String(passF.getPassword()), "Participant");
                    JOptionPane.showMessageDialog(frame, res);
                    if (res.startsWith("Success")) { refreshTable.run(); break; }
                } else break; // User canceled
            }
        });

        promoteBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if(r != -1) { JOptionPane.showMessageDialog(frame, a.changeUserRole((String)table.getValueAt(r, 1), "Organizer")); refreshTable.run(); }
        });

        demoteBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if(r != -1) { JOptionPane.showMessageDialog(frame, a.changeUserRole((String)table.getValueAt(r, 1), "Participant")); refreshTable.run(); }
        });

        purgeBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1 && JOptionPane.showConfirmDialog(frame, "Purge this account permanently?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(frame, a.deleteAccount((String)table.getValueAt(r, 1))); refreshTable.run();
            }
        });

        bot.add(createBtn); bot.add(promoteBtn); bot.add(demoteBtn); bot.add(purgeBtn);
        panel.add(new JScrollPane(table), BorderLayout.CENTER); panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new MainGUI()); }
}