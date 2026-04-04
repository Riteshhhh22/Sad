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

public class LibrarianPanel extends JPanel {
    private User currentUser;
    private BookshopDAO dao;
    private JTable loansTable, booksTable, exchangesTable;
    
    public LibrarianPanel(User user) {
        this.currentUser = user;
        this.dao = new BookshopDAO();
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(100, 80, 60));
        JLabel headerLabel = new JLabel("Librarian Dashboard - " + currentUser.getFullName());
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        
        // Tab 1: Loan Approvals
        tabs.addTab("Loan Approvals", createLoanApprovalPanel());
        
        // Tab 2: Book Management
        tabs.addTab("Book Management", createBookManagementPanel());
        
        // Tab 3: Exchange Approvals
        tabs.addTab("Exchange Approvals", createExchangePanel());
        
        // Tab 4: Statistics
        tabs.addTab("Statistics", createStatisticsPanel());
        
        // Tab 5: Reports
        tabs.addTab("Reports", createReportsPanel());
        
        add(tabs, BorderLayout.CENTER);
    }
    
    private JPanel createLoanApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        loansTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(loansTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton approveBtn = new JButton("Approve Selected");
        JButton rejectBtn = new JButton("Reject Selected");
        JButton viewDetailsBtn = new JButton("View Details");
        
        refreshBtn.addActionListener(e -> loadLoans());
        approveBtn.addActionListener(e -> approveLoan());
        rejectBtn.addActionListener(e -> rejectLoan());
        viewDetailsBtn.addActionListener(e -> viewLoanDetails());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(viewDetailsBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBookManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        booksTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add Book");
        JButton updateStockBtn = new JButton("Update Stock");
        JButton searchBtn = new JButton("Search");
        
        refreshBtn.addActionListener(e -> loadBooks());
        addBtn.addActionListener(e -> addBook());
        updateStockBtn.addActionListener(e -> updateStock());
        searchBtn.addActionListener(e -> searchBooks());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(updateStockBtn);
        buttonPanel.add(searchBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createExchangePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        exchangesTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(exchangesTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        
        refreshBtn.addActionListener(e -> loadExchanges());
        approveBtn.addActionListener(e -> approveExchange());
        rejectBtn.addActionListener(e -> rejectExchange());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        try {
            int loanCount = dao.getAllLoans().size();
            int approvedCount = 0;
            int pendingCount = 0;
            int bookCount = dao.getAllBooks().size();
            
            for (Loan l : dao.getAllLoans()) {
                if ("APPROVED".equals(l.getStatus())) approvedCount++;
                else if ("PENDING".equals(l.getStatus())) pendingCount++;
            }
            
            panel.add(createStatCard("Total Loans", String.valueOf(loanCount), new Color(70, 130, 180)));
            panel.add(createStatCard("Approved Loans", String.valueOf(approvedCount), new Color(50, 150, 100)));
            panel.add(createStatCard("Pending Loans", String.valueOf(pendingCount), new Color(200, 100, 50)));
            panel.add(createStatCard("Total Books", String.valueOf(bookCount), new Color(150, 50, 150)));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setText("Generate Reports:" + "1. Monthly Loan Report" +"2. Popular Books Report" +
            "3. Overdue Items Report" + "4. User Activity Report" +
            "Select an option from the buttons below.");
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        String[] reportTypes = {"Monthly Loans", "Popular Books", "Overdue Items", "User Activity"};
        for (String type : reportTypes) {
            JButton btn = new JButton(type);
            btn.addActionListener(e -> 
                JOptionPane.showMessageDialog(this, "Generating " + type + " report..."));
            buttonPanel.add(btn);
        }
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void loadData() {
        loadLoans();
        loadBooks();
        loadExchanges();
    }
    
    private void loadLoans() {
        try {
            List<Loan> loans = dao.getAllLoans();
            String[] columns = {"ID", "User", "Book", "Type", "Due Date", "Status"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Loan l : loans) {
                model.addRow(new Object[]{
                    l.getId(), l.getUserName(), l.getBookTitle(),
                    l.getLoanType(), l.getDueDate(), l.getStatus()
                });
            }
            loansTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadBooks() {
        try {
            List<Book> books = dao.getAllBooks();
            String[] columns = {"ID", "Title", "Author", "ISBN", "Stock"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Book b : books) {
                model.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getStock()
                });
            }
            booksTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadExchanges() {
        try {
            List<Exchange> exchanges = dao.getAllExchanges();
            String[] columns = {"ID", "User ID", "Status", "Request Date"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Exchange e : exchanges) {
                model.addRow(new Object[]{
                    e.getId(), e.getUserId(), e.getStatus(), e.getRequestDate()
                });
            }
            exchangesTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void approveLoan() {
        int row = loansTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan to approve");
            return;
        }
        
        int loanId = (int) loansTable.getValueAt(row, 0);
        
        try {
            if (dao.approveLoan(loanId, currentUser.getId())) {
                JOptionPane.showMessageDialog(this, "Loan approved successfully");
                loadLoans();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void rejectLoan() {
        int row = loansTable.getSelectedRow();
        if (row == -1) return;
        
        JOptionPane.showMessageDialog(this, "Loan rejection would be implemented here");
    }
    
    private void viewLoanDetails() {
        int row = loansTable.getSelectedRow();
        if (row == -1) return;
        
        JOptionPane.showMessageDialog(this, "Loan details would appear here");
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
        if (row == -1) return;
        
        int bookId = (int) booksTable.getValueAt(row, 0);
        String title = (String) booksTable.getValueAt(row, 1);
        int currentStock = (int) booksTable.getValueAt(row, 4);
        
        String newStockStr = JOptionPane.showInputDialog(this, "New stock for " + title + "(current: " + currentStock + "):");
        
        try {
            int newStock = Integer.parseInt(newStockStr);
            if (dao.updateBookStock(bookId, newStock)) {
                JOptionPane.showMessageDialog(this, "Stock updated");
                loadBooks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void searchBooks() {
        String keyword = JOptionPane.showInputDialog(this, "Enter search keyword:");
        if (keyword != null && !keyword.trim().isEmpty()) {
            try {
                List<Book> results = dao.searchBooks(keyword);
                StringBuilder sb = new StringBuilder("Search Results for " + keyword + ":");
                
                if (results.isEmpty()) {
                    sb.append("No results found");
                } else {
                    for (Book b : results) {
                        sb.append(b.getTitle()).append(" by ").append(b.getAuthor()).append(" (Stock: ").append(b.getStock()).append(")");
                    }
                }
                
                JOptionPane.showMessageDialog(this, sb.toString(), "Search Results", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void approveExchange() {
        int row = exchangesTable.getSelectedRow();
        if (row == -1) return;
        
        int exchangeId = (int) exchangesTable.getValueAt(row, 0);
        
        try {
            if (dao.approveExchange(exchangeId, currentUser.getId())) {
                JOptionPane.showMessageDialog(this, "Exchange approved");
                loadExchanges();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void rejectExchange() {
        int row = exchangesTable.getSelectedRow();
        if (row == -1) return;
        
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
}
