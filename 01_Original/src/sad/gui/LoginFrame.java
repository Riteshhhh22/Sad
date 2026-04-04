package sad.gui;

/**  @author eugen */

import javax.swing.*;
import java.awt.*;
import sad.dao.BookshopDAO;
import sad.model.User;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton guestButton;
    private BookshopDAO dao;
    
    public LoginFrame() {
        dao = new BookshopDAO();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Bookshop Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Bookshop Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);
        
        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);
        
        formPanel.add(new JLabel(""));
        JPanel buttonPanel = new JPanel(new FlowLayout());
        loginButton = new JButton("Login");
        guestButton = new JButton("Continue as Guest");
        buttonPanel.add(loginButton);
        buttonPanel.add(guestButton);
        formPanel.add(buttonPanel);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Status label
        JLabel statusLabel = new JLabel("Enter credentials or continue as guest", SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Action listeners
        loginButton.addActionListener(e -> performLogin());
        guestButton.addActionListener(e -> loginAsGuest());
        
        // Enter key listener
        usernameField.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            User user = dao.login(username, password);
            if (user != null) {
                JOptionPane.showMessageDialog(this, 
                    "Welcome, " + user.getFullName() + "!\nRole: " + user.getRole(), 
                    "Login Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                openMainFrame(user);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Database error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loginAsGuest() {
        try {
            User guest = dao.login("guest", "guest123");
            if (guest != null) {
                openMainFrame(guest);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Guest login failed. Using default guest.", 
                    "Warning", 
                    JOptionPane.WARNING_MESSAGE);
                User defaultGuest = new User();
                defaultGuest.setId(5);
                defaultGuest.setUsername("guest");
                defaultGuest.setFullName("Guest User");
                defaultGuest.setRole("GUEST");
                openMainFrame(defaultGuest);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void openMainFrame(User user) {
        MainFrame mainFrame = new MainFrame(user);
        mainFrame.setVisible(true);
        dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame().setVisible(true);
        });
    }
}
