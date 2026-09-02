import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Participant extends User {
    public Participant(int id, String username) { super(id, username, "Participant"); }

    public List<Event> browseEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM Events";
        try (Connection conn = DatabaseManager.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // ADDED: rs.getString("description") to match the updated Event constructor
                events.add(new Event(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getString("event_date"), rs.getInt("capacity"), rs.getInt("organizer_id")));
            }
        } catch (SQLException e) {}
        return events;
    }

    public String registerForEvent(int eventId) {
        // Query to check capacity vs current registration count
        String capCheckSql = "SELECT capacity, (SELECT COUNT(*) FROM Registrations WHERE event_id = ?) AS current_count FROM Events WHERE id = ?";
        String checkSql = "SELECT * FROM Registrations WHERE participant_id = ? AND event_id = ?";
        String insertSql = "INSERT INTO Registrations(participant_id, event_id, status) VALUES(?, ?, 'Confirmed')";
        
        try (Connection conn = DatabaseManager.connect(); 
             PreparedStatement capStmt = conn.prepareStatement(capCheckSql);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql); 
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            
            // 1. Check Capacity Limit
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

            // 2. Check if already registered
            checkStmt.setInt(1, this.id); checkStmt.setInt(2, eventId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) return "Error: You are already registered for this event.";
            }

            // 3. Process Registration
            insertStmt.setInt(1, this.id); insertStmt.setInt(2, eventId);
            insertStmt.executeUpdate();
            return "Success: Registered for Event ID " + eventId;
            
        } catch (SQLException e) { 
            return "Error processing registration."; 
        }
    }
    
    public List<String> checkNotifications() {
        List<String> notifs = new ArrayList<>();
        String sql = "SELECT e.title FROM Invites i JOIN Events e ON i.event_id = e.id WHERE i.participant_id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) notifs.add("You are invited to: " + rs.getString("title"));
            }
        } catch (SQLException e) {}
        return notifs;
    }
}