package main;

import model.*;
import service.*;
import repository.FileRepository;
import utils.ProjectPaths;
public class AppContext {
    private final Student student=new Student("SV001","Nguyen Van A","0912345678","A101",200000);
    private final Cart cart=new Cart();
    private final FoodService foods=new FoodService();
    private final OrderService orders=new OrderService(new FileRepository(ProjectPaths.root().resolve("data/orders.txt").toString()));
    public Student getStudent(){return student;}
    public Cart getCart(){return cart;}
    public FoodService getFoods(){return foods;}
    public OrderService getOrders(){return orders;}
    public PaymentMethod payment(int type){switch(type){case 1:return new WalletPayment();case 2:return new CashPayment();case 3:return new BankTransferPayment();default:throw new IllegalArgumentException("Phuong thuc thanh toan khong hop le");}}
}
