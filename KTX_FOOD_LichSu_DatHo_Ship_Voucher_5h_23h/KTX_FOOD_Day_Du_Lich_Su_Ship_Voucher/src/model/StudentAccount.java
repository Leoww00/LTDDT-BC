package model;

// Tai khoan dang nhap va thong tin sinh vien.
// Mat khau KHONG duoc luu duoi dang van ban thuong.
public class StudentAccount {
    private final Student student;
    private final String salt;
    private final String passwordHash;

    public StudentAccount(Student student, String salt, String passwordHash) {
        this.student = student;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }
    public Student getStudent() { return student; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
}
