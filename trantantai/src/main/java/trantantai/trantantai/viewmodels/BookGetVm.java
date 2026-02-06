package trantantai.trantantai.viewmodels;

import trantantai.trantantai.entities.Book;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Book response model")
public record BookGetVm(
    @Schema(description = "Book unique identifier", example = "507f1f77bcf86cd799439011")
    String id,
    @Schema(description = "Book title", example = "Clean Code")
    String title,
    @Schema(description = "Book author", example = "Robert C. Martin")
    String author,
    @Schema(description = "Book price in USD", example = "29.99")
    Double price,
    @Schema(description = "Category ID", example = "507f1f77bcf86cd799439012")
    String categoryId,
    @Schema(description = "Category name", example = "Programming")
    String categoryName,
    @Schema(description = "Stock quantity", example = "10")
    Integer quantity,
    @Schema(description = "List of image URLs from Cloudinary")
    List<String> imageUrls
) {
    public static BookGetVm from(@NotNull Book book) {
        String categoryName = book.getCategory() != null ? book.getCategory().getName() : null;
        return new BookGetVm(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPrice(),
            book.getCategoryId(),
            categoryName,
            book.getQuantity(),
            book.getImageUrls()
        );
    }
}
