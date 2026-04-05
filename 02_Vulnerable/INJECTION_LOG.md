# Injection Log — Intentional Vulnerability Injection

## Overview
5 exploitable vulnerabilities injected across 5 OWASP categories (Input Validation, Authentication, Access Control, Cryptographic Practices, Error Handling). All 5 exploit library business workflows. All changes confined to BookshopDAO.java.

---

## V1 — SQL Injection in Book Search

| Field | Detail |
|-------|--------|
| **ID** | V1 |
| **Category** | Input Validation |
| **Location** | BookshopDAO.java → searchBooks(), line ~170. Parameterized PreparedStatement replaced with string concatenation. |
| **Insecure Design Decision** | User-supplied search keyword concatenated directly into SQL WHERE clause without sanitization or parameterization. Developer chose string building for convenience, bypassing JDBC built-in protection. |
| **Why It Arises** | No input validation at the DAO layer. Application trusts GUI text fields to only receive benign input. No defence-in-depth — the single protection layer (PreparedStatement) was removed, leaving zero barriers between user input and the database engine. |
| **User Impact** | Any user including Guest can extract the entire catalogue including restricted research archives (collection_id=2) and rare books (collection_id=3). Embargoed titles with future release dates become visible. Users lose trust that the library enforces access policies on sensitive academic materials. |
| **System Trust Impact** | Database query boundary completely compromised. Attacker can escalate from data extraction to schema enumeration via UNION-based injection. Undermines confidentiality of entire database and violates the library obligation to protect restricted collections. |

**Exploit Steps:**
1. Login as Guest (or any role)
2. Navigate to Search Books
3. Enter: `' OR '1'='1`
4. All 10 books returned including 4 restricted/embargoed titles

---

## V2 — Authentication Bypass via SQL Injection

| Field | Detail |
|-------|--------|
| **ID** | V2 |
| **Category** | Authentication |
| **Location** | BookshopDAO.java → login(), line ~33. Parameterized authentication query replaced with string concatenation. |
| **Insecure Design Decision** | Authentication credentials interpolated directly into SQL string. Login query treated like any other string operation, ignoring that authentication is the highest-trust boundary. No parameterization, no sanitization, no prepared statement binding. |
| **Why It Arises** | No authentication framework — login is a single raw SQL query with no separation between auth layer and data access layer. LoginFrame passes user input directly to dao.login() with zero preprocessing. Flat architecture means one coding mistake compromises the entire auth system. |
| **User Impact** | Any anonymous user gains full administrator access without credentials. Username `admin' --` nullifies the password check via SQL comment. Attacker inherits all admin privileges: user management, book management, loan/exchange approval. Every user account is compromised. |
| **System Trust Impact** | Total authentication failure. System cannot distinguish legitimate admins from attackers. Audit trails meaningless — actions attributed to hijacked admin account. Library role-based access control collapses entirely. GDPR compliance violated as unauthorized parties access personal data of all registered users. |

**Exploit Steps:**
1. Open login screen
2. Username: `admin' --`
3. Password: `anything`
4. Click Login → Welcome message: "System Admin" with ADMIN role
5. Full Admin Dashboard accessible

---

## V3 — Broken Access Control in Loan Approval

| Field | Detail |
|-------|--------|
| **ID** | V3 |
| **Category** | Access Control |
| **Location** | BookshopDAO.java → approveLoan() (line ~310) has no role verification. New approveAnyLoan() added (line ~325) accepts zero authorization, sets approved_by=0. |
| **Insecure Design Decision** | DAO accepts any integer as approverId without verifying LIBRARIAN or ADMIN role. Authorization enforced only at GUI layer (MainFrame routes to role panels). Added approveAnyLoan() removes even the pretence of approver identity. |
| **Why It Arises** | Application confuses presentation-layer routing with access control. Because Customers see CustomerPanel not LibrarianPanel, developer assumed they cannot invoke librarian functions. Classic security-by-obscurity failure — business logic trusts UI to enforce authorization. |
| **User Impact** | Customer or Researcher can approve their own loan requests, bypassing librarian review. Defeats separation of duties protecting library physical inventory. Malicious user could request loans on high-value rare books and self-approve, enabling unauthorized removal of library assets. |
| **System Trust Impact** | Loan approval workflow — a core library business process — loses all integrity. Database approved_by=0 makes audit trails unreliable. Library cannot determine which loans were legitimately approved by staff versus self-approved. Insurance and accountability for lost/damaged books compromised because chain of custody is broken. |

**Exploit Steps:**
1. Login as Customer (customer / cust123)
2. Request a new loan → status PENDING
3. approveAnyLoan(loanId) called → status changes to APPROVED
4. MySQL: `SELECT * FROM loans WHERE approved_by = 0` shows illegitimate approval

