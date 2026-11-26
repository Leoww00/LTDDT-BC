package model;

import java.time.LocalDateTime;
import java.util.*;
public class Order {
    private final String id,studentId,paymentMethod;
    private final LocalDateTime createdAt;
    private final List<OrderDetail> details;
    private final long total;
    private String status;
    public Order(String id,String studentId,String method,Cart cart){
        this.id=id;this.studentId=studentId;this.paymentMethod=method;this.createdAt=LocalDateTime.now();
        List<OrderDetail> copy=new ArrayList<>();for(CartItem item:cart.getItems())copy.add(new OrderDetail(item));
        this.details=Collections.unmodifiableList(copy);this.total=cart.getTotal();this.status="Cho thanh toan";
    }
    public String getId(){return id;}
    public String getStudentId(){return studentId;}
    public String getPaymentMethod(){return paymentMethod;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public List<OrderDetail> getDetails(){return details;}
    public long getTotal(){return total;}
    public String getStatus(){return status;}
    public void setStatus(String s){status=s;}
    @Override public String toString(){return id+" | "+createdAt+" | "+total+" VND | "+paymentMethod+" | "+status;}
}
