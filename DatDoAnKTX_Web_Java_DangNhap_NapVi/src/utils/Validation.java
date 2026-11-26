package utils;

public final class Validation {
    private Validation() {}
    public static String required(String s, String label) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(label + " không được rỗng");
        return s.trim();
    }
    public static void positive(int n) { if (n < 1) throw new IllegalArgumentException("Số lượng phải >= 1"); }
    public static void nonNegative(long n) { if (n < 0) throw new IllegalArgumentException("Giá tiền không được âm"); }
}
