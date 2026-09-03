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
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = { "ID", "Title", "Description", "Date", "Time", "Capacity" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Event e : p.browseEvents())
            model.addRow(new Object[] { e.getId(), e.getTitle(), e.getDescription(), e.getDate(), e.getTime(),
                    e.getCapacity() });

        JButton regBtn = new JButton("Register for Selected Event");
        regBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(app.getFrame(), "Please select an event.");
            } else {
                JOptionPane.showMessageDialog(
                        app.getFrame(),
                        p.registerForEvent((int) table.getValueAt(row, 0)));
            }
        });

        JButton unregBtn = new JButton("Unregister from Selected Event");
        unregBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(app.getFrame(), "Please select an event.");
            } else {
                JOptionPane.showMessageDialog(
                        app.getFrame(),
                        p.unregisterFromEvent((int) table.getValueAt(row, 0)));
            }
        });

        JPanel notifPanel = new JPanel(new BorderLayout());
        notifPanel.setBorder(BorderFactory.createTitledBorder("Notifications & Invites"));
        JTextArea notifs = new JTextArea(5, 20);
        notifs.setEditable(false);
        for (String n : p.checkNotifications())
            notifs.append(n + "\n");
        notifPanel.add(new JScrollPane(notifs));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(regBtn);
        buttonPanel.add(unregBtn);

        JPanel bot = new JPanel(new BorderLayout(0, 10));
        bot.add(buttonPanel, BorderLayout.NORTH);
        bot.add(notifPanel, BorderLayout.CENTER);

        panel.add(bot, BorderLayout.SOUTH);
    }

    public JPanel getPanel() {
        return panel;
    }
}