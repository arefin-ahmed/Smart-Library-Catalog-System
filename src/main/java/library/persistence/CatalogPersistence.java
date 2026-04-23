package library.persistence;

import java.util.Map;

import library.models.Book;

public interface CatalogPersistence {
    void saveCatalog(Map<String, Book> catalog);

    Map<String, Book> loadCatalog();
}