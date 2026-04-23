package library.models;

/**
 * Stores one borrowing activity for history view.
 */
public class BorrowRecord {
    private String isbn;
    private String bookTitle;
    private String borrowerName;
    private String userRole;
    private String issueDate;
    private String dueDate;
    private String action;

    public BorrowRecord(String isbn, String bookTitle, String borrowerName, String userRole, String issueDate,
            String action) {
        this(isbn, bookTitle, borrowerName, userRole, issueDate, "", action);
    }

    public BorrowRecord(String isbn, String bookTitle, String borrowerName, String userRole, String issueDate,
            String dueDate, String action) {
        this.isbn = isbn;
        this.bookTitle = bookTitle;
        this.borrowerName = borrowerName;
        this.userRole = userRole;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.action = action;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getAction() {
        return action;
    }
}