package model;

import java.time.LocalDateTime;
import java.util.*;

public class Order {
    private final String id, studentId, paymentMethod;
    private final LocalDateTime createdAt;
    private final List<OrderDetail> details;
    private final long subtotal, shippingFee, discount, total;
    private final String voucherCode, deliveryMode, recipientName, recipientPhone, address;
    private final boolean forOther;
    private String status;

    public Order(String id, String studentId, String paymentMethod, LocalDateTime createdAt,
                 List<OrderDetail> details, OrderQuote quote, OrderOptions options, String status) {
        this.id=id; this.studentId=studentId; this.paymentMethod=paymentMethod;
        this.createdAt=createdAt;
        this.details=Collections.unmodifiableList(new ArrayList<>(details));
        this.subtotal=quote.getSubtotal(); this.shippingFee=quote.getShippingFee();
        this.discount=quote.getDiscount(); this.total=quote.getTotal();
        this.voucherCode=quote.getVoucherCode();
        this.deliveryMode=options.getDeliveryMode(); this.forOther=options.isForOther();
        this.recipientName=options.getRecipientName(); this.recipientPhone=options.getRecipientPhone();
        this.address=options.getAddress(); this.status=status;
    }
    public String getId(){return id;}
    public String getStudentId(){return studentId;}
    public String getPaymentMethod(){return paymentMethod;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public List<OrderDetail> getDetails(){return details;}
    public long getSubtotal(){return subtotal;}
    public long getShippingFee(){return shippingFee;}
    public long getDiscount(){return discount;}
    public long getTotal(){return total;}
    public String getVoucherCode(){return voucherCode;}
    public String getDeliveryMode(){return deliveryMode;}
    public boolean isForOther(){return forOther;}
    public String getRecipientName(){return recipientName;}
    public String getRecipientPhone(){return recipientPhone;}
    public String getAddress(){return address;}
    public String getStatus(){return status;}
    public void setStatus(String status){this.status=status;}
    @Override public String toString(){return id+" | "+createdAt+" | "+total+" VND | "+status;}
}
