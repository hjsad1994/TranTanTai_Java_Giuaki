package trantantai.trantantai.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import trantantai.trantantai.daos.Item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UserCart - persisted shopping cart for user session continuity.
 * Cart is saved to DB on logout and restored on login.
 */
@Document(collection = "user_carts")
public class UserCart {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private List<Item> cartItems = new ArrayList<>();

    private Date lastUpdated;

    // Default constructor
    public UserCart() {
        this.lastUpdated = new Date();
    }

    // Constructor with userId
    public UserCart(String userId) {
        this.userId = userId;
        this.lastUpdated = new Date();
    }

    // Constructor with all fields
    public UserCart(String userId, List<Item> cartItems) {
        this.userId = userId;
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.lastUpdated = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Item> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<Item> cartItems) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.lastUpdated = new Date();
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "UserCart{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", cartItems=" + cartItems.size() + " items" +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
