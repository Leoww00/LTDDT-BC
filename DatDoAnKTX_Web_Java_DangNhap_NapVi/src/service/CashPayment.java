package service;

import model.Student;
public class CashPayment implements PaymentMethod {
    public String getName(){return "Tiền mặt";}
    public void pay(Student student,long amount){} // Thanh toan khi nhan hang
    public String successStatus(){return "Chờ thanh toán tiền mặt";}
}
