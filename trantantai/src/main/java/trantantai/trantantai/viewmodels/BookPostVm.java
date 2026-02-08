package trantantai.trantantai.viewmodels;

import trantantai.trantantai.entities.Book;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Book creation/update request model")
public record BookPostVm(
    @Schema(description = "Book title", example = "Clean Code: A Handbook of Agile Software Craftsmanship", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,
    @Schema(description = "Book author", example = "Robert C. Martin", requiredMode = Schema.RequiredMode.REQUIRED)
    String author,
    @Schema(description = "Book price in VND", example = "350000", requiredMode = Schema.RequiredMode.REQUIRED)
    Double price,
    @Schema(description = "Category ID (must exist)", example = "507f1f77bcf86cd799439012", requiredMode = Schema.RequiredMode.REQUIRED)
    String categoryId,
    @Schema(description = "Stock quantity", example = "25")
    Integer quantity,
    @Schema(description = "List of image URLs from Cloudinary", example = "[\"https://res.cloudinary.com/demo/image/upload/v1/books/cleancode.jpg\"]")
    List<String> imageUrls
) {
    public static BookPostVm from(@NotNull Book book) {
        return new BookPostVm(
            book.getTitle(),
            book.getAuthor(),
            book.getPrice(),
            book.getCategoryId(),
            book.getQuantity(),
            book.getImageUrls()
        );
    }
}
