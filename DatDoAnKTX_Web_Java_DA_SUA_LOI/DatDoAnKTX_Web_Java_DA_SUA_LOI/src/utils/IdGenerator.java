package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
public final class IdGenerator {
    private static final AtomicInteger COUNT = new AtomicInteger();
    private IdGenerator() {}
    public static String nextOrderId() {
        return "DH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "-" + COUNT.incrementAndGet();
    }
}
