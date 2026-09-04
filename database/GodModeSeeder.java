package database;

import models.Admin;
import models.User;

public class GodModeSeeder {
    
    public static void seedDatabase(Admin admin) {
        // 1. Create 10 Users (3 Organizers, 7 Participants)
        for (int i = 1; i <= 10; i++) {
            String username = "testuser" + i;
            String password = "password"; // Easy password for testing
            String role = (i <= 3) ? "Organizer" : "Participant";
            
            // Register the user
            User.register(username, password, role);
            
            // Log in temporarily to retrieve the User object and fill out their profile
            User u = User.login(username, password);
            if (u != null) {
                u.updateProfile(
                    "Test User " + i, 
                    18 + (i % 5), 
                    "Computer Science", 
                    "Year " + ((i % 4) + 1)
                );
            }
        }

        // 2. Create 3 Events
        for (int i = 1; i <= 3; i++) {
            admin.createEvent(
                "God Mode Event " + i, 
                "This is an automatically generated event description for event number " + i + ". It contains enough text to demonstrate the text wrapping feature you just implemented.", 
                "December 1" + i + " 2026", 
                "0" + (7+i) + ":00 AM", 
                50 * i
            );
        }
    }
}