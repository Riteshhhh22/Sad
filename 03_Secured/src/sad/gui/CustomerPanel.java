package sad.gui;

/**  @author eugen */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import sad.dao.BookshopDAO;
import sad.model.Book;
import sad.model.Loan;
import sad.model.Order;
import sad.model.User;

public class CustomerPanel extends JPanel {
    private User currentUser;
    private BookshopDAO dao;
    private JTable booksTable, loansTable, exchangesTable, ordersTable;
    
    public CustomerPanel(User user) {
        this.currentUser = user;
        this.dao = new BookshopDAO();
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(0, 150, 100));
        JLabel headerLabel = new JLabel("Customer Dashboard - " + currentUser.getFullName());
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        JTabbedPane tabs = new JTabbedPane();
        
        // Tab 1: Browse Books
        tabs.addTab("Browse Books", createBrowsePanel());
        
        // Tab 2: Request Loan
        tabs.addTab("Request Loan", createLoanPanel());
        
        // Tab 3: Request Exchange
        tabs.addTab("Request Exchange", createExchangePanel());
        
        // Tab 4: My Loans
        tabs.addTab("My Loans", createMyLoansPanel());
        
        // Tab 5: My Orders
        tabs.addTab("My Orders", createOrdersPanel());
        
        add(tabs, BorderLayout.CENTER);
    }
    
    private JPanel createBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        booksTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton searchBtn = new JButton("Search");
        JButton detailsBtn = new JButton("Details");
        JButton purchaseBtn = new JButton("Purchase");
        
        refreshBtn.addActionListener(e -> loadBooks());
        searchBtn.addActionListener(e -> searchBooks());
        detailsBtn.addActionListener(e -> viewBookDetails());
        purchaseBtn.addActionListener(e -> purchaseBook());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(searchBtn);
        buttonPanel.add(detailsBtn);
        buttonPanel.add(purchaseBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLoanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JComboBox<String> bookCombo = new JComboBox<>();
        JRadioButton shortTerm = new JRadioButton("Short Term (14 days)");
        JRadioButton longTerm = new JRadioButton("Long Term (90 days)");
        ButtonGroup group = new ButtonGroup();
        group.add(shortTerm);
        group.add(longTerm);
        shortTerm.setSelected(true);
        
        try {
            List<Book> books = dao.getAllBooks();
            for (Book b : books) {
                bookCombo.addItem(b.getId() + ": " + b.getTitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        formPanel.add(new JLabel("Select Book:"));
        formPanel.add(bookCombo);
        formPanel.add(new JLabel("Loan Type:"));
        formPanel.add(shortTerm);
        formPanel.add(new JLabel(""));
        formPanel.add(longTerm);
        
        JButton submitBtn = new JButton("Request Loan");
        submitBtn.addActionListener(e -> {
            String selected = (String) bookCombo.getSelectedItem();
            int bookId = Integer.parseInt(selected.split(":")[0]);
            String type = shortTerm.isSelected() ? "SHORT_TERM" : "LONG_TERM";
            
            try {
                if (dao.requestLoan(currentUser.getId(), bookId, type)) {
                    JOptionPane.showMessageDialog(this, "Loan request submitted");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(submitBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createExchangePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JComboBox<String> offerCombo = new JComboBox<>();
        JComboBox<String> requestCombo = new JComboBox<>();
        
        try {
            List<Book> books = dao.getAllBooks();
            for (Book b : books) {
                String item = b.getId() + ": " + b.getTitle();
                offerCombo.addItem(item);
                requestCombo.addItem(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        formPanel.add(new JLabel("Book You Offer:"));
        formPanel.add(offerCombo);
        formPanel.add(new JLabel("Book You Want:"));
        formPanel.add(requestCombo);
        formPanel.add(new JLabel(""));
        
        JButton submitBtn = new JButton("Submit Exchange");
        submitBtn.addActionListener(e -> {
            String offer = (String) offerCombo.getSelectedItem();
            String request = (String) requestCombo.getSelectedItem();
            int offerId = Integer.parseInt(offer.split(":")[0]);
            int requestId = Integer.parseInt(request.split(":")[0]);
            
            try {
                if (dao.requestExchange(currentUser.getId(), offerId, requestId)) {
                    JOptionPane.showMessageDialog(this, "Exchange request submitted");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(submitBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMyLoansPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        loansTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(loansTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadMyLoans());
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        loadMyLoans();
        
        return panel;
    }
    
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        ordersTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadMyOrders());
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        loadMyOrders();
        
        return panel;
    }
    
    private void loadData() {
        loadBooks();
        loadMyLoans();
        loadMyOrders();
    }
    
    private void loadBooks() {
        try {
            List<Book> books = dao.getAllBooks();
            String[] columns = {"ID", "Title", "Author", "Price", "Stock"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Book b : books) {
                model.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), 
                    b.getPrice() != null ? "$" + b.getPrice() : "N/A", 
                    b.getStock()
                });
            }
            booksTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadMyLoans() {
        try {
            List<Loan> loans = dao.getUserLoans(currentUser.getId());
            String[] columns = {"Book", "Type", "Due Date", "Status"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Loan l : loans) {
                model.addRow(new Object[]{
                    l.getBookTitle(), l.getLoanType(), l.getDueDate(), l.getStatus()
                });
            }
            loansTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadMyOrders() {
        try {
            List<Order> orders = dao.getUserOrders(currentUser.getId());
            String[] columns = {"Order ID", "Date", "Total", "Status"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Order o : orders) {
                model.addRow(new Object[]{
                    o.getId(), o.getOrderDate(), 
                    o.getTotalAmount() != null ? "$" + o.getTotalAmount() : "$0.00", 
                    o.getStatus()
                });
            }
            ordersTable.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void searchBooks() {
        String keyword = JOptionPane.showInputDialog(this, "Enter search keyword:");
        if (keyword != null && !keyword.trim().isEmpty()) {
            try {
                List<Book> results = dao.searchBooks(keyword);
                StringBuilder sb = new StringBuilder("Search Results:");
                
                for (Book b : results) {
                    sb.append(b.getTitle()).append(" by ").append(b.getAuthor())
                      .append(" - Stock: ").append(b.getStock()).append("");
                }
                
                JOptionPane.showMessageDialog(this, sb.toString(), "Search Results", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void viewBookDetails() {
        int row = booksTable.getSelectedRow();
        if (row == -1) return;
        
        String title = (String) booksTable.getValueAt(row, 1);
        String author = (String) booksTable.getValueAt(row, 2);
        String price = (String) booksTable.getValueAt(row, 3);
        int stock = (int) booksTable.getValueAt(row, 4);
        
        JOptionPane.showMessageDialog(this,"Book Details:" + "Title: " + title + "" + "Author: " + author + "" +
            "Price: " + price + "" + "Available: " + stock + " copies",
            "Book Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void purchaseBook() {
        int row = booksTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to purchase");
            return;
        }
        
        String title = (String) booksTable.getValueAt(row, 1);
        int stock = (int) booksTable.getValueAt(row, 4);
        
        if (stock <= 0) {
            JOptionPane.showMessageDialog(this, "Sorry, this book is out of stock");
            return;
        }
        
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Confirm purchase of \"" + title + "\"?",
        "Confirm Purchase",
        JOptionPane.YES_NO_OPTION
);
            
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, 
                "Purchase successful! (Simulated)",
                "Purchase Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
