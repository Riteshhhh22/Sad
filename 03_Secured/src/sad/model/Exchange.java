package sad.model;

/**  @author eugen */

import java.sql.Timestamp;

public class Exchange {
    private int id;
    private int userId;
    private int offeredBookId;
    private int requestedBookId;
    private String exchangeType;
    private String status;
    private Timestamp requestDate;
    private int approvedBy;
    private String completionDate;
    
    public Exchange() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getOfferedBookId() { return offeredBookId; }
    public void setOfferedBookId(int offeredBookId) { this.offeredBookId = offeredBookId; }
    
    public int getRequestedBookId() { return requestedBookId; }
    public void setRequestedBookId(int requestedBookId) { this.requestedBookId = requestedBookId; }
    
    public String getExchangeType() { return exchangeType; }
    public void setExchangeType(String exchangeType) { this.exchangeType = exchangeType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getRequestDate() { return requestDate; }
    public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }
    
    public int getApprovedBy() { return approvedBy; }
    public void setApprovedBy(int approvedBy) { this.approvedBy = approvedBy; }
    
    public String getCompletionDate() { return completionDate; }
    public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }
}