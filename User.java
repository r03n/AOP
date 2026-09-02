import java.sql.*;

public class User {
    protected int id;
    protected String username;
    protected String role;
    
    // --- NEW PROFILE FIELDS ---
    protected String fullName;
    protected int age;
    protected String department;
    protected String yearLevel;
    
    public User() {}
    
    public User(int id, String username, String role) {
        this.id = id; 
        this.username = username; 
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    
    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public String getDepartment() { return department; }
    public String getYearLevel() { return yearLevel; }

    public static String register(String username, String password, String role) {
        String sql = "INSERT INTO Users(username, password, role) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, username); 
            pstmt.setString(2, password); 
            pstmt.setString(3, role);
            
            pstmt.executeUpdate();
            return "Success: User registered.";
            
        } catch (SQLException e) {
            return "Error: Username may already exist.";
        }
    }
    
    // --- UPDATED: PULLS PROFILE DATA ON LOGIN ---
    public static User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, username); 
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id"); 
                    String role = rs.getString("role");
                    
                    User loggedIn = null;
                    if (role.equalsIgnoreCase("Admin")) {
                        loggedIn = new Admin(id, username);
                    } else if (role.equalsIgnoreCase("Organizer")) {
                        loggedIn = new Organizer(id, username);
                    } else if (role.equalsIgnoreCase("Participant")) {
                        loggedIn = new Participant(id, username);
                    } else {
                        loggedIn = new User(id, username, role);
                    }
                    
                    // Populate Profile Fields from the database
                    loggedIn.fullName = rs.getString("full_name");
                    loggedIn.age = rs.getInt("age");
                    loggedIn.department = rs.getString("department");
                    loggedIn.yearLevel = rs.getString("year_level");
                    
                    return loggedIn;
                }
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // --- NEW: UPDATE PROFILE LOGIC ---
    public String updateProfile(String fName, int newAge, String dept, String yLevel) {
        String sql = "UPDATE Users SET full_name = ?, age = ?, department = ?, year_level = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, fName);
            pstmt.setInt(2, newAge);
            pstmt.setString(3, dept);
            pstmt.setString(4, yLevel);
            pstmt.setInt(5, this.id);
            
            pstmt.executeUpdate();
            
            // Update the object's variables so the GUI reflects the change immediately
            this.fullName = fName; 
            this.age = newAge; 
            this.department = dept; 
            this.yearLevel = yLevel;
            
            return "Profile updated successfully!";
            
        } catch (SQLException e) {
            return "Error updating profile.";
        }
    }
}