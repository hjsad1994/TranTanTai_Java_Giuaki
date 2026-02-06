package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import trantantai.trantantai.entities.Review;

import java.util.Date;
import java.util.List;

/**
 * Review response model for API.
 */
@Schema(description = "Review response model")
public record ReviewGetVm(
    @Schema(description = "Review unique identifier")
    String id,
    
    @Schema(description = "Book ID")
    String bookId,
    
    @Schema(description = "User ID")
    String userId,
    
    @Schema(description = "Rating 1-5 stars")
    int rating,
    
    @Schema(description = "Review comment")
    String comment,
    
    @Schema(description = "Username of reviewer")
    String username,
    
    @Schema(description = "First character of username for avatar")
    String userInitial,
    
    @Schema(description = "Review creation date")
    Date createdAt,
    
    @Schema(description = "List of image URLs")
    List<String> imageUrls
) {
    /**
     * Create a ReviewGetVm from a Review entity.
     */
    public static ReviewGetVm from(@NotNull Review review) {
        String initial = review.getUsername() != null && !review.getUsername().isEmpty()
            ? review.getUsername().substring(0, 1).toUpperCase()
            : "?";
        return new ReviewGetVm(
            review.getId(),
            review.getBookId(),
            review.getUserId(),
            review.getRating(),
            review.getComment(),
            review.getUsername(),
            initial,
            review.getCreatedAt(),
            review.getImageUrls()
        );
    }
}
