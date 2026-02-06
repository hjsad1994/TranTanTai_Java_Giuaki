package trantantai.trantantai.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Wishlist - stores user's favorite books.
 * Each entry represents one book in a user's wishlist.
 */
@Document(collection = "wishlists")
@CompoundIndex(name = "user_book_idx", def = "{'userId': 1, 'bookId': 1}", unique = true)
public class Wishlist {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String bookId;

    private Date addedAt;

    // Default constructor
    public Wishlist() {
        this.addedAt = new Date();
    }

    // Constructor with userId and bookId
    public Wishlist(String userId, String bookId) {
        this.userId = userId;
        this.bookId = bookId;
        this.addedAt = new Date();
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

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }

    @Override
    public String toString() {
        return "Wishlist{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", addedAt=" + addedAt +
                '}';
    }
}
