package trantantai.trantantai.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import trantantai.trantantai.constants.PaymentStatus;
import trantantai.trantantai.constants.PaymentMethod;
import trantantai.trantantai.constants.OrderStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Invoice - represents a checkout order.
 * ItemInvoices are embedded as a list (not a separate collection).
 */
@Document(collection = "invoices")
public class Invoice {

    @Id
    private String id;

    private Date invoiceDate;

    private Double price;

    // Embedded list of items (NOT @DBRef - embedded documents)
    private List<ItemInvoice> itemInvoices = new ArrayList<>();

    // Reference to user who created this invoice
    private String userId;

    // Payment tracking fields
    private PaymentStatus paymentStatus = PaymentStatus.COD_PENDING;
    private PaymentMethod paymentMethod = PaymentMethod.COD;
    private String momoTransactionId;
    private String momoRequestId;

    // Order lifecycle status (separate from payment status)
    private OrderStatus orderStatus = OrderStatus.PROCESSING;

    // Default constructor
    public Invoice() {
        this.invoiceDate = new Date();
    }

    // All-args constructor
    public Invoice(String id, Date invoiceDate, Double price, List<ItemInvoice> itemInvoices, String userId, 
                   PaymentStatus paymentStatus, PaymentMethod paymentMethod, String momoTransactionId, String momoRequestId,
                   OrderStatus orderStatus) {
        this.id = id;
        this.invoiceDate = invoiceDate;
        this.price = price;
        this.itemInvoices = itemInvoices != null ? itemInvoices : new ArrayList<>();
        this.userId = userId;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.momoTransactionId = momoTransactionId;
        this.momoRequestId = momoRequestId;
        this.orderStatus = orderStatus != null ? orderStatus : OrderStatus.PROCESSING;
    }

    // Constructor for new invoice
    public Invoice(Double price) {
        this.invoiceDate = new Date();
        this.price = price;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<ItemInvoice> getItemInvoices() {
        return itemInvoices;
    }

    public void setItemInvoices(List<ItemInvoice> itemInvoices) {
        this.itemInvoices = itemInvoices;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMomoTransactionId() {
        return momoTransactionId;
    }

    public void setMomoTransactionId(String momoTransactionId) {
        this.momoTransactionId = momoTransactionId;
    }

    public String getMomoRequestId() {
        return momoRequestId;
    }

    public void setMomoRequestId(String momoRequestId) {
        this.momoRequestId = momoRequestId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    // Helper method to add item
    public void addItemInvoice(ItemInvoice itemInvoice) {
        this.itemInvoices.add(itemInvoice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(id, invoice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id='" + id + '\'' +
                ", invoiceDate=" + invoiceDate +
                ", price=" + price +
                ", userId='" + userId + '\'' +
                ", paymentStatus=" + paymentStatus +
                ", paymentMethod=" + paymentMethod +
                ", orderStatus=" + orderStatus +
                ", momoTransactionId='" + momoTransactionId + '\'' +
                ", momoRequestId='" + momoRequestId + '\'' +
                ", itemInvoices=" + itemInvoices +
                '}';
    }
}
