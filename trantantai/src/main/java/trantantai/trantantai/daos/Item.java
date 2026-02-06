package trantantai.trantantai.daos;

import java.util.Objects;

public class Item {
    private String bookId;
    private String bookName;
    private Double price;
    private int quantity;

    // Default constructor
    public Item() {
    }

    // All-args constructor
    public Item(String bookId, String bookName, Double price, int quantity) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(bookId, item.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return "Item{" +
                "bookId='" + bookId + '\'' +
                ", bookName='" + bookName + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
