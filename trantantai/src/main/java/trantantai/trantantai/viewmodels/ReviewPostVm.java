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
    @Schema(description = "Book ID to review", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Book ID is required")
    String bookId,
    
    @Schema(description = "Rating 1-5 stars", minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    int rating,
    
    @Schema(description = "Review comment", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Comment is required")
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    String comment,
    
    @Schema(description = "List of image URLs (max 3)")
    @Size(max = 3, message = "Maximum 3 images allowed")
    List<String> imageUrls
) {}
