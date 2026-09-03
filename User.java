import java.sql.*;

public abstract class User {
    // Encapsulation: Variables are protected/private, accessed via getters
    protected int id;
    protected String username;
    protected String role;
    protected String fullName;
    protected int age;
    protected String department;
    protected String yearLevel;

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // ABSTRACTION & POLYMORPHISM: Forces all subclasses to implement their own version
    public abstract String getRoleDescription();

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public String getDepartment() { return department; }
    public String getYearLevel() { return yearLevel; }
    
    public void setProfileData(String fullName, int age, String department, String yearLevel) {
        this.fullName = fullName;
        this.age = age;
        this.department = department;
        this.yearLevel = yearLevel;
    }

    // Factory Method for Login
    public static User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        // Exception Handling: try-with-resources safely closes DB connections
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username); pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String role = rs.getString("role");
                    User u = null;
                    
                    // Instantiating the specific subclass based on role
                    if (role.equals("Admin")) u = new Admin(id, username);
                    else if (role.equals("Organizer")) u = new Organizer(id, username);
                    else if (role.equals("Participant")) u = new Participant(id, username);
                    
                    if (u != null) u.setProfileData(rs.getString("full_name"), rs.getInt("age"), rs.getString("department"), rs.getString("year_level"));
                    return u;
                }
            }
        } catch (SQLException e) {} // Safe failure
        return null;
    }

    public static String register(String username, String password, String role) {
        String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username); pstmt.setString(2, password); pstmt.setString(3, role);
            pstmt.executeUpdate();
            return "Success: Account created!";
        } catch (SQLException e) { return "Error: Username might already exist."; }
    }

    public String updateProfile(String fullName, int age, String department, String yearLevel) {
        String sql = "UPDATE Users SET full_name = ?, age = ?, department = ?, year_level = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName); pstmt.setInt(2, age);
            pstmt.setString(3, department); pstmt.setString(4, yearLevel); pstmt.setInt(5, this.id);
            if (pstmt.executeUpdate() > 0) {
                setProfileData(fullName, age, department, yearLevel);
                return "Success: Profile updated!";
            }
        } catch (SQLException e) {}
        return "Error updating profile.";
    }
}