package sad.gui;

/**  @author eugen */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import sad.dao.BookshopDAO;
import sad.model.Book;
import sad.model.Exchange;
import sad.model.Loan;
import sad.model.User;

public class AdminPanel extends JPanel {
    private User currentUser;
    private BookshopDAO dao;
    private JTabbedPane tabbedPane;
    private JTable usersTable, exchangesTable, booksTable;
    
    public AdminPanel(User user) {
        this.currentUser = user;
        this.dao = new BookshopDAO();
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Welcome header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(60, 60, 120));
        JLabel welcomeLabel = new JLabel("Administrator Dashboard - " + currentUser.getFullName());
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(welcomeLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed pane for different functions
        tabbedPane = new JTabbedPane();
        
        // Tab 1: User Management
        tabbedPane.addTab("User Management", createUserManagementPanel());
        
        // Tab 2: Exchange Approvals
        tabbedPane.addTab("Exchange Approvals", createExchangeApprovalPanel());
        
        // Tab 3: Book Management
        tabbedPane.addTab("Book Management", createBookManagementPanel());
        
        // Tab 4: System Overview
        tabbedPane.addTab("System Overview", createOverviewPanel());
        
        // Tab 5: Audit Log
        tabbedPane.addTab("Audit Log", createAuditPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table
        usersTable = new JTable();
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add User");
        JButton deleteBtn = new JButton("Delete User");
        JButton viewLoansBtn = new JButton("View User Loans");
        
        refreshBtn.addActionListener(e -> loadUsers());
        addBtn.addActionListener(e -> addUser());
        deleteBtn.addActionListener(e -> deleteUser());
        viewLoansBtn.addActionListener(e -> viewUserLoans());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(viewLoansBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createExchangeApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table
        exchangesTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(exchangesTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton approveBtn = new JButton("Approve Selected");
        JButton rejectBtn = new JButton("Reject Selected");
        JButton detailsBtn = new JButton("View Details");
        
        refreshBtn.addActionListener(e -> loadExchanges());
        approveBtn.addActionListener(e -> approveExchange());
        rejectBtn.addActionListener(e -> rejectExchange());
        detailsBtn.addActionListener(e -> viewExchangeDetails());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(detailsBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBookManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table
        booksTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton addBookBtn = new JButton("Add Book");
        JButton updateStockBtn = new JButton("Update Stock");
        JButton viewRestrictedBtn = new JButton("View Restricted");
        
        refreshBtn.addActionListener(e -> loadBooks());
        addBookBtn.addActionListener(e -> addBook());
        updateStockBtn.addActionListener(e -> updateStock());
        viewRestrictedBtn.addActionListener(e -> viewRestrictedBooks());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBookBtn);
        buttonPanel.add(updateStockBtn);
        buttonPanel.add(viewRestrictedBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        try {
            int userCount = dao.getAllUsers().size();
            int bookCount = dao.getAllBooks().size();
            int loanCount = dao.getAllLoans().size();
            int exchangeCount = dao.getAllExchanges().size();
            
            panel.add(createStatPanel("Total Users", String.valueOf(userCount), new Color(70, 130, 180)));
            panel.add(createStatPanel("Total Books", String.valueOf(bookCount), new Color(50, 150, 100)));
            panel.add(createStatPanel("Active Loans", String.valueOf(loanCount), new Color(200, 100, 50)));
            panel.add(createStatPanel("Pending Exchanges", String.valueOf(exchangeCount), new Color(150, 50, 150)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return panel;
    }
    
    private JPanel createStatPanel(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAuditPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea auditArea = new JTextArea();
        auditArea.setEditable(false);
        auditArea.setText("Recent System Activity:\n\n" +
                          "2026-02-15 10:30: Admin login\n" +
                          "2026-02-15 09:45: Loan approved by librarian\n" +
                          "2026-02-14 16:20: New user registered\n" +
                          "2026-02-14 14:10: Exchange request submitted\n" +
                          "2026-02-13 11:05: Book stock updated");
        panel.add(new JScrollPane(auditArea), BorderLayout.CENTER);
        
        JButton refreshAuditBtn = new JButton("Refresh Log");
        refreshAuditBtn.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, "Audit log refreshed (simulated)"));
        panel.add(refreshAuditBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadData() {
        loadUsers();
        loadExchanges();
        loadBooks();
    }
    
    private void loadUsers() {
        try {
            List<User> users = dao.getAllUsers();
            String[] columns = {"ID", "Username", "Full Name", "Email", "Role", "College ID"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (User u : users) {
                model.addRow(new Object[]{
                    u.getId(), u.getUsername(), u.getFullName(), 
                    u.getEmail(), u.getRole(), u.getCollegeId()
                });
            }
            usersTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadExchanges() {
        try {
            List<Exchange> exchanges = dao.getAllExchanges();
            String[] columns = {"ID", "User ID", "Offered Book", "Requested Book", "Status", "Request Date"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Exchange e : exchanges) {
                model.addRow(new Object[]{
                    e.getId(), e.getUserId(), e.getOfferedBookId(),
                    e.getRequestedBookId(), e.getStatus(), e.getRequestDate()
                });
            }
            exchangesTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadBooks() {
        try {
            List<Book> books = dao.getAllBooks();
            String[] columns = {"ID", "Title", "Author", "ISBN", "Stock", "Collection"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Book b : books) {
                model.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), 
                    b.getIsbn(), b.getStock(), b.getCollectionId()
                });
            }
            booksTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addUser() {
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"CUSTOMER", "RESEARCHER", "LIBRARIAN"});
        
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField,
            "Full Name:", nameField,
            "Email:", emailField,
            "Role:", roleBox
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, "Add New User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                User user = new User();
                user.setUsername(usernameField.getText());
                user.setPassword(passwordField.getText());
                user.setFullName(nameField.getText());
                user.setEmail(emailField.getText());
                user.setRole((String) roleBox.getSelectedItem());
                user.setCollegeId(1);
                
                if (dao.addUser(user)) {
                    JOptionPane.showMessageDialog(this, "User added successfully");
                    loadUsers();
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding user: " + e.getMessage());
            }
        }
    }
    
    private void deleteUser() {
        int row = usersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete");
            return;
        }
        
        int userId = (int) usersTable.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this user?", "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (dao.deleteUser(userId)) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully");
                    loadUsers();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void viewUserLoans() {
        int row = usersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user");
            return;
        }
        
        int userId = (int) usersTable.getValueAt(row, 0);
        String userName = (String) usersTable.getValueAt(row, 2);
        
        try {
            List<Loan> loans = dao.getUserLoans(userId);
            StringBuilder sb = new StringBuilder("Loans for " + userName + ":");
            
            if (loans.isEmpty()) {
                sb.append("No loans found");
            } else {
                for (Loan l : loans) {
                    sb.append(String.format("Book ID: %d | Type: %s | Status: %s | Due: %s",
                        l.getBookId(), l.getLoanType(), l.getStatus(), l.getDueDate()));
                }
            }
            
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, "User Loans", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void approveExchange() {
        int row = exchangesTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an exchange to approve");
            return;
        }
        
        int exchangeId = (int) exchangesTable.getValueAt(row, 0);
        
        try {
            if (dao.approveExchange(exchangeId, currentUser.getId())) {
                JOptionPane.showMessageDialog(this, "Exchange approved successfully");
                loadExchanges();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void rejectExchange() {
        int row = exchangesTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an exchange to reject");
            return;
        }
        
        int exchangeId = (int) exchangesTable.getValueAt(row, 0);
        
        try {
            if (dao.rejectExchange(exchangeId, currentUser.getId())) {
                JOptionPane.showMessageDialog(this, "Exchange rejected");
                loadExchanges();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void viewExchangeDetails() {
        int row = exchangesTable.getSelectedRow();
        if (row == -1) return;
        
        int exchangeId = (int) exchangesTable.getValueAt(row, 0);
        JOptionPane.showMessageDialog(this, 
            "Exchange #" + exchangeId + " details would appear here",
            "Exchange Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void addBook() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField stockField = new JTextField("0");
        
        Object[] message = {
            "Title:", titleField,
            "Author:", authorField,
            "ISBN:", isbnField,
            "Stock:", stockField
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, "Add New Book", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Book book = new Book();
                book.setTitle(titleField.getText());
                book.setAuthor(authorField.getText());
                book.setIsbn(isbnField.getText());
                book.setStock(Integer.parseInt(stockField.getText()));
                book.setCollectionId(1);
                
                if (dao.addBook(book)) {
                    JOptionPane.showMessageDialog(this, "Book added successfully");
                    loadBooks();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void updateStock() {
        int row = booksTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book");
            return;
        }
        
        int bookId = (int) booksTable.getValueAt(row, 0);
        String title = (String) booksTable.getValueAt(row, 1);
        int currentStock = (int) booksTable.getValueAt(row, 4);
        
        String newStockStr = JOptionPane.showInputDialog(this,"Update stock for " + title + " (current: " + currentStock + "):");
        
        try {
            int newStock = Integer.parseInt(newStockStr);
            if (dao.updateBookStock(bookId, newStock)) {
                JOptionPane.showMessageDialog(this, "Stock updated successfully");
                loadBooks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void viewRestrictedBooks() {
        try {
            List<Book> restricted = dao.getRestrictedBooks();
            StringBuilder sb = new StringBuilder("Restricted Books:");
            
            if (restricted.isEmpty()) {
                sb.append("No restricted books found");
            } else {
                for (Book b : restricted) {
                    sb.append(b.getTitle()).append(" by ").append(b.getAuthor()).append("");
                }
            }
            
            JOptionPane.showMessageDialog(this, sb.toString(), "Restricted Collection", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
