package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Review creation request model.
 */
@Schema(description = "Review creation request model")
public record ReviewPostVm(
    @Schema(
        description = "Book ID to review",
        example = "507f1f77bcf86cd799439011",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Book ID is required")
    String bookId,
    
    @Schema(
        description = "Rating 1-5 stars",
        example = "5",
        minimum = "1",
        maximum = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    int rating,
    
    @Schema(
        description = "Review comment",
        example = "Sách rất hay, nội dung chi tiết và dễ hiểu. Rất đáng để đọc!",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Comment is required")
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    String comment,
    
    @Schema(
        description = "List of image URLs (max 3)",
        example = "[\"https://res.cloudinary.com/demo/image/upload/v1/reviews/review1.jpg\"]"
    )
    @Size(max = 3, message = "Maximum 3 images allowed")
    List<String> imageUrls
) {}
