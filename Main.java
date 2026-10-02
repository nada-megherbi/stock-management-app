import view.MainFrame;
import database.DatabaseConnection;
import javax.swing.JOptionPane;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        try {
            Connection testConn = DatabaseConnection.getConnection();
            if (testConn != null && !testConn.isClosed()) {
                System.out.println("🟢 Database connection successful!");
                testConn.close();
                MainFrame.launch();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "❌ Unable to connect to the database!\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "Please ensure that XAMPP (Apache and MySQL) is running.", 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}