package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseManager {
    // --- UPDATED: Changed path to the database folder and renamed to aop.db ---
    private static final String DB_FOLDER = "database";
    private static final String DB_NAME = DB_FOLDER + File.separator + "aop.db";
    private static final String URL = "jdbc:sqlite:" + DB_NAME;

    public static Connection connect() {
        Connection conn = null;
        try { 
            // Ensure the directory exists before trying to connect
            File directory = new File(DB_FOLDER);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            conn = DriverManager.getConnection(URL); 
        } catch (SQLException e) { 
            System.err.println("Connection failed: " + e.getMessage()); 
        }
        return conn;
    }

    public static void initializeDatabase() {
        try {
            // Ensure directory exists before initialization 
            File directory = new File(DB_FOLDER);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // If the database already exists, skip creating tables
            if (new File(DB_NAME).exists()) return;

            String createUsers = "CREATE TABLE IF NOT EXISTS Users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT UNIQUE NOT NULL, "
                    + "password TEXT NOT NULL, "
                    + "role TEXT NOT NULL, "
                    + "full_name TEXT, "
                    + "age INTEGER, "
                    + "department TEXT, "
                    + "year_level TEXT);";
                    
            String createEvents = "CREATE TABLE IF NOT EXISTS Events ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "title TEXT NOT NULL, "
                    + "description TEXT, "
                    + "event_date TEXT NOT NULL, "
                    + "event_time TEXT NOT NULL, "
                    + "capacity INTEGER NOT NULL, "
                    + "organizer_id INTEGER, "
                    + "FOREIGN KEY(organizer_id) REFERENCES Users(id));";
                    
            String createRegistrations = "CREATE TABLE IF NOT EXISTS Registrations ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "participant_id INTEGER, "
                    + "event_id INTEGER, "
                    + "status TEXT NOT NULL, "
                    + "FOREIGN KEY(participant_id) REFERENCES Users(id), "
                    + "FOREIGN KEY(event_id) REFERENCES Events(id));";
                    
            String createInvites = "CREATE TABLE IF NOT EXISTS Invites ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "event_id INTEGER, "
                    + "participant_id INTEGER, "
                    + "FOREIGN KEY(event_id) REFERENCES Events(id), "
                    + "FOREIGN KEY(participant_id) REFERENCES Users(id));";
                    
            // Default Admin Account
            String insertAdmin = "INSERT OR IGNORE INTO Users (username, password, role) VALUES ('admin', 'admin123', 'Admin');";

            try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
                if (conn != null) {
                    stmt.execute(createUsers); 
                    stmt.execute(createEvents);
                    stmt.execute(createRegistrations); 
                    stmt.execute(createInvites);
                    stmt.execute(insertAdmin);
                }
            }
        } catch (Exception e) { 
            System.err.println("Init error: " + e.getMessage()); 
        }
    }
}