package library.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import library.models.BorrowRecord;
import library.util.textfile;

/**
 * Saves and loads borrow history in borrow_history.txt.
 */
public class BorrowHistoryPersistenceTXT {
    private final String filePath;

    public BorrowHistoryPersistenceTXT() {
        this("borrow_history.txt");
    }

    public BorrowHistoryPersistenceTXT(String filePath) {
        this.filePath = filePath;
    }

    public void saveHistory(List<BorrowRecord> history) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath)))) {
            writer.write("isbn,bookTitle,borrowerName,userRole,issueDate,dueDate,action");
            writer.newLine();
            for (BorrowRecord record : history) {
                writer.write(String.join(",",
                        textfile.escape(record.getIsbn()),
                        textfile.escape(record.getBookTitle()),
                        textfile.escape(record.getBorrowerName()),
                        textfile.escape(record.getUserRole()),
                        textfile.escape(record.getIssueDate()),
                        textfile.escape(record.getDueDate()),
                        textfile.escape(record.getAction())));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save borrow history: " + e.getMessage());
        }
    }

    public List<BorrowRecord> loadHistory() {
        List<BorrowRecord> history = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return history;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null) {
                return history;
            }

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = parseCsvLine(line);
                if (parts.length < 6) {
                    continue;
                }

                String dueDate = parts.length >= 7 ? parts[5] : "";
                String action = parts.length >= 7 ? parts[6] : parts[5];
                history.add(new BorrowRecord(parts[0], parts[1], parts[2], parts[3], parts[4], dueDate, action));
            }
        } catch (IOException e) {
            System.out.println("Could not load borrow history: " + e.getMessage());
        }

        return history;
    }

    private String[] parseCsvLine(String line) {
        return textfile.parseCsvLine(line);
    }
}