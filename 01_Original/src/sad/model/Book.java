package sad.model;

/**  @author eugen */

import java.math.BigDecimal;

public class Book {
    private int id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int year;
    private BigDecimal price;
    private int stock;
    private int collectionId;
    private String embargoDate;
    
    public Book() {}
    
    public Book(int id, String title, String author, int stock) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.stock = stock;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    
    public int getCollectionId() { return collectionId; }
    public void setCollectionId(int collectionId) { this.collectionId = collectionId; }
    
    public String getEmbargoDate() { return embargoDate; }
    public void setEmbargoDate(String embargoDate) { this.embargoDate = embargoDate; }
    
    @Override
    public String toString() {
        return title + " by " + author;
    }
}