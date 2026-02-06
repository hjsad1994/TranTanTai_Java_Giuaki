package trantantai.trantantai.entities;

import org.springframework.data.annotation.Transient;

import java.util.Objects;

/**
 * ItemInvoice - represents a single item in an invoice.
 * This is an embedded document (NOT a separate collection).
 * DO NOT add @Document annotation.
 */
public class ItemInvoice {

    private String id;
    private int quantity;
    private String bookId;

    // Transient field - populated from service layer
    @Transient
    private Book book;

    // Default constructor
    public ItemInvoice() {
    }

    // All-args constructor
    public ItemInvoice(String id, int quantity, String bookId) {
        this.id = id;
        this.quantity = quantity;
        this.bookId = bookId;
    }

    // Constructor without id (for new items)
    public ItemInvoice(int quantity, String bookId) {
        this.quantity = quantity;
        this.bookId = bookId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemInvoice that = (ItemInvoice) o;
        return Objects.equals(id, that.id) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bookId);
    }

    @Override
    public String toString() {
        return "ItemInvoice{" +
                "id='" + id + '\'' +
                ", quantity=" + quantity +
                ", bookId='" + bookId + '\'' +
                '}';
    }
}
