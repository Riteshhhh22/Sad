package sad.model;

/**  @author eugen */

import java.sql.Date;

public class Loan {
    private int id;
    private int userId;
    private int bookId;
    private String loanType;
    private Date loanDate;
    private Date dueDate;
    private Date returnDate;
    private int approvedBy;
    private String status;
    private String bookTitle;
    private String userName;
    
    public Loan() {}
    
    public Loan(int id, int userId, int bookId, String loanType, Date dueDate, String status) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.loanType = loanType;
        this.dueDate = dueDate;
        this.status = status;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    
    public Date getLoanDate() { return loanDate; }
    public void setLoanDate(Date loanDate) { this.loanDate = loanDate; }
    
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    
    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
    
    public int getApprovedBy() { return approvedBy; }
    public void setApprovedBy(int approvedBy) { this.approvedBy = approvedBy; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}