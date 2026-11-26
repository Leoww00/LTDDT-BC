package model;

import utils.Validation;
public abstract class User {
    private String studentId, fullName, phone, dormAddress;
    public User(String id, String name, String phone, String room) {
        setStudentId(id);
        setFullName(name);
        setPhone(phone);
        setDormAddress(room);
    }
    public String getStudentId(){
        return studentId;
    }
    public void setStudentId(String s){
        studentId=Validation.required(s,"Mã sinh viên");
    }
    public String getFullName(){
        return fullName;
    }
    public void setFullName(String s){
        fullName=Validation.required(s,"Họ tên");
    }
    public String getPhone(){
        return phone;
    }
    public void setPhone(String s){
        phone=Validation.required(s,"Số điện thoại");
    }
    public String getDormAddress(){
        return dormAddress;
    }
    public void setDormAddress(String s){
        dormAddress=Validation.required(s,"Phòng KTX");
    }
    public abstract String displayInfo();
}
