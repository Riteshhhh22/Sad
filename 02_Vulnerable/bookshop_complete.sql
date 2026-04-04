DROP DATABASE bookshop;
-- Complete Bookshop Database Schema with Sample Data
CREATE DATABASE IF NOT EXISTS bookshop;
USE bookshop;

-- Colleges table (for inter-college exchanges)
CREATE TABLE colleges (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100)
);

-- Collections table (for restricted materials)
CREATE TABLE collections (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    is_restricted BOOLEAN DEFAULT FALSE
);

-- Users table (all roles)
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    role ENUM('ADMIN', 'LIBRARIAN', 'RESEARCHER', 'CUSTOMER', 'GUEST') NOT NULL,
    college_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (college_id) REFERENCES colleges(id)
);

-- Books catalogue
CREATE TABLE books (
    id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20),
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    publisher VARCHAR(100),
    year INT,
    price DECIMAL(10,2),
    stock INT DEFAULT 0,
    collection_id INT,
    embargo_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (collection_id) REFERENCES collections(id)
);

-- Orders (purchases)
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2),
    status VARCHAR(50) DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order items
CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    book_id INT NOT NULL,
    quantity INT,
    price DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- Loans
CREATE TABLE loans (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    loan_type ENUM('SHORT_TERM', 'LONG_TERM') NOT NULL,
    loan_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    approved_by INT,
    status VARCHAR(50) DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- Book exchanges
CREATE TABLE exchanges (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    offered_book_id INT,
    requested_book_id INT,
    exchange_type VARCHAR(50) DEFAULT 'STANDARD',
    status VARCHAR(50) DEFAULT 'PENDING',
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by INT,
    completion_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (offered_book_id) REFERENCES books(id),
    FOREIGN KEY (requested_book_id) REFERENCES books(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- Research annotations
CREATE TABLE annotations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    annotation_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_public BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- Audit log (for security exercises)
CREATE TABLE audit_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    details TEXT
);

-- ============================================================================
-- SAMPLE DATA
-- ============================================================================

-- Colleges
INSERT INTO colleges (id, name, location) VALUES
(1, 'University of Technology', 'Boston'),
(2, 'City College', 'New York'),
(3, 'State University', 'Chicago');

-- Collections
INSERT INTO collections (id, name, is_restricted) VALUES
(1, 'General Collection', FALSE),
(2, 'Research Archive', TRUE),
(3, 'Rare Books', TRUE),
(4, 'Textbooks', FALSE);

-- Users (with plaintext passwords - INTENTIONAL VULNERABILITY)
INSERT INTO users (id, username, password, full_name, email, role, college_id) VALUES
(1, 'admin', 'admin123', 'System Admin', 'admin@bookshop.com', 'ADMIN', 1),
(2, 'librarian', 'lib456', 'Jane Librarian', 'jane@bookshop.com', 'LIBRARIAN', 1),
(3, 'researcher', 'research789', 'Dr. Smith', 'smith@university.edu', 'RESEARCHER', 2),
(4, 'customer', 'cust123', 'John Customer', 'john@email.com', 'CUSTOMER', 1),
(5, 'guest', 'guest123', 'Guest User', 'guest@temp.com', 'GUEST', 3),
(6, 'alice', 'alicepass', 'Alice Johnson', 'alice@research.org', 'RESEARCHER', 2),
(7, 'bob', 'bobpass', 'Bob Wilson', 'bob@customer.com', 'CUSTOMER', 1),
(8, 'charlie', 'charlie123', 'Charlie Brown', 'charlie@library.org', 'LIBRARIAN', 3);

-- Books
INSERT INTO books (id, isbn, title, author, publisher, year, price, stock, collection_id, embargo_date) VALUES
(1, '978-0134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley', 2018, 45.99, 10, 1, NULL),
(2, '978-0596009205', 'Head First Java', 'Kathy Sierra', 'O\'Reilly', 2005, 35.50, 8, 4, NULL),
(3, '978-1118873797', 'Java Programming', 'Joyce Farrell', 'Cengage', 2016, 120.00, 5, 1, NULL),
(4, '978-0262033848', 'Introduction to Algorithms', 'Cormen', 'MIT Press', 2009, 89.95, 3, 2, '2024-12-31'),
(5, '978-0123744579', 'Computer Networks', 'Tanenbaum', 'Pearson', 2010, 95.00, 2, 2, '2024-06-30'),
(6, '978-1449365035', 'Python Data Science', 'McKinney', 'O\'Reilly', 2017, 55.00, 12, 1, NULL),
(7, '978-1491950296', 'Machine Learning', 'Géron', 'O\'Reilly', 2019, 65.00, 7, 2, '2024-09-30'),
(8, '978-1593279509', 'The Linux Command Line', 'Shotts', 'No Starch', 2019, 39.95, 15, 1, NULL),
(9, '978-0134757599', 'Clean Code', 'Robert Martin', 'Prentice Hall', 2008, 49.99, 6, 1, NULL),
(10, '978-1491910390', 'Database Systems', 'Garcia-Molina', 'Pearson', 2014, 110.00, 4, 2, '2024-03-31');

-- Orders
INSERT INTO orders (id, user_id, order_date, total_amount, status) VALUES
(1, 4, '2026-01-15 10:30:00', 81.49, 'COMPLETED'),
(2, 7, '2026-02-03 14:20:00', 45.99, 'SHIPPED'),
(3, 4, '2026-02-10 09:15:00', 110.00, 'PENDING');

-- Order items
INSERT INTO order_items (order_id, book_id, quantity, price) VALUES
(1, 1, 1, 45.99),
(1, 2, 1, 35.50),
(2, 1, 1, 45.99),
(3, 10, 1, 110.00);

-- Loans
INSERT INTO loans (id, user_id, book_id, loan_type, loan_date, due_date, return_date, approved_by, status) VALUES
(1, 4, 2, 'SHORT_TERM', '2026-02-01', '2026-02-15', NULL, 2, 'APPROVED'),
(2, 3, 4, 'LONG_TERM', '2026-01-10', '2026-04-10', NULL, 2, 'APPROVED'),
(3, 6, 5, 'LONG_TERM', '2026-01-15', '2026-04-15', '2026-02-20', 8, 'RETURNED'),
(4, 7, 3, 'SHORT_TERM', '2026-02-05', '2026-02-19', NULL, 2, 'APPROVED'),
(5, 4, 8, 'SHORT_TERM', '2026-02-12', '2026-02-26', NULL, NULL, 'PENDING');

-- Exchanges
INSERT INTO exchanges (id, user_id, offered_book_id, requested_book_id, exchange_type, status, request_date, approved_by, completion_date) VALUES
(1, 4, 2, 5, 'STANDARD', 'APPROVED', '2026-01-20 11:30:00', 2, '2026-01-25'),
(2, 7, 3, 1, 'STANDARD', 'PENDING', '2026-02-08 16:45:00', NULL, NULL),
(3, 3, 8, 9, 'PREMIUM', 'REJECTED', '2026-02-01 09:20:00', 8, NULL),
(4, 4, 1, 7, 'STANDARD', 'PENDING', '2026-02-14 13:10:00', NULL, NULL);

-- Research annotations
INSERT INTO annotations (user_id, book_id, annotation_text, is_public) VALUES
(3, 4, 'Excellent resource for algorithms research. Chapter 7 has detailed proofs.', TRUE),
(6, 5, 'Needs updated section on modern protocols. Outdated.', FALSE),
(3, 9, 'Best practices section is essential reading for students.', TRUE);

-- Audit log
INSERT INTO audit_log (user_id, action, ip_address, details) VALUES
(1, 'LOGIN', '192.168.1.100', 'Admin login from office'),
(2, 'LOAN_APPROVAL', '192.168.1.101', 'Approved loan #1'),
(3, 'RESEARCH_ACCESS', '192.168.1.102', 'Accessed restricted collection'),
(4, 'ORDER_PLACED', '192.168.1.103', 'Order #1 placed'),
(5, 'GUEST_BROWSE', '192.168.1.104', 'Browsed catalogue');
