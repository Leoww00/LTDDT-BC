package model;

// Thong tin giao hang: nhan tai can tin hoac ship den phong KTX.
public class OrderOptions {
    private final String deliveryMode, recipientName, recipientPhone, address, voucherCode;
    private final boolean forOther;
    public OrderOptions(String mode, boolean forOther, String name, String phone, String address, String voucher) {
        this.deliveryMode = mode == null ? "" : mode.trim();
        this.forOther = forOther;
        this.recipientName = name == null ? "" : name.trim();
        this.recipientPhone = phone == null ? "" : phone.trim();
        this.address = address == null ? "" : address.trim();
        this.voucherCode = voucher == null ? "" : voucher.trim().toUpperCase(java.util.Locale.ROOT);
    }
    public String getDeliveryMode() { return deliveryMode; }
    public boolean isForOther() { return forOther; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public String getAddress() { return address; }
    public String getVoucherCode() { return voucherCode; }
}
