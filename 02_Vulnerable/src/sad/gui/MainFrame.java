package sad.gui;

/**  @author eugen */
import javax.swing.*;
import sad.model.User;

public class MainFrame extends JFrame {
    private User currentUser;
    
    public MainFrame(User user) {
        this.currentUser = user;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Bookshop - " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Route to appropriate panel based on role
        JPanel mainPanel = null;
        String role = currentUser.getRole().toUpperCase();
        
        switch (role) {
            case "ADMIN":
                mainPanel = new AdminPanel(currentUser);
                break;
            case "LIBRARIAN":
                mainPanel = new LibrarianPanel(currentUser);
                break;
            case "RESEARCHER":
                mainPanel = new ResearcherPanel(currentUser);
                break;
            case "CUSTOMER":
                mainPanel = new CustomerPanel(currentUser);
                break;
            case "GUEST":
            default:
                mainPanel = new GuestPanel(currentUser);
                break;
        }
        
        setContentPane(mainPanel);
    }
}
