import database.DatabaseManager;
import gui.MainGUI;
import cli.MainCLI;

public class Main {
    public static void main(String[] args) {
        // Initialize the database regardless of which mode we are in
        DatabaseManager.initializeDatabase();

        boolean runInCLI = false;

        // Check for the --nogui argument
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--nogui")) {
                runInCLI = true;
                break;
            }
        }

        if (runInCLI) {
            // Launch Command Line Interface
            new MainCLI().start();
        } else {
            // Launch Graphical User Interface
            // Ensure GUI runs on the Event Dispatch Thread for thread safety
            javax.swing.SwingUtilities.invokeLater(() -> {
                new MainGUI();
            });
        }
    }
}