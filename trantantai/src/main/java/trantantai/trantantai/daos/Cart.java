package trantantai.trantantai.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Cart {
    private List<Item> cartItems = new ArrayList<>();

    // Default constructor
    public Cart() {
    }

    // Getter and Setter
    public List<Item> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<Item> cartItems) {
        this.cartItems = cartItems;
    }

    // Add item to cart (increase quantity if exists)
    public void addItems(Item item) {
        boolean isExist = cartItems.stream()
                .filter(i -> Objects.equals(i.getBookId(), item.getBookId()))
                .findFirst()
                .map(i -> {
                    i.setQuantity(i.getQuantity() + item.getQuantity());
                    return true;
                })
                .orElse(false);

        if (!isExist) {
            cartItems.add(item);
        }
    }

    // Remove item from cart
    public void removeItems(String bookId) {
        cartItems.removeIf(item -> Objects.equals(item.getBookId(), bookId));
    }

    // Update item quantity
    public void updateItems(String bookId, int quantity) {
        cartItems.stream()
                .filter(item -> Objects.equals(item.getBookId(), bookId))
                .forEach(item -> item.setQuantity(quantity));
    }

    @Override
    public String toString() {
        return "Cart{" +
                "cartItems=" + cartItems +
                '}';
    }
}
