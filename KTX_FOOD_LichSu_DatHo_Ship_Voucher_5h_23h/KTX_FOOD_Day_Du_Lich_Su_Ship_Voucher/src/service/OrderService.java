package service;

import java.io.IOException;
import java.time.*;
import java.util.*;
import model.*;
import repository.FileRepository;
import utils.IdGenerator;

// Tat ca tinh tien, voucher, ship va gio gioi han deu kiem tra tai Java.
public class OrderService {
    private final FileRepository repo;
    private final List<Order> orders;
    private final Clock clock;
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final LocalTime OPEN = LocalTime.of(5,0);
    private static final LocalTime CLOSE = LocalTime.of(23,0);

    public OrderService(FileRepository repo) throws IOException { this(repo, Clock.system(VIETNAM)); }
    public OrderService(FileRepository repo, Clock clock) throws IOException {
        this.repo = repo;
        this.clock = clock;
        this.orders = new ArrayList<>(repo.loadOrders());
    }
    public List<Order> getOrders(){return Collections.unmodifiableList(orders);}
    public LocalTime currentTime(){return LocalTime.now(clock.withZone(VIETNAM));}
    public boolean isOpen(){ LocalTime time=currentTime(); return !time.isBefore(OPEN) && time.isBefore(CLOSE); }

    public OrderQuote quote(Cart cart, OrderOptions options) {
        if (cart.isEmpty()) throw new IllegalArgumentException("Gio hang rong. Hay chon mon truoc.");
        if (!"pickup".equals(options.getDeliveryMode()) && !"ship".equals(options.getDeliveryMode()))
            throw new IllegalArgumentException("Hay chon nhan tai can tin hoac giao hang.");
        long subtotal = cart.getTotal();
        // Phi giao hang DEMO: 8.000 VND cho don ship noi bo KTX.
        long shipping = "ship".equals(options.getDeliveryMode()) ? 8000 : 0;
        String voucher = options.getVoucherCode();
        long discount = 0;
        switch(voucher) {
            case "": break;
            case "KTX10":
                if (subtotal < 50000) throw new IllegalArgumentException("KTX10 can don hang tu 50.000 VND.");
                discount = Math.min(subtotal / 10, 15000); break;
            case "FREESHIP":
                if (subtotal < 60000) throw new IllegalArgumentException("FREESHIP can don hang tu 60.000 VND.");
                if (shipping == 0) throw new IllegalArgumentException("FREESHIP chi ap dung khi chon giao hang.");
                discount = shipping; break;
            default: throw new IllegalArgumentException("Ma voucher khong ton tai hoac het hieu luc.");
        }
        return new OrderQuote(subtotal, shipping, discount, voucher);
    }
    public Order place(Student student, Cart cart, PaymentMethod payment, OrderOptions rawOptions) throws IOException {
        if (!isOpen()) throw new IllegalArgumentException("Can tin chi nhan don 05:00 - 23:00 (gio Viet Nam). Hen ban ngay mai!");
        if (cart.isEmpty()) throw new IllegalArgumentException("Gio hang rong.");
        for (CartItem item:cart.getItems())
            if (!item.getFood().isAvailable()) throw new IllegalArgumentException("Mon "+item.getFood().getName()+" da het hang.");
        String name, phone, address;
        boolean other = rawOptions.isForOther();
        if (other) {
            name=rawOptions.getRecipientName();phone=rawOptions.getRecipientPhone();address=rawOptions.getAddress();
            if (name.length()<2 || name.length()>80) throw new IllegalArgumentException("Nhap ten nguoi nhan (2-80 ky tu).");
            if (!phone.matches("0[0-9]{9}")) throw new IllegalArgumentException("So dien thoai nguoi nhan phai co 10 chu so, bat dau bang 0.");
        } else { name=student.getFullName();phone=student.getPhone();address=student.getDormAddress(); }
        if ("ship".equals(rawOptions.getDeliveryMode()) && (address.isBlank() || address.length()>120))
            throw new IllegalArgumentException("Hay nhap dia chi giao trong KTX (toi da 120 ky tu).");
        if ("pickup".equals(rawOptions.getDeliveryMode())) address="Nhan tai can tin";
        OrderOptions options = new OrderOptions(rawOptions.getDeliveryMode(),other,name,phone,address,rawOptions.getVoucherCode());
        OrderQuote quote = quote(cart,options);
        if(payment instanceof WalletPayment && student.getBalance()<quote.getTotal())
            throw new IllegalArgumentException("So du vi khong du, vui long nap them.");
        List<OrderDetail> details = new ArrayList<>();
        for (CartItem item : cart.getItems()) details.add(new OrderDetail(item));
        Order order = new Order(IdGenerator.nextOrderId(), student.getStudentId(),payment.getName(),
            LocalDateTime.now(clock.withZone(VIETNAM)), details, quote, options, payment.successStatus());
        // Ghi file truoc khi tru vi. Thanh toan tien mat/chuyen khoan cho xac nhan.
        repo.save(order);
        payment.pay(student,quote.getTotal());
        orders.add(order);
        cart.clear();
        return order;
    }
}
