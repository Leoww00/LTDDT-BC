package model;

import utils.Validation;
public class Student extends User {
    private long balance;
    public Student(String id,String name,String phone,String room,long balance){super(id,name,phone,room);setBalance(balance);}
    public long getBalance(){return balance;}
    public void setBalance(long n){Validation.nonNegative(n);balance=n;}
    public void deduct(long amount){Validation.nonNegative(amount);if(balance<amount)throw new IllegalArgumentException("So du vi khong du");balance-=amount;}
    public void addBalance(long amount){
        if(amount <= 0) throw new IllegalArgumentException("So tien nap phai lon hon 0");
        balance = Math.addExact(balance,amount);
    }
    @Override public String displayInfo(){return getStudentId()+" - "+getFullName()+" - "+getDormAddress()+" - Vi: "+balance+" VND";}
}
