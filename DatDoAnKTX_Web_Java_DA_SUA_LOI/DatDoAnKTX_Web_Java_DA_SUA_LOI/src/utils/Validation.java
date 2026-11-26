package utils;

public final class Validation {
    private Validation() {}
    public static String required(String s, String label) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(label + " khong duoc rong");
        return s.trim();
    }
    public static void positive(int n) { if (n < 1) throw new IllegalArgumentException("So luong phai >= 1"); }
    public static void nonNegative(long n) { if (n < 0) throw new IllegalArgumentException("Gia tien khong duoc am"); }
}
