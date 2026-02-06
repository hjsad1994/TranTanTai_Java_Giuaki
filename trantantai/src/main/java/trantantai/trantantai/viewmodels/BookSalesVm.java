package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Book sales data for top selling books report.
 */
@Schema(description = "Book sales statistics")
public record BookSalesVm(
    @Schema(description = "Book unique identifier", example = "507f1f77bcf86cd799439011")
    String bookId,
    
    @Schema(description = "Book title", example = "Clean Code")
    String title,
    
    @Schema(description = "Book author", example = "Robert C. Martin")
    String author,
    
    @Schema(description = "Total units sold", example = "128")
    Integer soldCount,
    
    @Schema(description = "Total revenue from this book", example = "12800000")
    Double revenue,
    
    @Schema(description = "Book cover image URL")
    String imageUrl
) {}
