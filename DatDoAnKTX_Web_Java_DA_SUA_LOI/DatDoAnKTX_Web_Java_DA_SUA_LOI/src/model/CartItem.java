package model;

import utils.Validation;
public class CartItem {
    private final Food food;
    private int quantity;
    public CartItem(Food food,int quantity){if(food==null)throw new IllegalArgumentException("Mon khong hop le");this.food=food;setQuantity(quantity);}
    public Food getFood(){return food;}
    public int getQuantity(){return quantity;}
    public void setQuantity(int n){Validation.positive(n);quantity=n;}
    public long getSubtotal(){return Math.multiplyExact(food.getPrice(),quantity);}
}
