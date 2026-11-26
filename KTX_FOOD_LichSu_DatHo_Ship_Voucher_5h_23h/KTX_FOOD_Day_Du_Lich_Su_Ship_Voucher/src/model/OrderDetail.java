package model;

public class OrderDetail {
    private final String foodId, foodName;
    private final int quantity;
    private final long unitPrice;
    public OrderDetail(CartItem item) {
        this(item.getFood().getId(), item.getFood().getName(), item.getQuantity(), item.getFood().getPrice());
    }
    public OrderDetail(String foodId, String foodName, int quantity, long unitPrice) {
        this.foodId=foodId; this.foodName=foodName; this.quantity=quantity; this.unitPrice=unitPrice;
    }
    public String getFoodId(){return foodId;}
    public String getFoodName(){return foodName;}
    public int getQuantity(){return quantity;}
    public long getUnitPrice(){return unitPrice;}
    public long getSubtotal(){return Math.multiplyExact(unitPrice,quantity);}
}
