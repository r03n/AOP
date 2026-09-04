package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseManager;

public class Participant extends User {

    public Participant(int id, String username) {
        super(id, username, "Participant");
    }

    @Override
    public String getRoleDescription() {
        return "Can browse and register for events.";
    }

    public List<Event> browseEvents() {
        List<Event> events = new ArrayList<>();
        // Show the organizer's full name (falling back to username) instead of their raw ID
        String sql = "SELECT e.id, e.title, e.description, e.event_date, e.event_time, e.capacity, "
                + "COALESCE(NULLIF(u.full_name, ''), u.username) AS organizer "
                + "FROM Events e LEFT JOIN Users u ON e.organizer_id = u.id";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                events.add(new Event(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                        rs.getString("event_date"), rs.getString("event_time"), rs.getInt("capacity"),
                        rs.getString("organizer")));
            }
        } catch (SQLException e) {
            System.err.println("Browse events error: " + e.getMessage());
        }
        return events;
    }

    public String registerForEvent(int eventId) {
        // Included from the first snippet: Profile completion validation
        if (fullName == null || fullName.trim().isEmpty()
                || age <= 0
                || department == null || department.trim().isEmpty()
                || yearLevel == null || yearLevel.trim().isEmpty()) {
            return "Error: Please complete your profile before registering for an event.";
        }

        String capCheckSql = "SELECT capacity, (SELECT COUNT(*) FROM Registrations WHERE event_id = ?) AS current_count FROM Events WHERE id = ?";
        String checkSql = "SELECT * FROM Registrations WHERE participant_id = ? AND event_id = ?";
        String insertSql = "INSERT INTO Registrations(participant_id, event_id, status) VALUES(?, ?, 'Confirmed')";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement capStmt = conn.prepareStatement(capCheckSql);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Check Capacity First
            capStmt.setInt(1, eventId);
            capStmt.setInt(2, eventId);
            try (ResultSet rsCap = capStmt.executeQuery()) {
                if (rsCap.next()) {
                    if (rsCap.getInt("current_count") >= rsCap.getInt("capacity")) {
                        return "Error: Event has reached its maximum capacity.";
                    }
                } else {
                    return "Error: Event does not exist.";
                }
            }

            // Check if already registered
            checkStmt.setInt(1, this.id);
            checkStmt.setInt(2, eventId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return "Error: You are already registered for this event.";
                }
            }

            // Confirm Registration
            insertStmt.setInt(1, this.id);
            insertStmt.setInt(2, eventId);
            insertStmt.executeUpdate();
            return "Success: Registered for Event ID " + eventId;

        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return "Error processing registration.";
        }
    }

    // Included from the second snippet: Unregister function
    public String unregisterFromEvent(int eventId) {
        String checkSql = "SELECT * FROM Registrations WHERE participant_id = ? AND event_id = ?";
        String deleteSql = "DELETE FROM Registrations WHERE participant_id = ? AND event_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

            // Check if the participant is registered
            checkStmt.setInt(1, this.id);
            checkStmt.setInt(2, eventId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    return "Error: You are not registered for this event.";
                }
            }

            // Remove the registration
            deleteStmt.setInt(1, this.id);
            deleteStmt.setInt(2, eventId);
            deleteStmt.executeUpdate();

            return "Success: Unregistered from Event ID " + eventId;

        } catch (SQLException e) {
            System.err.println("Unregistration error: " + e.getMessage());
            return "Error processing unregistration.";
        }
    }

    public List<String> checkNotifications() {
        List<String> notifs = new ArrayList<>();
        String sql = "SELECT e.title FROM Invites i JOIN Events e ON i.event_id = e.id WHERE i.participant_id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifs.add("You are invited to: " + rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Check notifications error: " + e.getMessage());
        }
        return notifs;
    }
}