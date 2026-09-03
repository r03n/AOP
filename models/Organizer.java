package models;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseManager;

public class Organizer extends User {
    
    public Organizer(int id, String username) { 
        super(id, username, "Organizer"); 
    }

    @Override
    public String getRoleDescription() { 
        return "Can create, manage, and delete personal events."; 
    }

    public String createEvent(String title, String description, String date, String time, int capacity) {
        String sql = "INSERT INTO Events(title, description, event_date, event_time, capacity, organizer_id) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title); pstmt.setString(2, description); 
            pstmt.setString(3, date); pstmt.setString(4, time); 
            pstmt.setInt(5, capacity); pstmt.setInt(6, this.id);
            pstmt.executeUpdate();
            return "Success: Event Created!";
        } catch (SQLException e) { return "Error: Failed to create event."; }
    }

    public String updateEvent(int eventId, String title, String description, String date, String time, int capacity) {
        String sql = "UPDATE Events SET title = ?, description = ?, event_date = ?, event_time = ?, capacity = ? WHERE id = ? AND organizer_id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title); pstmt.setString(2, description); 
            pstmt.setString(3, date); pstmt.setString(4, time);
            pstmt.setInt(5, capacity); pstmt.setInt(6, eventId); pstmt.setInt(7, this.id); 
            if (pstmt.executeUpdate() > 0) return "Success: Event updated!";
            return "Error: Event not found or unauthorized.";
        } catch (SQLException e) { return "Error: Failed to update event."; }
    }

    public List<Event> viewMyEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM Events WHERE organizer_id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(new Event(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getString("event_date"), rs.getString("event_time"), rs.getInt("capacity"), this.id));
                }
            }
        } catch (SQLException e) {}
        return events;
    }
    
    public String deleteEvent(int eventId) {
        String delRegs = "DELETE FROM Registrations WHERE event_id = ?";
        String delInvites = "DELETE FROM Invites WHERE event_id = ?";
        String delEvent = "DELETE FROM Events WHERE id = ? AND organizer_id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement regStmt = conn.prepareStatement(delRegs); PreparedStatement invStmt = conn.prepareStatement(delInvites); PreparedStatement evStmt = conn.prepareStatement(delEvent)) {
            regStmt.setInt(1, eventId); regStmt.executeUpdate();
            invStmt.setInt(1, eventId); invStmt.executeUpdate();
            evStmt.setInt(1, eventId); evStmt.setInt(2, this.id);
            if (evStmt.executeUpdate() > 0) return "Success: Event deleted.";
            return "Error: Event not found or unauthorized.";
        } catch (SQLException e) { return "Error deleting event."; }
    }

    public List<String> generateReport(int eventId) {
        List<String> report = new ArrayList<>();
        String userSql = "SELECT u.username, r.status FROM Registrations r JOIN Users u ON r.participant_id = u.id WHERE r.event_id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement userStmt = conn.prepareStatement(userSql)) {
            userStmt.setInt(1, eventId);
            try (ResultSet rs = userStmt.executeQuery()) {
                while (rs.next()) report.add(rs.getString("username") + " (" + rs.getString("status") + ")");
            }
        } catch (SQLException e) {}
        return report;
    }

    public String forceParticipantUpdate(int eventId, String username, boolean isAdding) {
        String getUserId = "SELECT id FROM Users WHERE username = ? AND role = 'Participant'";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement getStmt = conn.prepareStatement(getUserId)) {
            getStmt.setString(1, username);
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    int pId = rs.getInt("id");
                    if (isAdding) {
                        String capCheckSql = "SELECT capacity, (SELECT COUNT(*) FROM Registrations WHERE event_id = ?) AS current_count FROM Events WHERE id = ?";
                        try (PreparedStatement capStmt = conn.prepareStatement(capCheckSql)) {
                            capStmt.setInt(1, eventId); capStmt.setInt(2, eventId);
                            try (ResultSet rsCap = capStmt.executeQuery()) {
                                if (rsCap.next() && rsCap.getInt("current_count") >= rsCap.getInt("capacity")) return "Error: Event is at maximum capacity.";
                            }
                        }
                        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO Registrations(participant_id, event_id, status) VALUES(?, ?, 'Force Added')")) {
                            ins.setInt(1, pId); ins.setInt(2, eventId); ins.executeUpdate();
                            return "Success: Added " + username;
                        } catch (SQLException e) { return "Error: User is likely already registered."; }
                    } else {
                        try (PreparedStatement del = conn.prepareStatement("DELETE FROM Registrations WHERE participant_id = ? AND event_id = ?")) {
                            del.setInt(1, pId); del.setInt(2, eventId);
                            if (del.executeUpdate() > 0) return "Success: Removed " + username;
                            return "Error: Not registered.";
                        }
                    }
                }
            }
        } catch (SQLException e) {}
        return "Error: User not found or update failed.";
    }

    public String inviteParticipant(int eventId, String username) {
        String sql = "INSERT INTO Invites(event_id, participant_id) SELECT ?, id FROM Users WHERE username = ? AND role = 'Participant'";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId); pstmt.setString(2, username);
            if (pstmt.executeUpdate() > 0) return "Success: Invited " + username;
            return "Error: Participant not found.";
        } catch (SQLException e) { return "Error: Could not invite."; }
    }
}