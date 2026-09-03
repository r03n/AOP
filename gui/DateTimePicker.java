package gui;

import javax.swing.*;
import java.awt.*;

public class DateTimePicker {
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