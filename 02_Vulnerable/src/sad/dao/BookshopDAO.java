package sad.dao;

/**  @author eugen */

import java.sql.*;
import java.util.*;
import java.util.Base64;
import sad.db.DBConnection;
import sad.model.Book;
import sad.model.Exchange;
import sad.model.Loan;
import sad.model.Order;
import sad.model.User;

public class BookshopDAO {
    
    // ==================== AUTHENTICATION ====================
    
    // ===== VULNERABILITY V2: SQL Injection in Login (Authentication Bypass) =====
    // CHANGED: Replaced parameterized query with string concatenation
    // EXPLOIT: Username "admin' --" with any password bypasses authentication
    // OWASP: A03:2021 Injection / A07:2021 Identification and Authentication Failures
    public User login(String username, String password) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            // VULNERABLE: User input concatenated directly into SQL query
            String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            // VULNERABILITY V4 NOTE: Login compares plaintext input against stored passwords
            // New users added with Base64 encoding can never authenticate
            // because this comparison does not decode the stored password
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setCollegeId(rs.getInt("college_id"));
                return user;
            }
            return null;
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
    
    // ==================== USERS ====================
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM users ORDER BY id");
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setCollegeId(rs.getInt("college_id"));
                users.add(user);
            }
            return users;
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
    
    // ===== VULNERABILITY V4: Weak Password Storage (Cryptographic Failure) =====
    // CHANGED: Passwords encoded with Base64 instead of proper hashing (BCrypt/Argon2)
    // EXPLOIT: Base64 is trivially reversible - not encryption. Also breaks login
    //          because login() compares plaintext input vs Base64 stored value
    // OWASP: A02:2021 Cryptographic Failures
    public boolean addUser(User user) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO users (username, password, full_name, email, role, college_id) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            // VULNERABLE: Base64 encoding is NOT encryption - trivially reversible
            // decode with: new String(Base64.getDecoder().decode(encodedPassword))
            String encodedPassword = Base64.getEncoder().encodeToString(user.getPassword().getBytes());
            stmt.setString(2, encodedPassword);
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole());
            stmt.setInt(6, user.getCollegeId());
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    public boolean deleteUser(int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?");
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    // ==================== BOOKS ====================
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM books ORDER BY title");
            
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setPublisher(rs.getString("publisher"));
                book.setYear(rs.getInt("year"));
                book.setPrice(rs.getBigDecimal("price"));
                book.setStock(rs.getInt("stock"));
                book.setCollectionId(rs.getInt("collection_id"));
                book.setEmbargoDate(rs.getString("embargo_date"));
                books.add(book);
            }
            return books;
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
    
    // ===== VULNERABILITY V1: SQL Injection in Book Search (Input Validation) =====
    // CHANGED: Replaced parameterized query with string concatenation
    // EXPLOIT: Search for "' OR '1'='1" returns ALL books including restricted/embargoed
    // OWASP: A03:2021 Injection
    public List<Book> searchBooks(String keyword) throws SQLException {
        List<Book> books = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            // VULNERABLE: User input concatenated directly into SQL query
            String sql = "SELECT * FROM books WHERE title LIKE '%" + keyword + "%' OR author LIKE '%" + keyword + "%' OR isbn LIKE '%" + keyword + "%'";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setStock(rs.getInt("stock"));
                books.add(book);
            }
            return books;
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
    
    public boolean addBook(Book book) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO books (isbn, title, author, publisher, year, price, stock, collection_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getPublisher());
            stmt.setInt(5, book.getYear());
            stmt.setBigDecimal(6, book.getPrice());
            stmt.setInt(7, book.getStock());
            stmt.setInt(8, book.getCollectionId());
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    public boolean updateBookStock(int bookId, int newStock) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("UPDATE books SET stock = ? WHERE id = ?");
            stmt.setInt(1, newStock);
            stmt.setInt(2, bookId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    // ==================== LOANS ====================
    public List<Loan> getAllLoans() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                "SELECT l.*, b.title as book_title, u.username as user_name " +
                "FROM loans l " +
                "LEFT JOIN books b ON l.book_id = b.id " +
                "LEFT JOIN users u ON l.user_id = u.id " +
                "ORDER BY l.loan_date DESC");
            
            while (rs.next()) {
                Loan loan = new Loan();
                loan.setId(rs.getInt("id"));
                loan.setUserId(rs.getInt("user_id"));
                loan.setBookId(rs.getInt("book_id"));
                loan.setLoanType(rs.getString("loan_type"));
                loan.setLoanDate(rs.getDate("loan_date"));
                loan.setDueDate(rs.getDate("due_date"));
                loan.setReturnDate(rs.getDate("return_date"));
                loan.setApprovedBy(rs.getInt("approved_by"));
                loan.setStatus(rs.getString("status"));
                loan.setBookTitle(rs.getString("book_title"));
                loan.setUserName(rs.getString("user_name"));
                loans.add(loan);
            }
            return loans;
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
    
    public List<Loan> getUserLoans(int userId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(
                "SELECT l.*, b.title as book_title FROM loans l " +
                "LEFT JOIN books b ON l.book_id = b.id " +
                "WHERE l.user_id = ? ORDER BY l.loan_date DESC");
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Loan loan = new Loan();
                loan.setId(rs.getInt("id"));
                loan.setBookId(rs.getInt("book_id"));
                loan.setLoanType(rs.getString("loan_type"));
                loan.setLoanDate(rs.getDate("loan_date"));
                loan.setDueDate(rs.getDate("due_date"));
                loan.setReturnDate(rs.getDate("return_date"));
                loan.setStatus(rs.getString("status"));
                loan.setBookTitle(rs.getString("book_title"));
                loans.add(loan);
            }
            return loans;
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
    
    // ===== VULNERABILITY V5: Verbose Error Handling (Information Disclosure) =====
    // CHANGED: Catch block exposes DB URL, username, MySQL version, and full stack trace
    // EXPLOIT: Trigger error with invalid book ID - system internals revealed
    // OWASP: A04:2021 Insecure Design / A05:2021 Security Misconfiguration
    public boolean requestLoan(int userId, int bookId, String loanType) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO loans (user_id, book_id, loan_type, loan_date, due_date, status) " +
                         "VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL " +
                         (loanType.equals("SHORT_TERM") ? "14" : "90") + " DAY), 'PENDING')";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setString(3, loanType);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // VULNERABLE: Exposes full database internals to the user
            Connection errorConn = null;
            try {
                errorConn = DBConnection.getConnection();
                String errorDetail = "SQL ERROR in requestLoan:\n" +
                    "SQL State: " + e.getSQLState() + "\n" +
                    "Error Code: " + e.getErrorCode() + "\n" +
                    "Message: " + e.getMessage() + "\n" +
                    "Connection URL: " + errorConn.getMetaData().getURL() + "\n" +
                    "Database User: " + errorConn.getMetaData().getUserName() + "\n" +
                    "Database Product: " + errorConn.getMetaData().getDatabaseProductVersion() + "\n" +
                    "Stack Trace: " + java.util.Arrays.toString(e.getStackTrace());
                throw new SQLException(errorDetail);
            } finally {
                if (errorConn != null) errorConn.close();
            }
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    // ===== VULNERABILITY V3: Broken Access Control in Loan Approval =====
    // EXISTING METHOD: No role verification - any user ID can approve loans
    // No check that approverId belongs to a LIBRARIAN or ADMIN
    // No check that loan status is PENDING before approving
    // OWASP: A01:2021 Broken Access Control
    public boolean approveLoan(int loanId, int approverId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            // VULNERABLE: No role check on approverId, no status check on loan
            stmt = conn.prepareStatement("UPDATE loans SET approved_by = ?, status = 'APPROVED' WHERE id = ?");
            stmt.setInt(1, approverId);
            stmt.setInt(2, loanId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    // ===== VULNERABILITY V3 (continued): Unrestricted loan approval method =====
    // ADDED: New method with zero access control - any user can approve any loan
    // EXPLOIT: Customer/Guest can call this to self-approve their own loan requests
    // OWASP: A01:2021 Broken Access Control
    public boolean approveAnyLoan(int loanId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            // VULNERABLE: No approver validation, sets approved_by to 0 (invalid user)
            stmt = conn.prepareStatement("UPDATE loans SET status = 'APPROVED', approved_by = 0 WHERE id = ?");
            stmt.setInt(1, loanId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    // ==================== EXCHANGES ====================
    public List<Exchange> getAllExchanges() throws SQLException {
        List<Exchange> exchanges = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM exchanges ORDER BY request_date DESC");
            
            while (rs.next()) {
                Exchange exchange = new Exchange();
                exchange.setId(rs.getInt("id"));
                exchange.setUserId(rs.getInt("user_id"));
                exchange.setOfferedBookId(rs.getInt("offered_book_id"));
                exchange.setRequestedBookId(rs.getInt("requested_book_id"));
                exchange.setExchangeType(rs.getString("exchange_type"));
                exchange.setStatus(rs.getString("status"));
                exchange.setRequestDate(rs.getTimestamp("request_date"));
                exchange.setApprovedBy(rs.getInt("approved_by"));
                exchange.setCompletionDate(rs.getString("completion_date"));
                exchanges.add(exchange);
            }
            return exchanges;
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
    
    public boolean requestExchange(int userId, int offeredBookId, int requestedBookId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO exchanges (user_id, offered_book_id, requested_book_id, status) VALUES (?, ?, ?, 'PENDING')";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, offeredBookId);
            stmt.setInt(3, requestedBookId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    public boolean approveExchange(int exchangeId, int approverId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("UPDATE exchanges SET approved_by = ?, status = 'APPROVED' WHERE id = ?");
            stmt.setInt(1, approverId);
            stmt.setInt(2, exchangeId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    public boolean rejectExchange(int exchangeId, int approverId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("UPDATE exchanges SET approved_by = ?, status = 'REJECTED' WHERE id = ?");
            stmt.setInt(1, approverId);
            stmt.setInt(2, exchangeId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(conn, stmt, null);
        }
    }
    
    // ==================== ORDERS ====================
    public List<Order> getUserOrders(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC");
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
            return orders;
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
    
    // ==================== COLLECTIONS ====================
    public List<Book> getRestrictedBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                "SELECT b.* FROM books b " +
                "JOIN collections c ON b.collection_id = c.id " +
                "WHERE c.is_restricted = TRUE");
            
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setStock(rs.getInt("stock"));
                books.add(book);
            }
            return books;
        } finally {
            DBConnection.close(conn, stmt, rs);
        }
    }
}
