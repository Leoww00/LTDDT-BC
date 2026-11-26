package service;

import model.Student;
public class WalletPayment implements PaymentMethod {
    public String getName(){return "Vi sinh vien";}
    public void pay(Student student,long amount){student.deduct(amount);}
    public String successStatus(){return "Da thanh toan";}
}
