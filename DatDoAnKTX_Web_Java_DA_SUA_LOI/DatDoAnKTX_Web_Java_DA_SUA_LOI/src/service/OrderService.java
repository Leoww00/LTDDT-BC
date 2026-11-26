package service;

import java.io.IOException;
import java.util.*;
import model.*;
import repository.FileRepository;
import utils.IdGenerator;
public class OrderService {
    private final FileRepository repo;
    private final List<Order> orders=new ArrayList<>();
    public OrderService(FileRepository repo){this.repo=repo;}
    public List<Order> getOrders(){return Collections.unmodifiableList(orders);}
    public Order place(Student student,Cart cart,PaymentMethod payment) throws IOException {
        if(cart.isEmpty())throw new IllegalArgumentException("Gio hang rong");
        for(CartItem item:cart.getItems())if(!item.getFood().isAvailable())throw new IllegalArgumentException("Mon "+item.getFood().getName()+" da het hang");
        long total=cart.getTotal();
        if(payment instanceof WalletPayment && student.getBalance()<total)throw new IllegalArgumentException("So du vi khong du");
        Order order=new Order(IdGenerator.nextOrderId(),student.getStudentId(),payment.getName(),cart);
        order.setStatus(payment.successStatus());
        // Ghi file truoc khi tru vi; loi ghi file se khong lam mat tien.
        repo.save(order);
        payment.pay(student,total);
        orders.add(order);cart.clear();return order;
    }
}
