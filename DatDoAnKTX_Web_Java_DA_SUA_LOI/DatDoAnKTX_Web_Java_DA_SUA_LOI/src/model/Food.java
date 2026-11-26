package model;

import utils.Validation;
public class Food {
    private String id,name,category;
    private long price;
    private boolean available;
    public Food(String id,String name,long price,String category,boolean available){setId(id);setName(name);setPrice(price);setCategory(category);setAvailable(available);}
    public String getId(){return id;}
    public void setId(String s){id=Validation.required(s,"Ma mon");}
    public String getName(){return name;}
    public void setName(String s){name=Validation.required(s,"Ten mon");}
    public long getPrice(){return price;}
    public void setPrice(long n){Validation.nonNegative(n);price=n;}
    public String getCategory(){return category;}
    public void setCategory(String s){category=Validation.required(s,"Loai mon");}
    public boolean isAvailable(){return available;}
    public void setAvailable(boolean b){available=b;}
    @Override public String toString(){return id+" | "+name+" | "+price+" VND | "+category+" | "+(available?"Con hang":"Het hang");}
}
