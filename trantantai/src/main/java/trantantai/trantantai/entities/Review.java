package trantantai.trantantai.entities;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Review entity - represents a product review with star rating.
 * Users can submit multiple reviews for the same product (no limit).
 */
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Indexed
    @NotBlank(message = "Book ID is required")
    private String bookId;

    @Indexed
    @NotBlank(message = "User ID is required")
    private String userId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotBlank(message = "Comment is required")
    @Size(min = 1, max = 500, message = "Comment must be between 1 and 500 characters")
    private String comment;

    // Denormalized username for display (avoids extra query)
    private String username;

    private Date createdAt;

    // Image URLs for review (max 3 images)
    @Size(max = 3, message = "Maximum 3 images allowed per review")
    private List<String> imageUrls = new ArrayList<>();

    // Default constructor
    public Review() {
        this.createdAt = new Date();
    }

    // All-args constructor
    public Review(String id, String bookId, String userId, int rating, String comment, String username, Date createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.username = username;
        this.createdAt = createdAt != null ? createdAt : new Date();
    }

    // Constructor for new review
    public Review(String bookId, String userId, int rating, String comment, String username) {
        this.bookId = bookId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.username = username;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", bookId='" + bookId + '\'' +
                ", userId='" + userId + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", imageUrls=" + imageUrls +
                '}';
    }
}
