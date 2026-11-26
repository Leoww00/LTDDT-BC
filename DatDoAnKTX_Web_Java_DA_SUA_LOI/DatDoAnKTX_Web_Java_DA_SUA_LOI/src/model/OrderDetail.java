package model;

public class OrderDetail {
    private final String foodId,foodName;
    private final int quantity;
    private final long unitPrice;
    public OrderDetail(CartItem item){foodId=item.getFood().getId();foodName=item.getFood().getName();quantity=item.getQuantity();unitPrice=item.getFood().getPrice();}
    public String getFoodId(){return foodId;}
    public String getFoodName(){return foodName;}
    public int getQuantity(){return quantity;}
    public long getUnitPrice(){return unitPrice;}
    public long getSubtotal(){return Math.multiplyExact(unitPrice,quantity);}
}
