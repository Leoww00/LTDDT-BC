package model;

// Bang tinh tien do Java tinh, giao dien chi hien ket qua.
public class OrderQuote {
    private final long subtotal, shippingFee, discount, total;
    private final String voucherCode;
    public OrderQuote(long subtotal, long shippingFee, long discount, String voucherCode) {
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.discount = discount;
        this.total = Math.subtractExact(Math.addExact(subtotal, shippingFee), discount);
        this.voucherCode = voucherCode;
    }
    public long getSubtotal() { return subtotal; }
    public long getShippingFee() { return shippingFee; }
    public long getDiscount() { return discount; }
    public long getTotal() { return total; }
    public String getVoucherCode() { return voucherCode; }
}
