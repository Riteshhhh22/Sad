package sad.gui;

/**  @author eugen */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import sad.dao.BookshopDAO;
import sad.model.Book;
import sad.model.Loan;
import sad.model.User;

public class ResearcherPanel extends JPanel {
    private User currentUser;
    private BookshopDAO dao;
    private JTable booksTable, loansTable, restrictedTable;
    
    public ResearcherPanel(User user) {
        this.currentUser = user;
        this.dao = new BookshopDAO();
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(0, 100, 100));
        JLabel headerLabel = new JLabel("Researcher Portal - " + currentUser.getFullName());
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        JTabbedPane tabs = new JTabbedPane();
        
        // Tab 1: Browse Books
        tabs.addTab("Browse Books", createBrowsePanel());
        
        // Tab 2: Request Loans
        tabs.addTab("Request Loans", createLoanRequestPanel());
        
        // Tab 3: Restricted Collection
        tabs.addTab("Restricted Collection", createRestrictedPanel());
        
        // Tab 4: My Loans
        tabs.addTab("My Loans", createMyLoansPanel());
        
        // Tab 5: Request Exchange
        tabs.addTab("Request Exchange", createExchangePanel());
        
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
        JButton detailsBtn = new JButton("View Details");
        
        refreshBtn.addActionListener(e -> loadBooks());
        searchBtn.addActionListener(e -> searchBooks());
        detailsBtn.addActionListener(e -> viewBookDetails());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(searchBtn);
        buttonPanel.add(detailsBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLoanRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JComboBox<String> bookCombo = new JComboBox<>();
        JRadioButton shortTermBtn = new JRadioButton("Short Term (14 days)");
        JRadioButton longTermBtn = new JRadioButton("Long Term (90 days)");
        ButtonGroup group = new ButtonGroup();
        group.add(shortTermBtn);
        group.add(longTermBtn);
        shortTermBtn.setSelected(true);
        
        JTextArea purposeArea = new JTextArea(5, 20);
        
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
        formPanel.add(shortTermBtn);
        formPanel.add(new JLabel(""));
        formPanel.add(longTermBtn);
        formPanel.add(new JLabel("Research Purpose:"));
        formPanel.add(new JScrollPane(purposeArea));
        
        JButton submitBtn = new JButton("Submit Loan Request");
        submitBtn.addActionListener(e -> {
            String selected = (String) bookCombo.getSelectedItem();
            int bookId = Integer.parseInt(selected.split(":")[0]);
            String loanType = shortTermBtn.isSelected() ? "SHORT_TERM" : "LONG_TERM";
            
            try {
                if (dao.requestLoan(currentUser.getId(), bookId, loanType)) {
                    JOptionPane.showMessageDialog(this, "Loan request submitted successfully");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(submitBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createRestrictedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        restrictedTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(restrictedTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton accessBtn = new JButton("Request Access");
        accessBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Access request submitted. You will be notified when approved.",
                "Access Request", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(accessBtn, BorderLayout.SOUTH);
        
        loadRestrictedBooks();
        
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
    
    private JPanel createExchangePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JComboBox<String> offeredBookCombo = new JComboBox<>();
        JComboBox<String> requestedBookCombo = new JComboBox<>();
        
        try {
            List<Book> books = dao.getAllBooks();
            for (Book b : books) {
                String item = b.getId() + ": " + b.getTitle();
                offeredBookCombo.addItem(item);
                requestedBookCombo.addItem(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        formPanel.add(new JLabel("Your Book to Offer:"));
        formPanel.add(offeredBookCombo);
        formPanel.add(new JLabel("Book You Want:"));
        formPanel.add(requestedBookCombo);
        formPanel.add(new JLabel(""));
        
        JButton submitBtn = new JButton("Submit Exchange Request");
        submitBtn.addActionListener(e -> {
            String offered = (String) offeredBookCombo.getSelectedItem();
            String requested = (String) requestedBookCombo.getSelectedItem();
            int offeredId = Integer.parseInt(offered.split(":")[0]);
            int requestedId = Integer.parseInt(requested.split(":")[0]);
            
            try {
                if (dao.requestExchange(currentUser.getId(), offeredId, requestedId)) {
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
    
    private void loadData() {
        loadBooks();
        loadRestrictedBooks();
        loadMyLoans();
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
    
    private void loadRestrictedBooks() {
        try {
            List<Book> books = dao.getRestrictedBooks();
            String[] columns = {"ID", "Title", "Author", "Stock"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Book b : books) {
                model.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), b.getStock()
                });
            }
            restrictedTable.setModel(model);
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
    
    private void searchBooks() {
        String keyword = JOptionPane.showInputDialog(this, "Enter search keyword:");
        if (keyword != null && !keyword.trim().isEmpty()) {
            try {
                List<Book> results = dao.searchBooks(keyword);
                StringBuilder sb = new StringBuilder("Search Results:");
                
                for (Book b : results) {
                    sb.append(b.getTitle()).append(" by ").append(b.getAuthor()).append("");
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
        
        JOptionPane.showMessageDialog(this,   "Book Details:Title: " + title + "Author: " + author,"Book Information", JOptionPane.INFORMATION_MESSAGE);
    }
}
