package service;

import model.Student;
public interface PaymentMethod {
    String getName();
    void pay(Student student,long amount);
    String successStatus();
}
