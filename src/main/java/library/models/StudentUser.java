package library.models;

public class StudentUser extends User {
    public StudentUser(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public boolean canAddBook() {
        return false;
    }

    @Override
    public boolean canUpdateBookInfo() {
        return false;
    }

    @Override
    public boolean canDeleteBook() {
        return false;
    }

    @Override
    public boolean canBorrowBook() {
        return true;
    }

    @Override
    public boolean canViewAllBorrowHistory() {
        return false;
    }
}