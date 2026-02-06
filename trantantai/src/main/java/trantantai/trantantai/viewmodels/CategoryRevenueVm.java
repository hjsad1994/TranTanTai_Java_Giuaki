package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Category revenue distribution data.
 */
@Schema(description = "Category revenue statistics")
public record CategoryRevenueVm(
    @Schema(description = "Category unique identifier", example = "507f1f77bcf86cd799439012")
    String categoryId,
    
    @Schema(description = "Category name", example = "Cong nghe")
    String categoryName,
    
    @Schema(description = "Total revenue from this category", example = "45000000")
    Double revenue,
    
    @Schema(description = "Percentage of total revenue", example = "35.5")
    Double percentage
) {}
