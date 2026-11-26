package model;

import utils.Validation;
public abstract class User {
    private String studentId, fullName, phone, dormAddress;
    public User(String id, String name, String phone, String room) {
        setStudentId(id); setFullName(name); setPhone(phone); setDormAddress(room);
    }
    public String getStudentId(){return studentId;}
    public void setStudentId(String s){studentId=Validation.required(s,"Ma sinh vien");}
    public String getFullName(){return fullName;}
    public void setFullName(String s){fullName=Validation.required(s,"Ho ten");}
    public String getPhone(){return phone;}
    public void setPhone(String s){phone=Validation.required(s,"So dien thoai");}
    public String getDormAddress(){return dormAddress;}
    public void setDormAddress(String s){dormAddress=Validation.required(s,"Phong KTX");}
    public abstract String displayInfo();
}
