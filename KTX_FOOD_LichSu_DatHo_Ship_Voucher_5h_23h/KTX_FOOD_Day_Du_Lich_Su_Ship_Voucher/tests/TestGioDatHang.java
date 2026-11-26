import java.nio.file.*;
import java.time.*;
import java.util.*;
import model.*;
import service.*;
import repository.*;

// Kiem tra gio mo cua bang dong ho gia (khong can cho den 23:00).
public class TestGioDatHang {
    private static void check(String localDateTime, boolean expected) throws Exception {
        ZoneId vn=ZoneId.of("Asia/Ho_Chi_Minh");
        Clock clock=Clock.fixed(LocalDateTime.parse(localDateTime).atZone(vn).toInstant(),vn);
        Path orders=Files.createTempFile("ktx-order-test-", ".txt");
        try {
            OrderService service=new OrderService(new FileRepository(orders.toString()),clock);
            if (service.isOpen()!=expected) throw new AssertionError(localDateTime);
            Student student=new Student("SV999","Ban Thu","0912345678","A101",100000);
            Cart cart=new Cart();cart.add(new Food("M01","Com ga",35000,"Com",true),1);
            if (!expected) {
                boolean blocked=false;
                try { service.place(student,cart,new CashPayment(),new OrderOptions("pickup",false,"","","","")); }
                catch(IllegalArgumentException ex){blocked=true;}
                if (!blocked) throw new AssertionError("Ngoai gio van cho dat: "+localDateTime);
            }
            System.out.println(localDateTime+" => "+(service.isOpen()?"Mo dat hang":"Dong dat hang"));
        } finally {Files.deleteIfExists(orders);}
    }
    public static void main(String[] args) throws Exception {
        check("2026-11-26T04:59:00",false);
        check("2026-11-26T05:00:00",true);
        check("2026-11-26T22:59:59",true);
        check("2026-11-26T23:00:00",false);
        System.out.println("DA QUA KIEM TRA GIO MO CUA.");
    }
}
