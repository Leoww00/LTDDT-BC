package service;

import model.Student;
public class WalletPayment implements PaymentMethod {
    public String getName(){return "Ví sinh viên";}
    public void pay(Student student,long amount){student.deduct(amount);}
    public String successStatus(){return "Đã thanh toán";}
}
