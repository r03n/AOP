package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseManager;

public class Admin extends User {

    public Admin(int id, String username) {
        super(id, username, "Admin");
    }

    @Override
    public String getRoleDescription() {
        return "Has full system access and user management.";
    }

    // --- EVENT MANAGEMENT (Global Access) ---
    public String createEvent(String title, String description, String date, String time, int capacity) {
        String sql = "INSERT INTO Events(title, description, event_date, event_time, capacity, organizer_id) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, date);
            pstmt.setString(4, time);
            pstmt.setInt(5, capacity);
            pstmt.setInt(6, this.id);
            pstmt.executeUpdate();
            return "Success: Event Created!";
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return "Error: Failed to create event.";
        }
    }

    public String updateAnyEvent(int eventId, String title, String description, String date, String time,
            int capacity) {
        if (capacity <= 0) {
            return "Error: Capacity must be greater than zero.";
        }
        String sql = "UPDATE Events SET title = ?, description = ?, event_date = ?, event_time = ?, capacity = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Prevent shrinking capacity below the number of participants already registered
            try (PreparedStatement countStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Registrations WHERE event_id = ?")) {
                countStmt.setInt(1, eventId);
                try (ResultSet rsCount = countStmt.executeQuery()) {
                    if (rsCount.next() && rsCount.getInt(1) > capacity) {
                        return "Error: Capacity cannot be lower than the " + rsCount.getInt(1)
                                + " participant(s) already registered.";
                    }
                }
            }
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, date);
            pstmt.setString(4, time);
            pstmt.setInt(5, capacity);
            pstmt.setInt(6, eventId);
            if (pstmt.executeUpdate() > 0)
                return "Success: Event updated!";
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        return "Error: Failed to update event.";
    }

    public List<Event> viewAllEvents() {
        List<Event> events = new ArrayList<>();
        // Prefer the organizer's full name; fall back to username if the profile isn't filled out
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
            System.err.println("View all events error: " + e.getMessage());
        }
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
            if (evStmt.executeUpdate() > 0)
                return "Success: Event terminated.";
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        return "Error terminating event.";
    }

    // --- USER MANAGEMENT ---
    public List<String[]> viewAllUsers() {
        List<String[]> users = new ArrayList<>();
        String sql = "SELECT id, username, role, full_name FROM Users WHERE role != 'Admin'";
        try (Connection conn = DatabaseManager.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                users.add(new String[] { String.valueOf(rs.getInt("id")), rs.getString("username"),
                        rs.getString("role"), rs.getString("full_name") });
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        return users;
    }

    // Show full details of a participant/user to Admin (not just the summary table row)
    public String[] viewUserProfile(String targetUsername) {
        String sql = "SELECT username, role, full_name, age, department, year_level FROM Users WHERE username = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, targetUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new String[] {
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("full_name") != null ? rs.getString("full_name") : "Not set",
                            rs.getInt("age") > 0 ? String.valueOf(rs.getInt("age")) : "Not set",
                            rs.getString("department") != null ? rs.getString("department") : "Not set",
                            rs.getString("year_level") != null ? rs.getString("year_level") : "Not set"
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("View user profile error: " + e.getMessage());
        }
        return null;
    }

    public String changeUserRole(String targetUsername, String newRole) {
        String sql = "UPDATE Users SET role = ? WHERE username = ? AND role != 'Admin'";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setString(2, targetUsername);
            if (pstmt.executeUpdate() > 0)
                return "Success: " + targetUsername + " is now " + newRole;
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        return "Error updating role.";
    }

    public String deleteAccount(String targetUsername) {
        String delUser = "DELETE FROM Users WHERE username = ? AND role != 'Admin'";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(delUser)) {
            pstmt.setString(1, targetUsername);
            if (pstmt.executeUpdate() > 0)
                return "Success: Account purged.";
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        return "Error deleting account.";
    }

    // --- PARTICIPANT MANAGEMENT (Admin Overrides) ---
    public List<String> generateReport(int eventId) {
        List<String> report = new ArrayList<>();
        String userSql = "SELECT u.full_name, r.status FROM Registrations r JOIN Users u ON r.participant_id = u.id WHERE r.event_id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement userStmt = conn.prepareStatement(userSql)) {
            userStmt.setInt(1, eventId);
            try (ResultSet rs = userStmt.executeQuery()) {
                while (rs.next())
                    report.add(rs.getString("full_name") + " (" + rs.getString("status") + ")");
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        return report;
    }

    // Show invited participants (invited but not yet registered) in the detailed event view
    public List<String> getInvitedParticipants(int eventId) {
        List<String> invited = new ArrayList<>();
        String sql = "SELECT COALESCE(NULLIF(u.full_name, ''), u.username) AS name "
                + "FROM Invites i JOIN Users u ON i.participant_id = u.id "
                + "WHERE i.event_id = ? AND u.id NOT IN "
                + "(SELECT participant_id FROM Registrations WHERE event_id = ?)";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    invited.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Get invited participants error: " + e.getMessage());
        }
        return invited;
    }

    public String forceParticipantUpdate(int eventId, String fullName, boolean isAdding) {
        String getUserId = "SELECT id FROM Users WHERE full_name = ? AND role = 'Participant'";

        try (Connection conn = DatabaseManager.connect();
                PreparedStatement getStmt = conn.prepareStatement(getUserId)) {

            getStmt.setString(1, fullName);

            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    int pId = rs.getInt("id");

                    if (isAdding) {
                        String capCheckSql = "SELECT capacity, " +
                                "(SELECT COUNT(*) FROM Registrations WHERE event_id = ?) AS current_count " +
                                "FROM Events WHERE id = ?";

                        try (PreparedStatement capStmt = conn.prepareStatement(capCheckSql)) {
                            capStmt.setInt(1, eventId);
                            capStmt.setInt(2, eventId);

                            try (ResultSet rsCap = capStmt.executeQuery()) {
                                if (rsCap.next() && rsCap.getInt("current_count") >= rsCap.getInt("capacity")) {
                                    return "Error: Event is at maximum capacity.";
                                }
                            }
                        }

                        try (PreparedStatement ins = conn.prepareStatement(
                                "INSERT INTO Registrations(participant_id, event_id, status) VALUES(?, ?, 'Force Added')")) {

                            ins.setInt(1, pId);
                            ins.setInt(2, eventId);
                            ins.executeUpdate();

                            return "Success: Added " + fullName;
                        } catch (SQLException e) {
                            System.err.println("Database error: " + e.getMessage());
                            return "Error: User is likely already registered.";
                        }

                    } else {
                        try (PreparedStatement del = conn.prepareStatement(
                                "DELETE FROM Registrations WHERE participant_id = ? AND event_id = ?")) {

                            del.setInt(1, pId);
                            del.setInt(2, eventId);

                            if (del.executeUpdate() > 0) {
                                return "Success: Removed " + fullName;
                            }

                            return "Error: Not registered.";
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }

        return "Error: User not found or update failed.";
    }

    public String inviteParticipant(int eventId, String username) {
        String sql = "INSERT INTO Invites(event_id, participant_id) SELECT ?, id FROM Users WHERE username = ? AND role = 'Participant'";
        // FIXED: Added "conn." before prepareStatement(sql)
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setString(2, username);
            if (pstmt.executeUpdate() > 0)
                return "Success: Invited " + username;
            return "Error: Participant not found.";
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return "Error: Could not invite.";
        }
    }
}