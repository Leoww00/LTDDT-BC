package repository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import model.*;

// File IO: luu va doc lai don hang khi khoi dong; file TSV de sinh vien de hieu.
public class FileRepository {
    private final Path file;
    public FileRepository(String filename) { file=Paths.get(filename); }
    private String clean(String s) { return (s==null?"":s).replace('\t',' ').replace('\n',' ').replace('\r',' '); }
    public void save(Order order) throws IOException {
        if (file.getParent()!=null) Files.createDirectories(file.getParent());
        try (BufferedWriter out=Files.newBufferedWriter(file,StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,StandardOpenOption.APPEND)) {
            out.write(String.join("\t", "ORDER2", clean(order.getId()),clean(order.getStudentId()),
                order.getCreatedAt().toString(),String.valueOf(order.getSubtotal()),
                String.valueOf(order.getShippingFee()),String.valueOf(order.getDiscount()),
                String.valueOf(order.getTotal()),clean(order.getPaymentMethod()),clean(order.getStatus()),
                clean(order.getVoucherCode()),clean(order.getDeliveryMode()),
                String.valueOf(order.isForOther()),clean(order.getRecipientName()),clean(order.getRecipientPhone()),
                clean(order.getAddress())));
            out.newLine();
            for (OrderDetail d:order.getDetails()) {
                out.write(String.join("\t","ITEM",clean(order.getId()),clean(d.getFoodId()),
                    clean(d.getFoodName()),String.valueOf(d.getQuantity()),String.valueOf(d.getUnitPrice())));
                out.newLine();
            }
        }
    }
    public List<Order> loadOrders() throws IOException {
        List<Order> result=new ArrayList<>();
        if(!Files.exists(file)) return result;
        Map<String,OrderDraft> drafts=new LinkedHashMap<>();
        for (String line:Files.readAllLines(file,StandardCharsets.UTF_8)) {
            String[] f=line.split("\t",-1);
            try {
                if(f.length==16 && f[0].equals("ORDER2")) {
                    OrderDraft d=new OrderDraft();
                    d.id=f[1];d.studentId=f[2];d.date=LocalDateTime.parse(f[3]);
                    d.subtotal=Long.parseLong(f[4]);d.fee=Long.parseLong(f[5]);d.discount=Long.parseLong(f[6]);
                    d.total=Long.parseLong(f[7]);d.payment=f[8];d.status=f[9];d.voucher=f[10];
                    d.mode=f[11];d.other=Boolean.parseBoolean(f[12]);d.name=f[13];d.phone=f[14];d.address=f[15];
                    drafts.put(d.id,d);
                } else if(f.length>=7 && f[0].equals("ORDER")) {
                    OrderDraft d=new OrderDraft();d.id=f[1];d.studentId=f[2];d.date=LocalDateTime.parse(f[3]);
                    d.total=Long.parseLong(f[4]);d.subtotal=d.total;d.payment=f[5];d.status=f[6];
                    d.mode="pickup";d.name="Sinh vien";d.address="Nhan tai can tin";
                    drafts.put(d.id,d);
                } else if(f.length>=6 && f[0].equals("ITEM")) {
                    OrderDraft d=drafts.get(f[1]);
                    if(d!=null) d.details.add(new OrderDetail(f[2],f[3],Integer.parseInt(f[4]),Long.parseLong(f[5])));
                }
            } catch(RuntimeException ex) { throw new IOException("Du lieu don hang loi: "+line,ex); }
        }
        for(OrderDraft d:drafts.values()) {
            OrderQuote quote=new OrderQuote(d.subtotal,d.fee,d.discount,d.voucher);
            if(quote.getTotal()!=d.total) throw new IOException("Tong tien don khong khop: "+d.id);
            result.add(new Order(d.id,d.studentId,d.payment,d.date,d.details,quote,
                    new OrderOptions(d.mode,d.other,d.name,d.phone,d.address,d.voucher),d.status));
        }
        return result;
    }
    private static class OrderDraft {
        String id,studentId,payment,status,voucher="",mode="pickup",name="",phone="",address="";
        boolean other; LocalDateTime date;
        long subtotal,fee,discount,total;
        List<OrderDetail> details=new ArrayList<>();
    }
    public List<String> readAll() throws IOException {
        if(!Files.exists(file)) return Collections.emptyList();
        return Files.readAllLines(file,StandardCharsets.UTF_8);
    }
}
