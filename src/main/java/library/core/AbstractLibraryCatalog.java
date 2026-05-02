package library.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import library.models.Book;
import library.models.BorrowRecord;
import library.persistence.CatalogPersistence;

// classes define search and borrow rules.

public abstract class AbstractLibraryCatalog {
    protected Map<String, Book> catalog;
    protected CatalogPersistence persistence;

    public AbstractLibraryCatalog(CatalogPersistence persistence) {
        this.persistence = persistence;
        this.catalog = new HashMap<>();
        loadCatalog();
    }

    public void addBook(Book book) {
        if (book == null || book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            return;
        }

        Book existing = catalog.get(book.getIsbn());
        if (existing != null) {
            existing.setTotalCopies(existing.getTotalCopies() + book.getTotalCopies());
            existing.setAvailableCopies(existing.getAvailableCopies() + book.getAvailableCopies());
            if (book.getTitle() != null && !book.getTitle().trim().isEmpty()) {
                existing.setTitle(book.getTitle());
            }
            if (book.getAuthor() != null && !book.getAuthor().trim().isEmpty()) {
                existing.setAuthor(book.getAuthor());
            }
            if (book.getGenre() != null && !book.getGenre().trim().isEmpty()) {
                existing.setGenre(book.getGenre());
            }
        } else {
            catalog.put(book.getIsbn(), book);
        }
    }

    public Book getBookByIsbn(String isbn) {
        return catalog.get(isbn);
    }

    public Map<String, Book> getAllBooks() {
        return new HashMap<>(catalog);
    }

    public void saveCatalog() {
        persistence.saveCatalog(catalog);
    }

    public void loadCatalog() {
        Map<String, Book> loaded = persistence.loadCatalog();
        if (loaded != null) {
            catalog.clear();
            catalog.putAll(loaded);
        }
    }

    public abstract java.util.List<Book> searchByTitle(String title);

    public abstract java.util.List<Book> searchByAuthor(String author);

    public abstract java.util.List<Book> searchByGenre(String genre);

    public abstract boolean borrowBook(String isbn);

    public abstract boolean returnBook(String isbn);

    public abstract boolean updateBookInfo(String isbn, String title, String author, String genre);

    public abstract boolean deleteBook(String isbn);

    public abstract List<Book> getAvailableBooks();

    public abstract boolean borrowBook(String isbn, String borrowerName, String userRole);

    public abstract boolean returnBook(String isbn, String borrowerName, String userRole);

    public abstract List<BorrowRecord> getBorrowHistory();

    public abstract int getActiveBorrowCountForUser(String borrowerName);

    public abstract int getActiveBorrowCountForUserByType(String borrowerName, String itemType);

    public abstract List<Book> getTopBorrowedBooks(int limit);

    public abstract void saveBorrowHistory();

    public abstract void loadBorrowHistory();
}