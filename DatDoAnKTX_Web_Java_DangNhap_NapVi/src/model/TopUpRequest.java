package model;

import java.time.LocalDateTime;

// Yeu cau nap tien vao vi. Day la giao dich NGAN HANG GIA LAP.
public class TopUpRequest {
    private final String id;
    private final String studentId;
    private final long amount;
    private final LocalDateTime createdAt;
    private boolean confirmed;

    public TopUpRequest(String id, String studentId, long amount, LocalDateTime createdAt, boolean confirmed) {
        if (amount < 10000 || amount > 2000000) {
            throw new IllegalArgumentException("So tien nap phai tu 10.000 den 2.000.000 VND.");
        }
        this.id = id;
        this.studentId = studentId;
        this.amount = amount;
        this.createdAt = createdAt;
        this.confirmed = confirmed;
    }
    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public long getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
}
