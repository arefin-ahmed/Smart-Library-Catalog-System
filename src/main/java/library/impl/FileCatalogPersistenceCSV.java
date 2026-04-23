package library.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import library.models.Book;
import library.persistence.CatalogPersistence;
import library.util.CsvUtils;

/**
 * Saves and loads catalog in a human-readable text file (catalog.txt).
 */
public class FileCatalogPersistenceCSV implements CatalogPersistence {
    private final String filePath;

    public FileCatalogPersistenceCSV() {
        this("catalog.txt");
    }

    public FileCatalogPersistenceCSV(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void saveCatalog(Map<String, Book> catalog) {
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(
                    "isbn,title,author,genre,publisher,totalCopies,availableCopies,shelfLocation,borrowCount,lastIssueDate");
            writer.newLine();

            for (Book book : catalog.values()) {
                writer.write(toCsvLine(book));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save catalog: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Book> loadCatalog() {
        Map<String, Book> loaded = new HashMap<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return loaded;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // header
            if (line == null) {
                return loaded;
            }

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseCsvLine(line);
                if (parts.length < 9) {
                    continue;
                }

                Book book = new Book(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        parseIntSafe(parts[5]),
                        parseIntSafe(parts[6]),
                        parts[7],
                        parseIntSafe(parts[8]),
                        parts.length > 9 ? parts[9] : "");

                loaded.put(book.getIsbn(), book);
            }
        } catch (IOException e) {
            System.out.println("Could not load catalog: " + e.getMessage());
        }

        return loaded;
    }

    private String toCsvLine(Book book) {
        return String.join(",",
                CsvUtils.escape(book.getIsbn()),
                CsvUtils.escape(book.getTitle()),
                CsvUtils.escape(book.getAuthor()),
                CsvUtils.escape(book.getGenre()),
                CsvUtils.escape(book.getPublisher()),
                String.valueOf(book.getTotalCopies()),
                String.valueOf(book.getAvailableCopies()),
                // CsvUtils.escape(book.getShelfLocation()),
                String.valueOf(book.getBorrowCount()),
                CsvUtils.escape(book.getLastIssueDate()));
    }

    private String[] parseCsvLine(String line) {
        return CsvUtils.parseCsvLine(line);
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}