import gui.MainGUI;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Launch the application safely on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
}