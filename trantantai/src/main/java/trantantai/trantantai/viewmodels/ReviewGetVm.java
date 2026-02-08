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
    @Schema(description = "Review unique identifier", example = "507f1f77bcf86cd799439099")
    String id,
    
    @Schema(description = "Book ID", example = "507f1f77bcf86cd799439011")
    String bookId,
    
    @Schema(description = "User ID", example = "507f1f77bcf86cd799439001")
    String userId,
    
    @Schema(description = "Rating 1-5 stars", example = "5")
    int rating,
    
    @Schema(description = "Review comment", example = "Sách rất hay, nội dung chi tiết!")
    String comment,
    
    @Schema(description = "Username of reviewer", example = "nguyen_van_a")
    String username,
    
    @Schema(description = "First character of username for avatar", example = "N")
    String userInitial,
    
    @Schema(description = "Review creation date", example = "2024-01-15T10:30:00.000Z")
    Date createdAt,
    
    @Schema(description = "List of image URLs", example = "[\"https://res.cloudinary.com/demo/image/upload/v1/reviews/img1.jpg\"]")
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
