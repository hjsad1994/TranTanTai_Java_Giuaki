package trantantai.trantantai.viewmodels;

import trantantai.trantantai.entities.Book;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Book response model")
public record BookGetVm(
    @Schema(description = "Book unique identifier", example = "507f1f77bcf86cd799439011")
    String id,
    @Schema(description = "Book title", example = "Clean Code: A Handbook of Agile Software Craftsmanship")
    String title,
    @Schema(description = "Book author", example = "Robert C. Martin")
    String author,
    @Schema(description = "Book price in VND", example = "350000")
    Double price,
    @Schema(description = "Category ID", example = "507f1f77bcf86cd799439012")
    String categoryId,
    @Schema(description = "Category name", example = "Lập trình")
    String categoryName,
    @Schema(description = "Stock quantity", example = "25")
    Integer quantity,
    @Schema(description = "List of image URLs from Cloudinary", example = "[\"https://res.cloudinary.com/demo/image/upload/v1/books/cleancode.jpg\"]")
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
