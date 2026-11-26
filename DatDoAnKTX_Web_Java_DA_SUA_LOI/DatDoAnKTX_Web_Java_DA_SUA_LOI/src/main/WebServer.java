package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.*;
import utils.ProjectPaths;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

// Web server dung Java thu vien co san. Khong can Spring Boot.
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

    // Dong bo de giao dien web va gio hang khong xu ly cung luc.
    private synchronized void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        try {
            if (path.equals("/api/state") && ex.getRequestMethod().equals("GET")) {
                reply(ex, 200, state(), "application/json"); return;
            }
            if (path.equals("/api/orders") && ex.getRequestMethod().equals("GET")) {
                reply(ex, 200, history(), "application/json"); return;
            }
            if (path.startsWith("/api/") && ex.getRequestMethod().equals("POST")) {
                Map<String, String> p = form(ex);
                String foodId = p.getOrDefault("foodId", "");
                if (path.equals("/api/cart/add")) {
                    app.getCart().add(app.getFoods().findById(foodId), number(p, "quantity"));
                    reply(ex, 200, message(true, "Da them mon vao gio."), "application/json"); return;
                }
                if (path.equals("/api/cart/update")) {
                    app.getCart().update(foodId, number(p, "quantity"));
                    reply(ex, 200, message(true, "Da cap nhat so luong."), "application/json"); return;
                }
                if (path.equals("/api/cart/remove")) {
                    app.getCart().remove(foodId);
                    reply(ex, 200, message(true, "Da xoa mon khoi gio."), "application/json"); return;
                }
                if (path.equals("/api/order")) {
                    String method = p.getOrDefault("payment", "");
                    int type = method.equals("wallet") ? 1 : method.equals("cash") ? 2 : method.equals("bank") ? 3 : 0;
                    Order o = app.getOrders().place(app.getStudent(), app.getCart(), app.payment(type));
                    reply(ex, 200, "{\"ok\":true,\"message\":\"Dat hang thanh cong!\",\"order\":" + orderJson(o) + "}", "application/json"); return;
                }
                reply(ex, 404, message(false, "Khong tim thay chuc nang."), "application/json"); return;
            }
            if (ex.getRequestMethod().equals("GET") && !path.startsWith("/api/")) {
                staticFile(ex, path); return;
            }
            reply(ex, 404, message(false, "Duong dan khong ton tai."), "application/json");
        } catch (IllegalArgumentException | ArithmeticException e) {
            reply(ex, 400, message(false, e.getMessage()), "application/json");
        } catch (Exception e) {
            e.printStackTrace();
            reply(ex, 500, message(false, "Co loi khi luu/lay du lieu. Kiem tra Terminal."), "application/json");
        }
    }
    private int number(Map<String, String> p, String key) {
        try { return Integer.parseInt(p.getOrDefault(key, "")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("So luong phai la so nguyen."); }
    }
    private Map<String, String> form(HttpExchange ex) throws IOException {
        String raw = new String(ex.getRequestBody().readNBytes(10001), StandardCharsets.UTF_8);
        if (raw.length() > 10000) throw new IllegalArgumentException("Du lieu qua dai.");
        Map<String, String> p = new HashMap<>();
        for (String part : raw.split("&")) {
            if (part.isBlank()) continue;
            String[] field = part.split("=", 2);
            p.put(URLDecoder.decode(field[0], StandardCharsets.UTF_8), URLDecoder.decode(field.length > 1 ? field[1] : "", StandardCharsets.UTF_8));
        }
        return p;
    }
    private String state() {
        StringBuilder s = new StringBuilder("{\"student\":{");
        Student st = app.getStudent();
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
        for (CartItem i : app.getCart().getItems()) {
            if (!first) s.append(','); first = false;
            count += i.getQuantity();
            s.append("{\"id\":").append(q(i.getFood().getId()))
             .append(",\"name\":").append(q(i.getFood().getName()))
             .append(",\"quantity\":").append(i.getQuantity())
             .append(",\"price\":").append(i.getFood().getPrice())
             .append(",\"subtotal\":").append(i.getSubtotal()).append('}');
        }
        return s.append("],\"count\":").append(count)
                .append(",\"total\":").append(app.getCart().getTotal()).append('}').toString();
    }
    private String history() {
        StringBuilder s = new StringBuilder("{\"orders\":[");
        boolean first = true;
        List<Order> orders = app.getOrders().getOrders();
        for (int i = orders.size() - 1; i >= 0; i--) {
            if (!first) s.append(','); first = false;
            s.append(orderJson(orders.get(i)));
        }
        return s.append("]}").toString();
    }
    private String orderJson(Order o) {
        StringBuilder s = new StringBuilder("{\"id\":").append(q(o.getId()))
         .append(",\"date\":").append(q(o.getCreatedAt().toString()))
         .append(",\"total\":").append(o.getTotal())
         .append(",\"payment\":").append(q(o.getPaymentMethod()))
         .append(",\"status\":").append(q(o.getStatus()))
         .append(",\"details\":[");
        boolean first = true;
        for (OrderDetail d : o.getDetails()) {
            if (!first) s.append(','); first = false;
            s.append("{\"name\":").append(q(d.getFoodName()))
             .append(",\"quantity\":").append(d.getQuantity())
             .append(",\"subtotal\":").append(d.getQuantity() * d.getUnitPrice()).append('}');
        }
        return s.append("]}").toString();
    }
    private String message(boolean ok, String msg) { return "{\"ok\":" + ok + ",\"message\":" + q(msg) + "}"; }
    private String q(String v) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : v.toCharArray()) {
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
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
    private void staticFile(HttpExchange ex, String path) throws IOException {
        String filename = path.equals("/") ? "index.html" : path.substring(1);
        if (!filename.equals("index.html") && !filename.equals("style.css") && !filename.equals("app.js")) {
            reply(ex, 404, "Not found", "text/plain"); return;
        }
        Path file = webFolder.resolve(filename);
        if (!Files.exists(file)) { reply(ex, 404, "Khong tim thay file giao dien: " + file, "text/plain"); return; }
        String type = filename.endsWith(".html") ? "text/html" : filename.endsWith(".css") ? "text/css" : "text/javascript";
        reply(ex, 200, Files.readString(file, StandardCharsets.UTF_8), type);
    }
}
