package model;

import java.util.*;
import utils.Validation;
public class Cart {
    private final List<CartItem> items=new ArrayList<>();
    public List<CartItem> getItems(){
        return Collections.unmodifiableList(items);
    }
    public boolean isEmpty(){
        return items.isEmpty();
    }
    public void add(Food food,int qty){
        Validation.positive(qty);
        if(food==null||!food.isAvailable())
            throw new IllegalArgumentException("Món đã hết hàng hoặc không tồn tại");
        for(CartItem i:items)
            if(i.getFood().getId().equals(food.getId())){
                i.setQuantity(Math.addExact(i.getQuantity(),qty));
                return;
            }
        items.add(new CartItem(food,qty));
    }
    public void update(String foodId,int qty){
        Validation.positive(qty);
        for(CartItem i:items)
            if(i.getFood().getId().equalsIgnoreCase(foodId)){
                i.setQuantity(qty);return;
            }
        throw new IllegalArgumentException("Món không có trong giỏ");}
    public void remove(String foodId){
        if(!items.removeIf(i->i.getFood().getId().equalsIgnoreCase(foodId)))
            throw new IllegalArgumentException("Món không có trong giỏ");}
    public long getTotal(){
        long sum=0;
        for(CartItem i:items)
            sum=Math.addExact(sum,i.getSubtotal());
        return sum;
    }
    public void clear(){
        items.clear();
    }
}
