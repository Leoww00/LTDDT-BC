package main;

import model.*;
import service.*;
import repository.*;
import utils.ProjectPaths;
import java.io.IOException;

// Noi tap trung cac thanh phan cua ung dung.
public class AppContext {
    private final AccountService accounts;
    private final FoodService foods = new FoodService();
    private final OrderService orders;

    public AppContext() throws IOException {
        accounts = new AccountService(new AccountRepository(ProjectPaths.root().resolve("data/student_wallets.tsv")));
        orders = new OrderService(new FileRepository(ProjectPaths.root().resolve("data/orders.txt").toString()));
    }
    public AccountService getAccounts() { return accounts; }
    public FoodService getFoods() { return foods; }
    public OrderService getOrders() { return orders; }
    public PaymentMethod payment(String code) {
        switch (code) {
            case "wallet": return new WalletPayment();
            case "cash": return new CashPayment();
            case "bank": return new BankTransferPayment();
            default: throw new IllegalArgumentException("Phuong thuc thanh toan khong hop le.");
        }
    }
}
