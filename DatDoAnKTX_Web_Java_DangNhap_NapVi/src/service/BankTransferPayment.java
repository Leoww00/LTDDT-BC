package service;

import model.Student;
public class BankTransferPayment implements PaymentMethod {
    public String getName(){return "Chuyển khoản";}
    public void pay(Student student,long amount){} // Chua ket noi ngan hang thuc
    public String successStatus(){return "Chờ xác nhận chuyển khoản";}
}
