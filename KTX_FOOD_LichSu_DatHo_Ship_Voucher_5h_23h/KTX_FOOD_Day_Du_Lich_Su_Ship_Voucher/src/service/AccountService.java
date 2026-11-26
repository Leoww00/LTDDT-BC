package service;

import model.*;
import repository.AccountRepository;
import utils.Passwords;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Quan ly dang nhap, moi sinh vien 1 gio hang, 1 so du va lich su nap vi.
public class AccountService {
    private final AccountRepository repository;
    private final Map<String, StudentAccount> accounts = new LinkedHashMap<>();
    private final Map<String, TopUpRequest> topups = new LinkedHashMap<>();
    private final Map<String, String> sessions = new HashMap<>();
    private final Map<String, Cart> carts = new HashMap<>();
    private final Random random = new java.security.SecureRandom();

    public AccountService(AccountRepository repository) throws IOException {
        this.repository = repository;
        repository.load(accounts, topups);
        if (accounts.isEmpty()) {
            addDemoAccount("SV001", "Nguyen Van A", "0912345678", "A101", 200000);
            addDemoAccount("SV002", "Tran Thi B", "0987654321", "B205", 80000);
            save();
        }
    }
    private void addDemoAccount(String id, String name, String phone, String room, long balance) {
        String salt = Passwords.createSalt();
        accounts.put(id, new StudentAccount(new Student(id, name, phone, room, balance), salt, Passwords.hash("123456", salt)));
    }
    public String login(String studentId, String password) {
        StudentAccount account = accounts.get(studentId == null ? "" : studentId.trim().toUpperCase(Locale.ROOT));
        if (account == null || !Passwords.matches(password, account.getSalt(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Ma sinh vien hoac mat khau khong dung.");
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, account.getStudent().getStudentId());
        return token;
    }
    public Student student(String token) {
        String id = sessions.get(token);
        if (id == null) throw new SecurityException("Ban can dang nhap de su dung website.");
        return accounts.get(id).getStudent();
    }
    public Cart cart(Student student) { return carts.computeIfAbsent(student.getStudentId(), id -> new Cart()); }
    public void logout(String token) { sessions.remove(token); }
    public void save() throws IOException { repository.save(accounts.values(), topups.values()); }

    public TopUpRequest requestTopup(Student student, long amount) throws IOException {
        if (amount < 10000 || amount > 2000000 || amount % 1000 != 0) {
            throw new IllegalArgumentException("Nap tu 10.000 den 2.000.000 VND, boi so 1.000.");
        }
        String id = "NAP" + LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + (1000 + random.nextInt(9000));
        TopUpRequest request = new TopUpRequest(id, student.getStudentId(), amount, LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")), false);
        topups.put(id, request);
        try { save(); }
        catch (IOException e) { topups.remove(id); throw e; }
        return request;
    }
    // Trong do an, nut nay dong vai tro ngan hang GIA LAP xac nhan giao dich.
    // Khong dung endpoint nay de nap tien that!
    public TopUpRequest confirmMockBank(Student student, String id) throws IOException {
        TopUpRequest request = topups.get(id);
        if (request == null || !request.getStudentId().equals(student.getStudentId())) {
            throw new IllegalArgumentException("Khong tim thay yeu cau nap vi cua ban.");
        }
        if (request.isConfirmed()) throw new IllegalArgumentException("Giao dich nay da duoc xac nhan.");
        long oldBalance = student.getBalance();
        long newBalance = Math.addExact(oldBalance, request.getAmount());
        request.setConfirmed(true);
        student.setBalance(newBalance);
        try { save(); }
        catch (IOException | RuntimeException e) {
            request.setConfirmed(false);
            student.setBalance(oldBalance);
            throw e;
        }
        return request;
    }
    public List<TopUpRequest> topupsFor(Student student) {
        List<TopUpRequest> result = new ArrayList<>();
        for (TopUpRequest r : topups.values()) if (r.getStudentId().equals(student.getStudentId())) result.add(r);
        Collections.reverse(result);
        return result;
    }
}
