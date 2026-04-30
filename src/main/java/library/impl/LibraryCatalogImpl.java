package library.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import library.core.AbstractLibraryCatalog;
import library.models.Book;
import library.models.BorrowRecord;
import library.persistence.CatalogPersistence;

/**
 * Concrete implementation of the catalog with simple search and circulation
 * logic.
 */
public class LibraryCatalogImpl extends AbstractLibraryCatalog {
    private static final int UG_STUDENT_MAX_ACTIVE_BORROWS = 3;
    private static final int UG_STUDENT_LOAN_DAYS = 10;
    private static final int G_STUDENT_MAX_ACTIVE_BORROWS = 5;
    private static final int G_STUDENT_LOAN_DAYS = 15;
    private static final int FACULTY_MAX_ACTIVE_BORROWS = 7;
    private static final int FACULTY_LOAN_DAYS = 20;

    private List<BorrowRecord> borrowHistory;
    private BorrowHistoryPersistenceTXT historyPersistence;

    public LibraryCatalogImpl(CatalogPersistence persistence) {
        super(persistence);
        this.borrowHistory = new ArrayList<>();
        this.historyPersistence = new BorrowHistoryPersistenceTXT("borrow_history.txt");
        loadBorrowHistory();
    }

    @Override
    public List<Book> searchByTitle(String title) {
        List<Book> result = new ArrayList<>();
        String key = safeLower(title);
        for (Book book : catalog.values()) {
            if (safeLower(book.getTitle()).contains(key)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        List<Book> result = new ArrayList<>();
        String key = safeLower(author);
        for (Book book : catalog.values()) {
            if (safeLower(book.getAuthor()).contains(key)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> searchByGenre(String genre) {
        List<Book> result = new ArrayList<>();
        String key = safeLower(genre);
        for (Book book : catalog.values()) {
            if (safeLower(book.getGenre()).contains(key)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public boolean borrowBook(String isbn) {
        return borrowBook(isbn, "Unknown", "Unknown");
    }

    @Override
    public boolean borrowBook(String isbn, String borrowerName, String userRole) {
        Book book = catalog.get(isbn);
        if (book == null) {
            return false;
        }

        int roleBorrowLimit = getBorrowLimitForRole(userRole);
        if (roleBorrowLimit > 0 && getActiveBorrowCountForUser(borrowerName) >= roleBorrowLimit) {
            return false;
        }

        // Borrow logic
        boolean borrowed = book.borrowBook();
        if (borrowed) {
            LocalDate issue = LocalDate.now();
            String issueDate = issue.toString();
            int loanDays = getLoanDaysForRole(userRole);
            String dueDate = loanDays > 0 ? issue.plusDays(loanDays).toString() : "";
            book.setLastIssueDate(issueDate);
            borrowHistory.add(new BorrowRecord(
                    book.getIsbn(),
                    book.getTitle(),
                    borrowerName,
                    userRole,
                    issueDate,
                    dueDate,
                    "BORROW"));
        }
        return borrowed;
    }

    @Override
    public boolean returnBook(String isbn) {
        return returnBook(isbn, "Unknown", "Unknown");
    }

    @Override
    public boolean returnBook(String isbn, String borrowerName, String userRole) {
        Book book = catalog.get(isbn);
        if (book == null) {
            return false;
        }

        boolean returned = book.returnBook();
        if (returned) {
            borrowHistory.add(new BorrowRecord(
                    book.getIsbn(),
                    book.getTitle(),
                    borrowerName,
                    userRole,
                    LocalDate.now().toString(),
                    "",
                    "RETURN"));
        }
        return returned;
    }

    @Override
    public boolean updateBookInfo(String isbn, String title, String author, String genre) {
        Book book = catalog.get(isbn);
        if (book == null) {
            return false;
        }

        if (title != null && !title.trim().isEmpty()) {
            book.setTitle(title.trim());
        }
        if (author != null && !author.trim().isEmpty()) {
            book.setAuthor(author.trim());
        }
        if (genre != null && !genre.trim().isEmpty()) {
            book.setGenre(genre.trim());
        }

        return true;
    }

    @Override
    public boolean deleteBook(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        return catalog.remove(isbn) != null;
    }

    @Override
    public List<Book> getAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Book book : catalog.values()) {
            if (book.getAvailableCopies() > 0) {
                available.add(book);
            }
        }
        return available;
    }

    @Override
    public List<BorrowRecord> getBorrowHistory() {
        return new ArrayList<>(borrowHistory);
    }

    @Override
    public List<Book> getTopBorrowedBooks(int limit) {
        List<Book> books = new ArrayList<>(catalog.values());
        books.sort((left, right) -> {
            int countCompare = Integer.compare(right.getBorrowCount(), left.getBorrowCount());
            if (countCompare != 0) {
                return countCompare;
            }
            String leftTitle = left.getTitle() == null ? "" : left.getTitle();
            String rightTitle = right.getTitle() == null ? "" : right.getTitle();
            return leftTitle.compareToIgnoreCase(rightTitle);
        });

        if (limit <= 0 || books.isEmpty()) {
            return new ArrayList<>();
        }

        int cappedLimit = Math.min(limit, books.size());
        return new ArrayList<>(books.subList(0, cappedLimit));
    }

    @Override
    public int getActiveBorrowCountForUser(String borrowerName) {
        if (borrowerName == null || borrowerName.trim().isEmpty()) {
            return 0;
        }

        int activeCount = 0;
        for (BorrowRecord record : borrowHistory) {
            if (!borrowerName.equals(record.getBorrowerName())) {
                continue;
            }

            if ("BORROW".equalsIgnoreCase(record.getAction())) {
                activeCount++;
            } else if ("RETURN".equalsIgnoreCase(record.getAction()) && activeCount > 0) {
                activeCount--;
            }
        }
        return activeCount;
    }

    @Override
    public void saveBorrowHistory() {
        historyPersistence.saveHistory(borrowHistory);
    }

    @Override
    public void loadBorrowHistory() {
        this.borrowHistory = historyPersistence.loadHistory();
    }

    private String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase();
    }

    private boolean isUGStudentRole(String userRole) {
        return userRole != null && "ug_student".equalsIgnoreCase(userRole.trim());
    }

    private boolean isGStudentRole(String userRole) {
        return userRole != null && "g_student".equalsIgnoreCase(userRole.trim());
    }

    private boolean isFacultyRole(String userRole) {
        return userRole != null && "faculty".equalsIgnoreCase(userRole.trim());
    }

    private int getBorrowLimitForRole(String userRole) {
        if (isUGStudentRole(userRole)) {
            return UG_STUDENT_MAX_ACTIVE_BORROWS;
        }
        if (isGStudentRole(userRole)) {
            return G_STUDENT_MAX_ACTIVE_BORROWS;
        }
        if (isFacultyRole(userRole)) {
            return FACULTY_MAX_ACTIVE_BORROWS;
        }
        return -1;
    }

    private int getLoanDaysForRole(String userRole) {
        if (isUGStudentRole(userRole)) {
            return UG_STUDENT_LOAN_DAYS;
        }
        if (isGStudentRole(userRole)) {
            return G_STUDENT_LOAN_DAYS;
        }
        if (isFacultyRole(userRole)) {
            return FACULTY_LOAN_DAYS;
        }
        return 0;
    }
}