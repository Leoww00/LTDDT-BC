package service;

import model.Student;
public class CashPayment implements PaymentMethod {
    public String getName(){return "Tien mat";}
    public void pay(Student student,long amount){} // Thanh toan khi nhan hang
    public String successStatus(){return "Cho thanh toan tien mat";}
}
