package cli;

import models.*;
import java.util.List;
import java.util.Scanner;
import database.GodModeSeeder;

public class MainCLI {
    private Scanner scanner;
    private User loggedInUser;

    public MainCLI() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=========================================");
        System.out.println("   Academic Organization Platform CLI    ");
        System.out.println("=========================================");

        while (true) {
            if (loggedInUser == null) {
                authMenu();
            } else {
                dashboardMenu();
            }
        }
    }

    private void authMenu() {
        System.out.println("\n--- Authentication ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Select an option: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Password: ");
                String password = scanner.nextLine().trim();

                User u = User.login(username, password);
                if (u != null) {
                    loggedInUser = u;
                    System.out.println("\nLogin successful! Welcome, " + u.getUsername() + ".");
                } else {
                    System.out.println("\nError: Invalid credentials.");
                }
                break;
            case "2":
                System.out.print("Choose Username: ");
                String newUsername = scanner.nextLine().trim();
                System.out.print("Choose Password: ");
                String newPassword = scanner.nextLine().trim();

                String res = User.register(newUsername, newPassword, "Participant");
                System.out.println("\n" + res);
                break;
            case "3":
                System.out.println("Exiting application...");
                System.exit(0);
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void dashboardMenu() {
        System.out.println("\n--- Dashboard (" + loggedInUser.getRole() + ") ---");
        System.out.println("1. View Profile");
        System.out.println("2. Edit Profile");
        System.out.println("3. Change Password");
        System.out.println("4. Enter Role Menu (" + loggedInUser.getRole() + ")");
        System.out.println("5. Logout");
        
        if (loggedInUser instanceof Admin) {
            System.out.println("6. GOD MODE: Seed DB");
        }
        
        System.out.print("Select an option: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                viewProfile();
                break;
            case "2":
                editProfile();
                break;
            case "3":
                changePassword();
                break;
            case "4":
                if (loggedInUser instanceof Admin) {
                    adminMenu((Admin) loggedInUser);
                } else if (loggedInUser instanceof Organizer) {
                    organizerMenu((Organizer) loggedInUser);
                } else if (loggedInUser instanceof Participant) {
                    participantMenu((Participant) loggedInUser);
                }
                break;
            case "5":
                loggedInUser = null;
                System.out.println("\nLogged out successfully.");
                break;
            case "6":
                if (loggedInUser instanceof Admin) {
                    GodModeSeeder.seedDatabase((Admin) loggedInUser);
                    System.out.println("\nDatabase successfully seeded with 10 Users and 3 Events!");
                } else {
                    System.out.println("Invalid option.");
                }
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void viewProfile() {
        System.out.println("\n--- Your Profile ---");
        System.out.println("Username:   " + loggedInUser.getUsername());
        System.out.println("Full Name:  " + (loggedInUser.getFullName() != null ? loggedInUser.getFullName() : "N/A"));
        System.out.println("Age:        " + (loggedInUser.getAge() > 0 ? loggedInUser.getAge() : "N/A"));
        System.out.println("Department: " + (loggedInUser.getDepartment() != null ? loggedInUser.getDepartment() : "N/A"));
        System.out.println("Year Level: " + (loggedInUser.getYearLevel() != null ? loggedInUser.getYearLevel() : "N/A"));
        System.out.println("--------------------");
        System.out.println("Press ENTER to return.");
        scanner.nextLine();
    }

    private void editProfile() {
        System.out.println("\n--- Edit Profile ---");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Age: ");
        int age = 0;
        try {
            String ageStr = scanner.nextLine().trim();
            if (!ageStr.isEmpty()) age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Defaulting to 0.");
        }

        System.out.print("Department: ");
        String dept = scanner.nextLine().trim();
        System.out.print("Year Level: ");
        String year = scanner.nextLine().trim();

        System.out.println(loggedInUser.updateProfile(name, age, dept, year));
    }

    private void changePassword() {
        System.out.println("\n--- Change Password ---");
        System.out.print("Current Password: ");
        String currentPass = scanner.nextLine().trim();
        System.out.print("New Password: ");
        String newPass = scanner.nextLine().trim();
        System.out.print("Confirm New Password: ");
        String confirmPass = scanner.nextLine().trim();

        if (!newPass.equals(confirmPass)) {
            System.out.println("Error: New passwords do not match.");
            return;
        }

        System.out.println(loggedInUser.changePassword(currentPass, newPass));
    }

    // ==========================================
    // ROLE-SPECIFIC MENUS
    // ==========================================

    private void adminMenu(Admin admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Event Management");
            System.out.println("2. User Management");
            System.out.println("3. Back to Dashboard");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": adminEventManagement(admin); break;
                case "2": adminUserManagement(admin); break;
                case "3": back = true; break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void adminEventManagement(Admin a) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Admin Event Management ---");
            System.out.println("1. View All Events (List)");
            System.out.println("2. Manage Specific Event (Details & Participants)");
            System.out.println("3. Create Event");
            System.out.println("4. Edit Event");
            System.out.println("5. Delete Event");
            System.out.println("6. Back");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("\n[All Events]");
                    for (Event e : a.viewAllEvents()) {
                        System.out.println("ID: " + e.getId() + " | Title: " + e.getTitle() + " | Date/Time: " + e.getDate() + " " + e.getTime() + " | Org: " + e.getOrganizerName());
                    }
                    break;
                case "2":
                    System.out.print("Enter Event ID to manage: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        Event target = null;
                        for (Event ev : a.viewAllEvents()) {
                            if (ev.getId() == id) { target = ev; break; }
                        }
                        if (target != null) manageEventParticipantsAdmin(a, target);
                        else System.out.println("Event not found.");
                    } catch (NumberFormatException e) { System.out.println("Invalid ID format."); }
                    break;
                case "3":
                    System.out.print("Title: "); String title = scanner.nextLine().trim();
                    System.out.print("Description: "); String desc = scanner.nextLine().trim();
                    System.out.print("Date (e.g., January 01 2026): "); String date = scanner.nextLine().trim();
                    System.out.print("Time (e.g., 08:30 AM): "); String time = scanner.nextLine().trim();
                    System.out.print("Capacity: ");
                    try {
                        int cap = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(a.createEvent(title, desc, date, time, cap));
                    } catch (NumberFormatException e) { System.out.println("Invalid capacity."); }
                    break;
                case "4":
                    System.out.print("Enter Event ID to edit: ");
                    try {
                        int editId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("New Title: "); String nTitle = scanner.nextLine().trim();
                        System.out.print("New Description: "); String nDesc = scanner.nextLine().trim();
                        System.out.print("New Date: "); String nDate = scanner.nextLine().trim();
                        System.out.print("New Time: "); String nTime = scanner.nextLine().trim();
                        System.out.print("New Capacity: "); int nCap = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(a.updateAnyEvent(editId, nTitle, nDesc, nDate, nTime, nCap));
                    } catch (NumberFormatException e) { System.out.println("Invalid input format."); }
                    break;
                case "5":
                    System.out.print("Enter Event ID to delete: ");
                    try {
                        int delId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(a.deleteAnyEvent(delId));
                    } catch (NumberFormatException e) { System.out.println("Invalid ID format."); }
                    break;
                case "6": back = true; break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void manageEventParticipantsAdmin(Admin a, Event e) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Event Details ---");
            System.out.println("Title: " + e.getTitle());
            System.out.println("Description: " + (e.getDescription() != null ? e.getDescription() : "N/A"));
            System.out.println("Date & Time: " + e.getDate() + " @ " + e.getTime());
            System.out.println("Capacity: " + e.getCapacity());

            System.out.println("\n[Participants]");
            List<String> parts = a.generateReport(e.getId());
            for (String p : parts) System.out.println("- " + p);

            System.out.println("\n[Invited (Pending)]");
            List<String> invites = a.getInvitedParticipants(e.getId());
            for (String inv : invites) System.out.println("- " + inv);

            System.out.println("\n1. Invite Participant");
            System.out.println("2. Remove Participant");
            System.out.println("3. Back");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Enter username to invite: ");
                    String uname = scanner.nextLine().trim();
                    System.out.println(a.inviteParticipant(e.getId(), uname));
                    break;
                case "2":
                    System.out.print("Enter username/full name to remove: ");
                    String rname = scanner.nextLine().trim();
                    System.out.println(a.forceParticipantUpdate(e.getId(), rname, false));
                    break;
                case "3": back = true; break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void adminUserManagement(Admin a) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Admin User Management ---");
            System.out.println("1. View All Users (List)");
            System.out.println("2. View Specific User Profile");
            System.out.println("3. Create User");
            System.out.println("4. Promote to Organizer");
            System.out.println("5. Demote to Participant");
            System.out.println("6. Purge Account");
            System.out.println("7. Back");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("\n[All Users]");
                    for (String[] u : a.viewAllUsers()) {
                        System.out.println("ID: " + u[0] + " | Username: " + u[1] + " | Role: " + u[2] + " | Name: " + u[3]);
                    }
                    break;
                case "2":
                    System.out.print("Enter username to view profile: ");
                    String[] profile = a.viewUserProfile(scanner.nextLine().trim());
                    if (profile != null) {
                        System.out.println("\n--- User Profile ---");
                        System.out.println("Username: " + profile[0]);
                        System.out.println("Role: " + profile[1]);
                        System.out.println("Full Name: " + profile[2]);
                        System.out.println("Age: " + profile[3]);
                        System.out.println("Department: " + profile[4]);
                        System.out.println("Year Level: " + profile[5]);
                    } else {
                        System.out.println("User not found.");
                    }
                    break;
                case "3":
                    System.out.print("New Username: "); String nUser = scanner.nextLine().trim();
                    System.out.print("New Password: "); String nPass = scanner.nextLine().trim();
                    System.out.println(User.register(nUser, nPass, "Participant"));
                    break;
                case "4":
                    System.out.print("Enter username to promote: ");
                    System.out.println(a.changeUserRole(scanner.nextLine().trim(), "Organizer"));
                    break;
                case "5":
                    System.out.print("Enter username to demote: ");
                    System.out.println(a.changeUserRole(scanner.nextLine().trim(), "Participant"));
                    break;
                case "6":
                    System.out.print("Enter username to purge: ");
                    System.out.println(a.deleteAccount(scanner.nextLine().trim()));
                    break;
                case "7": back = true; break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void organizerMenu(Organizer org) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Organizer Menu ---");
            System.out.println("1. View My Events (List)");
            System.out.println("2. Manage Specific Event (Details & Participants)");
            System.out.println("3. Create Event");
            System.out.println("4. Edit Event");
            System.out.println("5. Delete My Event");
            System.out.println("6. Back to Dashboard");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("\n[My Events]");
                    for (Event e : org.viewMyEvents()) {
                        System.out.println("ID: " + e.getId() + " | Title: " + e.getTitle() + " | Date/Time: " + e.getDate() + " " + e.getTime() + " | Cap: " + e.getCapacity());
                    }
                    break;
                case "2":
                    System.out.print("Enter Event ID to manage: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        Event target = null;
                        for (Event ev : org.viewMyEvents()) {
                            if (ev.getId() == id) { target = ev; break; }
                        }
                        if (target != null) manageEventParticipantsOrg(org, target);
                        else System.out.println("Event not found or you do not own it.");
                    } catch (NumberFormatException e) { System.out.println("Invalid ID format."); }
                    break;
                case "3":
                    System.out.print("Title: "); String title = scanner.nextLine().trim();
                    System.out.print("Description: "); String desc = scanner.nextLine().trim();
                    System.out.print("Date (e.g., January 01 2026): "); String date = scanner.nextLine().trim();
                    System.out.print("Time (e.g., 08:30 AM): "); String time = scanner.nextLine().trim();
                    System.out.print("Capacity: ");
                    try {
                        int cap = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(org.createEvent(title, desc, date, time, cap));
                    } catch (NumberFormatException e) { System.out.println("Invalid capacity."); }
                    break;
                case "4":
                    System.out.print("Enter Event ID to edit: ");
                    try {
                        int editId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("New Title: "); String nTitle = scanner.nextLine().trim();
                        System.out.print("New Description: "); String nDesc = scanner.nextLine().trim();
                        System.out.print("New Date: "); String nDate = scanner.nextLine().trim();
                        System.out.print("New Time: "); String nTime = scanner.nextLine().trim();
                        System.out.print("New Capacity: "); int nCap = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(org.updateEvent(editId, nTitle, nDesc, nDate, nTime, nCap));
                    } catch (NumberFormatException e) { System.out.println("Invalid input format."); }
                    break;
                case "5":
                    System.out.print("Enter Event ID to delete: ");
                    try {
                        int delId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(org.deleteEvent(delId));
                    } catch (NumberFormatException e) { System.out.println("Invalid ID format."); }
                    break;
                case "6": back = true; break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void manageEventParticipantsOrg(Organizer o, Event e) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Event Details ---");
            System.out.println("Title: " + e.getTitle());
            System.out.println("Description: " + (e.getDescription() != null ? e.getDescription() : "N/A"));
            System.out.println("Date & Time: " + e.getDate() + " @ " + e.getTime());
            System.out.println("Capacity: " + e.getCapacity());

            System.out.println("\n[Participants]");
            List<String> parts = o.generateReport(e.getId());
            for (String p : parts) System.out.println("- " + p);

            System.out.println("\n[Invited (Pending)]");
            List<String> invites = o.getInvitedParticipants(e.getId());
            for (String inv : invites) System.out.println("- " + inv);

            System.out.println("\n1. Invite Participant");
            System.out.println("2. Remove Participant");
            System.out.println("3. Back");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Enter username to invite: ");
                    String uname = scanner.nextLine().trim();
                    System.out.println(o.inviteParticipant(e.getId(), uname));
                    break;
                case "2":
                    System.out.print("Enter username/full name to remove: ");
                    String rname = scanner.nextLine().trim();
                    System.out.println(o.forceParticipantUpdate(e.getId(), rname, false));
                    break;
                case "3": back = true; break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void participantMenu(Participant p) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Participant Menu ---");
            System.out.println("1. Browse Events");
            System.out.println("2. View Event Details");
            System.out.println("3. Register for an Event");
            System.out.println("4. Unregister from an Event");
            System.out.println("5. Check Notifications & Invites");
            System.out.println("6. Back to Dashboard");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("\n[Available Events]");
                    for (Event e : p.browseEvents()) {
                        System.out.println("ID: " + e.getId() + " | Title: " + e.getTitle() + " | Date/Time: " + e.getDate() + " " + e.getTime() + " | Org: " + e.getOrganizerName());
                    }
                    break;
                case "2":
                    System.out.print("Enter Event ID to view: ");
                    try {
                        int viewId = Integer.parseInt(scanner.nextLine().trim());
                        Event viewEvent = null;
                        for (Event e : p.browseEvents()) {
                            if (e.getId() == viewId) { viewEvent = e; break; }
                        }
                        if (viewEvent != null) {
                            System.out.println("\n--- Event Details ---");
                            System.out.println("Title:       " + viewEvent.getTitle());
                            System.out.println("Description: " + (viewEvent.getDescription() != null ? viewEvent.getDescription() : "N/A"));
                            System.out.println("Date & Time: " + viewEvent.getDate() + " @ " + viewEvent.getTime());
                            System.out.println("Capacity:    " + viewEvent.getCapacity());
                            System.out.println("Organizer:   " + (viewEvent.getOrganizerName() != null ? viewEvent.getOrganizerName() : "Unknown"));
                            System.out.println("---------------------");
                        } else {
                            System.out.println("Event not found.");
                        }
                    } catch (NumberFormatException e) { System.out.println("Invalid ID format."); }
                    break;
                case "3":
                    System.out.print("Enter Event ID to register: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(p.registerForEvent(id));
                    } catch (NumberFormatException e) { System.out.println("Invalid ID format."); }
                    break;
                case "4":
                    System.out.print("Enter Event ID to unregister: ");
                    try {
                        int uId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(p.unregisterFromEvent(uId));
                    } catch (NumberFormatException e) { System.out.println("Invalid ID format."); }
                    break;
                case "5":
                    System.out.println("\n[Notifications & Invites]");
                    for (String notif : p.checkNotifications()) {
                        System.out.println("- " + notif);
                    }
                    break;
                case "6": back = true; break;
                default: System.out.println("Invalid option.");
            }
        }
    }
}