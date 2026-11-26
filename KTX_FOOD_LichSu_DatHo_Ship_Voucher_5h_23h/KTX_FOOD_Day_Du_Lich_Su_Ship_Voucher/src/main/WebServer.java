package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.*;
import service.AccountService;
import service.OrderService;
import utils.ProjectPaths;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Web server dung Java co san (khong can Spring Boot). */
public class WebServer {
    private final AppContext app;
    private final HttpServer server;
    private final Path webFolder;

    public WebServer(AppContext app, int port) throws IOException {
        this.app = app;
        this.webFolder = ProjectPaths.root().resolve("web");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", this::handle);
    }
    public void start() { server.start(); }

    // Dong bo de 2 thao tac vi/gio hang khong thay doi cung mot luc.
    private synchronized void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();
        try {
            if (path.startsWith("/api/") && method.equals("POST")) {
                // Chi nhan POST tu chinh website dang chay tren localhost.
                String origin = ex.getRequestHeaders().getFirst("Origin");
                if (origin != null && !origin.equals("http://localhost:" + ex.getLocalAddress().getPort())
                        && !origin.equals("http://127.0.0.1:" + ex.getLocalAddress().getPort())) {
                    reply(ex, 403, message(false, "Yeu cau khong hop le."), "application/json"); return;
                }
            }
            if (path.equals("/api/login") && method.equals("POST")) {
                Map<String, String> p = form(ex);
                String token = app.getAccounts().login(p.getOrDefault("studentId", ""), p.getOrDefault("password", ""));
                ex.getResponseHeaders().add("Set-Cookie", "KTXSESSION=" + token + "; Path=/; HttpOnly; SameSite=Strict");
                reply(ex, 200, message(true, "Dang nhap thanh cong."), "application/json"); return;
            }
            if (path.startsWith("/api/")) {
                String token = token(ex);
                AccountService accounts = app.getAccounts();
                Student student = accounts.student(token); // Kiem tra dang nhap cho moi API.
                Cart cart = accounts.cart(student);

                if (path.equals("/api/state") && method.equals("GET")) {
                    reply(ex, 200, state(student, cart), "application/json"); return;
                }
                if (path.equals("/api/orders") && method.equals("GET")) {
                    reply(ex, 200, history(student), "application/json"); return;
                }
                if (path.equals("/api/payments") && method.equals("GET")) {
                    reply(ex, 200, payments(student), "application/json"); return;
                }
                if (path.equals("/api/topups") && method.equals("GET")) {
                    reply(ex, 200, topups(student), "application/json"); return;
                }
                if (path.equals("/api/logout") && method.equals("POST")) {
                    accounts.logout(token);
                    ex.getResponseHeaders().add("Set-Cookie", "KTXSESSION=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0");
                    reply(ex, 200, message(true, "Da dang xuat."), "application/json"); return;
                }
                if (method.equals("POST")) {
                    Map<String, String> p = form(ex);
                    String foodId = p.getOrDefault("foodId", "");
                    if (path.equals("/api/cart/add")) {
                        cart.add(app.getFoods().findById(foodId), number(p, "quantity"));
                        reply(ex, 200, message(true, "Da them mon vao gio."), "application/json"); return;
                    }
                    if (path.equals("/api/cart/update")) {
                        cart.update(foodId, number(p, "quantity"));
                        reply(ex, 200, message(true, "Da cap nhat so luong."), "application/json"); return;
                    }
                    if (path.equals("/api/cart/remove")) {
                        cart.remove(foodId);
                        reply(ex, 200, message(true, "Da xoa mon."), "application/json"); return;
                    }
                    if (path.equals("/api/quote")) {
                        OrderQuote quote = app.getOrders().quote(cart, orderOptions(p));
                        reply(ex, 200, "{\"ok\":true,\"quote\":" + quoteJson(quote) + "}", "application/json"); return;
                    }
                    if (path.equals("/api/order")) {
                        String payment = p.getOrDefault("payment", "");
                        Order order = app.getOrders().place(student, cart, app.payment(payment), orderOptions(p));
                        if (payment.equals("wallet")) accounts.save(); // Luu so du vi sau thanh toan.
                        reply(ex, 200, "{\"ok\":true,\"order\":" + orderJson(order) + "}", "application/json"); return;
                    }
                    if (path.equals("/api/topups/request")) {
                        long amount = money(p.getOrDefault("amount", ""));
                        TopUpRequest request = accounts.requestTopup(student, amount);
                        reply(ex, 200, "{\"ok\":true,\"topup\":" + topupJson(request) + "}", "application/json"); return;
                    }
                    if (path.equals("/api/topups/mock-confirm")) {
                        TopUpRequest request = accounts.confirmMockBank(student, p.getOrDefault("id", ""));
                        reply(ex, 200, "{\"ok\":true,\"message\":\"Ngan hang gia lap da xac nhan. Tien da vao vi.\",\"topup\":" + topupJson(request) + "}", "application/json"); return;
                    }
                }
                reply(ex, 404, message(false, "Khong tim thay chuc nang."), "application/json"); return;
            }
            if (method.equals("GET")) { staticFile(ex, path); return; }
            reply(ex, 404, message(false, "Duong dan khong ton tai."), "application/json");
        } catch (SecurityException e) {
            reply(ex, 401, message(false, e.getMessage()), "application/json");
        } catch (IllegalArgumentException | ArithmeticException e) {
            reply(ex, 400, message(false, e.getMessage()), "application/json");
        } catch (Exception e) {
            e.printStackTrace();
            reply(ex, 500, message(false, "Loi luu du lieu. Hay xem cua so chay Java."), "application/json");
        }
    }
    private OrderOptions orderOptions(Map<String,String> p) {
        return new OrderOptions(p.getOrDefault("delivery", "pickup"),
                "true".equals(p.getOrDefault("forOther", "false")),
                p.getOrDefault("recipientName", ""), p.getOrDefault("recipientPhone", ""),
                p.getOrDefault("address", ""), p.getOrDefault("voucher", ""));
    }
    private String quoteJson(OrderQuote q) {
        return "{\"subtotal\":" + q.getSubtotal() + ",\"shippingFee\":" + q.getShippingFee() +
                ",\"discount\":" + q.getDiscount() + ",\"total\":" + q.getTotal() +
                ",\"voucher\":" + this.q(q.getVoucherCode()) + "}";
    }
    private String payments(Student student) {
        StringBuilder s = new StringBuilder("{\"payments\":[");
        boolean first=true;
        List<Order> orders=app.getOrders().getOrders();
        for (int i=orders.size()-1;i>=0;i--) {
            Order o=orders.get(i);
            if (!o.getStudentId().equals(student.getStudentId())) continue;
            if (!first) s.append(','); first=false;
            s.append("{\"orderId\":").append(q(o.getId()))
             .append(",\"date\":").append(q(o.getCreatedAt().toString()))
             .append(",\"amount\":").append(o.getTotal())
             .append(",\"method\":").append(q(o.getPaymentMethod()))
             .append(",\"status\":").append(q(o.getStatus()))
             .append('}');
        }
        return s.append("]}").toString();
    }
    private String token(HttpExchange ex) {
        String cookies = ex.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) for (String item : cookies.split(";")) {
            String[] p = item.trim().split("=", 2);
            if (p.length == 2 && p[0].equals("KTXSESSION")) return p[1];
        }
        return "";
    }
    private int number(Map<String, String> p, String key) {
        try { return Integer.parseInt(p.getOrDefault(key, "")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("So luong phai la so nguyen."); }
    }
    private long money(String raw) {
        try { return Long.parseLong(raw); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("So tien nap phai la so nguyen."); }
    }
    private Map<String, String> form(HttpExchange ex) throws IOException {
        byte[] bytes = ex.getRequestBody().readNBytes(10001);
        if (bytes.length > 10000) throw new IllegalArgumentException("Du lieu qua dai.");
        String raw = new String(bytes, StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String part : raw.split("&")) {
            if (part.isEmpty()) continue;
            String[] field = part.split("=", 2);
            map.put(URLDecoder.decode(field[0], StandardCharsets.UTF_8),
                URLDecoder.decode(field.length > 1 ? field[1] : "", StandardCharsets.UTF_8));
        }
        return map;
    }
    private String state(Student st, Cart cart) {
        StringBuilder s = new StringBuilder("{\"student\":{");
        s.append("\"id\":").append(q(st.getStudentId()))
         .append(",\"name\":").append(q(st.getFullName()))
         .append(",\"phone\":").append(q(st.getPhone()))
         .append(",\"room\":").append(q(st.getDormAddress()))
         .append(",\"balance\":").append(st.getBalance()).append("},\"foods\":[");
        boolean first = true;
        for (Food f : app.getFoods().getAll()) {
            if (!first) s.append(','); first = false;
            s.append("{\"id\":").append(q(f.getId()))
             .append(",\"name\":").append(q(f.getName()))
             .append(",\"price\":").append(f.getPrice())
             .append(",\"category\":").append(q(f.getCategory()))
             .append(",\"available\":").append(f.isAvailable()).append('}');
        }
        s.append("],\"cart\":["); first = true;
        int count = 0;
        for (CartItem i : cart.getItems()) {
            if (!first) s.append(','); first = false;
            count = Math.addExact(count, i.getQuantity());
            s.append("{\"id\":").append(q(i.getFood().getId()))
             .append(",\"name\":").append(q(i.getFood().getName()))
             .append(",\"quantity\":").append(i.getQuantity())
             .append(",\"price\":").append(i.getFood().getPrice())
             .append(",\"subtotal\":").append(i.getSubtotal()).append('}');
        }
        return s.append("],\"count\":").append(count)
                .append(",\"total\":").append(cart.getTotal())
                .append(",\"orderingOpen\":").append(app.getOrders().isOpen())
                .append(",\"serverTime\":").append(q(app.getOrders().currentTime().toString()))
                .append('}').toString();
    }
    private String history(Student student) {
        StringBuilder s = new StringBuilder("{\"orders\":[");
        boolean first = true;
        List<Order> orders = app.getOrders().getOrders();
        for (int i = orders.size() - 1; i >= 0; i--) {
            Order order = orders.get(i);
            if (!order.getStudentId().equals(student.getStudentId())) continue;
            if (!first) s.append(','); first = false;
            s.append(orderJson(order));
        }
        return s.append("]}").toString();
    }
    private String topups(Student student) {
        StringBuilder s = new StringBuilder("{\"topups\":[");
        boolean first = true;
        for (TopUpRequest request : app.getAccounts().topupsFor(student)) {
            if (!first) s.append(','); first = false;
            s.append(topupJson(request));
        }
        return s.append("]}").toString();
    }
    private String topupJson(TopUpRequest r) {
        return "{\"id\":" + q(r.getId()) + ",\"amount\":" + r.getAmount()
                + ",\"date\":" + q(r.getCreatedAt().toString())
                + ",\"confirmed\":" + r.isConfirmed() + "}";
    }
    private String orderJson(Order o) {
        StringBuilder s = new StringBuilder("{\"id\":").append(q(o.getId()))
         .append(",\"date\":").append(q(o.getCreatedAt().toString()))
         .append(",\"subtotal\":").append(o.getSubtotal())
         .append(",\"shippingFee\":").append(o.getShippingFee())
         .append(",\"discount\":").append(o.getDiscount())
         .append(",\"total\":").append(o.getTotal())
         .append(",\"voucher\":").append(q(o.getVoucherCode()))
         .append(",\"delivery\":").append(q(o.getDeliveryMode()))
         .append(",\"forOther\":").append(o.isForOther())
         .append(",\"recipientName\":").append(q(o.getRecipientName()))
         .append(",\"recipientPhone\":").append(q(o.getRecipientPhone()))
         .append(",\"address\":").append(q(o.getAddress()))
         .append(",\"payment\":").append(q(o.getPaymentMethod()))
         .append(",\"status\":").append(q(o.getStatus()))
         .append(",\"details\":[");
        boolean first = true;
        for (OrderDetail d : o.getDetails()) {
            if (!first) s.append(','); first = false;
            s.append("{\"name\":").append(q(d.getFoodName()))
             .append(",\"quantity\":").append(d.getQuantity())
             .append(",\"subtotal\":").append(d.getSubtotal()).append('}');
        }
        return s.append("]}").toString();
    }
    private String message(boolean ok, String msg) { return "{\"ok\":" + ok + ",\"message\":" + q(msg) + "}"; }
    private String q(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            if (c == '"') out.append("\\\"");
            else if (c == '\\') out.append("\\\\");
            else if (c == '\n') out.append("\\n");
            else if (c == '\r') out.append("\\r");
            else if (c == '\t') out.append("\\t");
            else if (c < 32) out.append(' ');
            else out.append(c);
        }
        return out.append('"').toString();
    }
    private void reply(HttpExchange ex, int code, String body, String type) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
    private void staticFile(HttpExchange ex, String path) throws IOException {
        String filename = path.equals("/") ? "index.html" : path.substring(1);
        if (!filename.equals("index.html") && !filename.equals("style.css") && !filename.equals("app.js") && !filename.equals("qr.js") && !filename.equals("payment-config.js")) {
            reply(ex, 404, "Not found", "text/plain"); return;
        }
        Path file = webFolder.resolve(filename);
        if (!Files.exists(file)) { reply(ex, 404, "Khong tim thay giao dien: " + file, "text/plain"); return; }
        String type = filename.endsWith(".html") ? "text/html" : filename.endsWith(".css") ? "text/css" : "text/javascript";
        reply(ex, 200, Files.readString(file, StandardCharsets.UTF_8), type);
    }
}
