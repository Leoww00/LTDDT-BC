package utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Ma hoa mat khau bang cong cu co san trong Java (PBKDF2).
public class Passwords {
    private static final int ITERATIONS = 120000;

    public static String createSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
    public static String hash(String password, String salt) {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, Base64.getDecoder().decode(salt), ITERATIONS, 256);
        try {
            byte[] hashed = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Khong the ma hoa mat khau.", e);
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(chars, '\0');
        }
    }
    public static boolean matches(String password, String salt, String savedHash) {
        try {
            byte[] a = Base64.getDecoder().decode(hash(password, salt));
            byte[] b = Base64.getDecoder().decode(savedHash);
            return MessageDigest.isEqual(a, b);
        } catch (Exception e) { return false; }
    }
}
