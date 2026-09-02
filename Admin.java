import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    public Admin(int id, String username) { super(id, username, "Admin"); }

    // --- EVENT MANAGEMENT (Organizer equivalent but global) ---
    public String createEvent(String title, String desc, String date, int capacity) {
        String sql = "INSERT INTO Events(title, description, event_date, capacity, organizer_id) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title); pstmt.setString(2, desc); pstmt.setString(3, date); 
            pstmt.setInt(4, capacity); pstmt.setInt(5, this.id);
            pstmt.executeUpdate();
            return "Success: Event Created!";
        } catch (SQLException e) { return "Error: Failed to create event."; }
    }

    public String updateAnyEvent(int eventId, String title, String desc, String date, int capacity) {
        String sql = "UPDATE Events SET title = ?, description = ?, event_date = ?, capacity = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title); pstmt.setString(2, desc);
            pstmt.setString(3, date); pstmt.setInt(4, capacity); pstmt.setInt(5, eventId);
            if (pstmt.executeUpdate() > 0) return "Success: Event updated!";
        } catch (SQLException e) {}
        return "Error: Failed to update event.";
    }

    public List<Event> viewAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.id, e.title, e.description, e.event_date, e.capacity, u.username as organizer FROM Events e LEFT JOIN Users u ON e.organizer_id = u.id";
        try (Connection conn = DatabaseManager.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) events.add(new Event(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getString("event_date"), rs.getInt("capacity"), rs.getString("organizer")));
        } catch (SQLException e) {}
        return events;
    }

    public String deleteAnyEvent(int eventId) {
        String delEvent = "DELETE FROM Events WHERE id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement evStmt = conn.prepareStatement(delEvent)) {
            try (Statement s = conn.createStatement()) {
                s.execute("DELETE FROM Registrations WHERE event_id = " + eventId);
                s.execute("DELETE FROM Invites WHERE event_id = " + eventId);
            }
            evStmt.setInt(1, eventId); 
            if (evStmt.executeUpdate() > 0) return "Success: Event terminated.";
        } catch (SQLException e) {}
        return "Error terminating event.";
    }

    // --- USER MANAGEMENT ---
    public List<String[]> viewAllUsers() {
        List<String[]> users = new ArrayList<>();
        String sql = "SELECT id, username, role, full_name FROM Users WHERE role != 'Admin'";
        try (Connection conn = DatabaseManager.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new String[]{ String.valueOf(rs.getInt("id")), rs.getString("username"), rs.getString("role"), rs.getString("full_name") });
            }
        } catch (SQLException e) {}
        return users;
    }

    public String changeUserRole(String targetUsername, String newRole) {
        String sql = "UPDATE Users SET role = ? WHERE username = ? AND role != 'Admin'";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole); pstmt.setString(2, targetUsername);
            if (pstmt.executeUpdate() > 0) return "Success: " + targetUsername + " is now " + newRole;
        } catch (SQLException e) {}
        return "Error updating role.";
    }

    public String deleteAccount(String targetUsername) {
        String delUser = "DELETE FROM Users WHERE username = ? AND role != 'Admin'";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(delUser)) {
            pstmt.setString(1, targetUsername);
            if (pstmt.executeUpdate() > 0) return "Success: Account purged.";
        } catch (SQLException e) {}
        return "Error deleting account.";
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
                        // --- ENFORCE CAPACITY LIMIT ---
                        String capCheckSql = "SELECT capacity, (SELECT COUNT(*) FROM Registrations WHERE event_id = ?) AS current_count FROM Events WHERE id = ?";
                        try (PreparedStatement capStmt = conn.prepareStatement(capCheckSql)) {
                            capStmt.setInt(1, eventId); capStmt.setInt(2, eventId);
                            try (ResultSet rsCap = capStmt.executeQuery()) {
                                if (rsCap.next() && rsCap.getInt("current_count") >= rsCap.getInt("capacity")) {
                                    return "Error: Event is at maximum capacity.";
                                }
                            }
                        }
                        
                        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO Registrations(participant_id, event_id, status) VALUES(?, ?, 'Force Added')")) {
                            ins.setInt(1, pId); ins.setInt(2, eventId); ins.executeUpdate();
                            return "Success: Added " + username;
                        } catch (SQLException e) {
                            return "Error: User is likely already registered."; 
                        }
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