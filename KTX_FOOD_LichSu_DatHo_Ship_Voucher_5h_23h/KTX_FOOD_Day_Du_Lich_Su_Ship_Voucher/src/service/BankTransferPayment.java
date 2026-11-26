package service;

import model.Student;
public class BankTransferPayment implements PaymentMethod {
    public String getName(){return "Chuyen khoan";}
    public void pay(Student student,long amount){} // Chua ket noi ngan hang thuc
    public String successStatus(){return "Cho xac nhan chuyen khoan";}
}