---

## V4 — Weak Password Storage (Reversible Encoding)

| Field | Detail |
|-------|--------|
| **ID** | V4 |
| **Category** | Cryptographic Practices |
| **Location** | BookshopDAO.java → addUser(), line ~99. Password encoded with Base64.getEncoder().encodeToString() instead of cryptographic hashing (BCrypt, Argon2, PBKDF2). |
| **Insecure Design Decision** | Developer used Base64 encoding believing it provides password protection. Base64 is a reversible encoding scheme for data transport, not security — provides zero cryptographic protection. Additionally login() still compares raw plaintext, creating authentication inconsistency where new users can never log in. |
| **Why It Arises** | Confusion between encoding and encryption/hashing. No dependency on proper cryptographic library. No integration testing — mismatch between addUser() (Base64) and login() (plaintext comparison) would be caught by any login test for new users. |
| **User Impact** | Two impacts: (1) New users cannot login — Base64 password never matches plaintext input, causing denial of service for new accounts. Registration workflow broken. (2) Database breach exposes all passwords instantly — e.g. YWRtaW4xMjM= decodes to admin123 in milliseconds. Users who reuse passwords face credential stuffing attacks. |
| **System Trust Impact** | Password storage offers no protection against database breaches. Unlike BCrypt (computationally expensive, salted, one-way), Base64 is reversible in O(1) time. Combined with V1 or V2, attacker gets every user plaintext password. Library user data protection obligations (GDPR Article 32) violated. Broken registration damages operational trust. |

**Exploit Steps:**
1. Login as Admin → Add User with password "test123"
2. MySQL: `SELECT username, password FROM users WHERE username = 'testuser'`
3. Password shown as `dGVzdDEyMw==` (Base64, not hashed)
4. Decode: `echo "dGVzdDEyMw==" | base64 -d` → outputs "test123"
5. Login as testuser/test123 → FAILS (plaintext vs Base64 mismatch)

---

## V5 — Verbose Error Handling Exposing System Internals

| Field | Detail |
|-------|--------|
| **ID** | V5 |
| **Category** | Error Handling |
| **Location** | BookshopDAO.java → requestLoan(), line ~290. Catch block constructs detailed error containing SQL state, error code, JDBC connection URL, database username, MySQL version, and full Java stack trace. Thrown as SQLException that surfaces in GUI dialog. |
| **Insecure Design Decision** | Developer built a debugging error handler never removed before deployment. Catch block actively queries DatabaseMetaData to retrieve connection details and appends to error. Full technical details propagated to presentation layer and displayed in JOptionPane dialog. |
| **Why It Arises** | No separation between development and production error handling. No logging framework (Log4j, SLF4J). No error handling policy — each method handles exceptions ad hoc. GUI displays ex.getMessage() directly without filtering, creating uncontrolled information channel from database to user. |
| **User Impact** | Failed loan request shows error dialog containing: database URL (jdbc:mysql://localhost:3306/bookshop), database username (root), MySQL version, full stack trace with class names and line numbers. Provides complete reconnaissance map of backend. Attacker can craft targeted SQL injection payloads and identify MySQL version for known CVE exploits. |
| **System Trust Impact** | Error channel becomes intelligence-gathering tool. Exposed metadata: (1) database host/port confirming attack surface, (2) username root confirming overprivileged access, (3) MySQL version enabling version-specific exploits, (4) Java class hierarchy revealing architecture. Combined with V1/V2, transforms blind injection into fully informed attack. |

**Exploit Steps:**
1. Login as Customer (customer / cust123)
2. Request a loan that triggers SQL error
3. Error dialog shows:
   - Connection URL: jdbc:mysql://localhost:3306/bookshop
   - Database User: root
   - Database Product: MySQL 8.0.xx
   - Full stack trace
4. Screenshot the dialog as evidence

---

## Summary Matrix

| ID | Category | Library Workflow | OWASP Reference |
|----|----------|-----------------|-----------------|
| V1 | Input Validation | Book search / catalogue | A03:2021 Injection |
| V2 | Authentication | User login | A03:2021 + A07:2021 |
| V3 | Access Control | Loan approval | A01:2021 Broken Access Control |
| V4 | Cryptographic Practices | User registration | A02:2021 Cryptographic Failures |
| V5 | Error Handling | Loan request | A04:2021 + A05:2021 |

**Categories spanned:** 5 of 6 (Input Validation, Authentication, Access Control, Cryptographic Practices, Error Handling)
**Library workflows exploited:** 5 (Book search, Login, Loan approval, User registration, Loan request) — exceeds minimum of 3
