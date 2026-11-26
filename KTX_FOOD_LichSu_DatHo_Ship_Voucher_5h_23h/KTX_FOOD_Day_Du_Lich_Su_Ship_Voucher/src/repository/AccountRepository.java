package repository;

import model.Student;
import model.StudentAccount;
import model.TopUpRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

// File IO: luu tai khoan, so du vi va cac yeu cau nap tien vao 1 file.
// Ghi file tam roi thay the file cu, tranh file chi ghi duoc mot nua.
public class AccountRepository {
    private final Path file;
    public AccountRepository(Path file) { this.file = file; }

    public void load(Map<String, StudentAccount> accounts, Map<String, TopUpRequest> requests) throws IOException {
        if (!Files.exists(file)) return;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] fields = line.split("\t", -1);
            try {
                if (fields[0].equals("STUDENT") && fields.length == 8) {
                    Student student = new Student(fields[1], fields[2], fields[3], fields[4], Long.parseLong(fields[5]));
                    accounts.put(student.getStudentId(), new StudentAccount(student, fields[6], fields[7]));
                } else if (fields[0].equals("TOPUP") && fields.length == 6) {
                    TopUpRequest request = new TopUpRequest(fields[1], fields[2], Long.parseLong(fields[3]), LocalDateTime.parse(fields[4]), Boolean.parseBoolean(fields[5]));
                    requests.put(request.getId(), request);
                }
            } catch (RuntimeException e) {
                throw new IOException("Du lieu vi khong hop le: " + line, e);
            }
        }
    }
    public void save(Collection<StudentAccount> accounts, Collection<TopUpRequest> requests) throws IOException {
        Files.createDirectories(file.getParent());
        List<String> lines = new ArrayList<>();
        lines.add("# Du lieu demo. Khong dung voi tien that.");
        for (StudentAccount account : accounts) {
            Student s = account.getStudent();
            lines.add(String.join("\t", "STUDENT", clean(s.getStudentId()), clean(s.getFullName()),
                    clean(s.getPhone()), clean(s.getDormAddress()), String.valueOf(s.getBalance()),
                    account.getSalt(), account.getPasswordHash()));
        }
        for (TopUpRequest r : requests) {
            lines.add(String.join("\t", "TOPUP", r.getId(), r.getStudentId(),
                    String.valueOf(r.getAmount()), r.getCreatedAt().toString(), String.valueOf(r.isConfirmed())));
        }
        Path temp = Files.createTempFile(file.getParent(), "wallet-", ".tmp");
        try {
            Files.write(temp, lines, StandardCharsets.UTF_8);
            try { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
    private String clean(String s) { return s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' '); }
}
