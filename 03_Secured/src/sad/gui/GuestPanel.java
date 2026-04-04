package sad.gui;

/**  @author eugen */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import sad.dao.BookshopDAO;
import sad.model.Book;
import sad.model.User;

public class GuestPanel extends JPanel {
    private User currentUser;
    private BookshopDAO dao;
    private JTable booksTable;
    
    public GuestPanel(User user) {
        this.currentUser = user;
        this.dao = new BookshopDAO();
        initComponents();
        loadBooks();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(100, 100, 100));
        JLabel headerLabel = new JLabel("Guest Access - " + currentUser.getFullName() + " (Read Only)");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        JTabbedPane tabs = new JTabbedPane();
        
        // Tab 1: Browse Catalogue
        tabs.addTab("Browse Catalogue", createBrowsePanel());
        
        // Tab 2: Search Books
        tabs.addTab("Search Books", createSearchPanel());
        
        // Tab 3: View Collections
        tabs.addTab("View Collections", createCollectionsPanel());
        
        // Tab 4: Check Availability
        tabs.addTab("Check Availability", createAvailabilityPanel());
        
        // Tab 5: Register
        tabs.addTab("Register", createRegisterPanel());
        
        add(tabs, BorderLayout.CENTER);
    }
    
    private JPanel createBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        booksTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton detailsBtn = new JButton("View Details");
        
        refreshBtn.addActionListener(e -> loadBooks());
        detailsBtn.addActionListener(e -> viewBookDetails());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(detailsBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Search:"));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        JTextArea resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                try {
                    List<Book> results = dao.searchBooks(keyword);
                    StringBuilder sb = new StringBuilder("Search Results for " + keyword + ":");
                    
                    if (results.isEmpty()) {
                        sb.append("No books found.");
                    } else {
                        for (Book b : results) {
                            sb.append("• ").append(b.getTitle())
                              .append(" by ").append(b.getAuthor())
                              .append(" (Stock: ").append(b.getStock()).append(")");
                        }
                    }
                    resultsArea.setText(sb.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        return panel;
    }
    
    private JPanel createCollectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] collections = {
            "General Collection", 
            "Research Archive (Restricted)", 
            "Rare Books (Restricted)", 
            "Textbooks"
        };
        
        JList<String> collectionList = new JList<>(collections);
        collectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(collectionList), BorderLayout.CENTER);
        
        JButton viewBtn = new JButton("View Collection");
        viewBtn.addActionListener(e -> {
            String selected = collectionList.getSelectedValue();
            if (selected != null) {
                if (selected.contains("Restricted")) {
                    JOptionPane.showMessageDialog(this,
                        "This collection is restricted to researchers and faculty only." +
                        "Please register or login with appropriate credentials.",
                        "Access Denied", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,"Showing books from: " + selected + "(This would display the collection contents)",
                        "Collection View", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        panel.add(viewBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAvailabilityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        topPanel.add(new JLabel("Enter Book Title or ISBN:"));
        JTextField bookField = new JTextField();
        topPanel.add(bookField);
        
        JButton checkBtn = new JButton("Check Availability");
        topPanel.add(new JLabel(""));
        topPanel.add(checkBtn);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        JTextArea availabilityArea = new JTextArea();
        availabilityArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(availabilityArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        checkBtn.addActionListener(e -> {
            String query = bookField.getText().trim();
            if (!query.isEmpty()) {
                try {
                    List<Book> results = dao.searchBooks(query);
                    if (!results.isEmpty()) {
                        Book book = results.get(0);
                        availabilityArea.setText(
                            "Book: " + book.getTitle() + "" +        "Author: " + book.getAuthor() + "" +
                            "ISBN: " + book.getIsbn() + "" +  "Available Copies: " + book.getStock() + "" +
                            (book.getStock() > 0 ? "✓ In Stock" : "✗ Out of Stock")
                        );
                    } else {
                        availabilityArea.setText("Book not found in catalogue.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        return panel;
    }
    
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Confirm Password:"));
        formPanel.add(confirmField);
        
        JButton registerBtn = new JButton("Create Account");
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required", 
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", 
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JOptionPane.showMessageDialog(this,"Registration successful! You can now login with your credentials." +
                "Your account will be created as a CUSTOMER.","Registration Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(registerBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadBooks() {
        try {
            List<Book> books = dao.getAllBooks();
            String[] columns = {"ID", "Title", "Author", "Stock"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Book b : books) {
                model.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), b.getStock()
                });
            }
            booksTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void viewBookDetails() {
        int row = booksTable.getSelectedRow();
        if (row == -1) return;
        
        String title = (String) booksTable.getValueAt(row, 1);
        String author = (String) booksTable.getValueAt(row, 2);
        int stock = (int) booksTable.getValueAt(row, 3);
        
        JOptionPane.showMessageDialog(this,"Book Details:" +  "Title: " + title + "" +  "Author: " + author + "" +
            "Available: " + stock + " copies" +
            "To borrow or purchase, please register or login.",
            "Book Information", JOptionPane.INFORMATION_MESSAGE);
    }
}
