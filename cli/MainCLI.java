package cli;

import models.*;
import java.util.Scanner;

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
        System.out.println("2. Enter Role Menu (" + loggedInUser.getRole() + ")");
        System.out.println("3. Logout");
        System.out.print("Select an option: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                viewProfile();
                break;
            case "2":
                if (loggedInUser instanceof Admin) {
                    adminMenu((Admin) loggedInUser);
                } else if (loggedInUser instanceof Organizer) {
                    organizerMenu((Organizer) loggedInUser);
                } else if (loggedInUser instanceof Participant) {
                    participantMenu((Participant) loggedInUser);
                }
                break;
            case "3":
                loggedInUser = null;
                System.out.println("\nLogged out successfully.");
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
        System.out.println("Press ENTER to return to Dashboard.");
        scanner.nextLine();
    }

    // ==========================================
    // ROLE-SPECIFIC MENUS
    // ==========================================

    private void adminMenu(Admin admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Admin Management ---");
            System.out.println("1. View All Events");
            System.out.println("2. Delete an Event");
            System.out.println("3. View All Users");
            System.out.println("4. Back to Dashboard");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("\n[All Events]");
                    for (Event e : admin.viewAllEvents()) {
                        System.out.println(e.getId() + " | " + e.getTitle() + " | " + e.getDate() + " | Org: " + e.getOrganizerName());
                    }
                    break;
                case "2":
                    System.out.print("Enter Event ID to delete: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(admin.deleteAnyEvent(id));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                    break;
                case "3":
                    System.out.println("\n[All Users]");
                    for (String[] u : admin.viewAllUsers()) {
                        System.out.println("ID: " + u[0] + " | User: " + u[1] + " | Role: " + u[2] + " | Name: " + u[3]);
                    }
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void organizerMenu(Organizer org) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Organizer Menu ---");
            System.out.println("1. View My Events");
            System.out.println("2. Create Event");
            System.out.println("3. Delete My Event");
            System.out.println("4. Back to Dashboard");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("\n[My Events]");
                    for (Event e : org.viewMyEvents()) {
                        System.out.println(e.getId() + " | " + e.getTitle() + " | " + e.getDate() + " | Cap: " + e.getCapacity());
                    }
                    break;
                case "2":
                    System.out.print("Title: "); String title = scanner.nextLine().trim();
                    System.out.print("Description: "); String desc = scanner.nextLine().trim();
                    System.out.print("Date (e.g., January 01 2026): "); String date = scanner.nextLine().trim();
                    System.out.print("Time (e.g., 08:30 AM): "); String time = scanner.nextLine().trim();
                    System.out.print("Capacity: ");
                    try {
                        int cap = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(org.createEvent(title, desc, date, time, cap));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid capacity. Event creation failed.");
                    }
                    break;
                case "3":
                    System.out.print("Enter Event ID to delete: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(org.deleteEvent(id));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void participantMenu(Participant p) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Participant Menu ---");
            System.out.println("1. Browse Events");
            System.out.println("2. Register for an Event");
            System.out.println("3. Check Notifications");
            System.out.println("4. Back to Dashboard");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("\n[Available Events]");
                    for (Event e : p.browseEvents()) {
                        System.out.println(e.getId() + " | " + e.getTitle() + " | " + e.getDate() + " " + e.getTime() + " | Org: " + e.getOrganizerName());
                    }
                    break;
                case "2":
                    System.out.print("Enter Event ID to register: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(p.registerForEvent(id));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                    break;
                case "3":
                    System.out.println("\n[Notifications]");
                    for (String notif : p.checkNotifications()) {
                        System.out.println("- " + notif);
                    }
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}