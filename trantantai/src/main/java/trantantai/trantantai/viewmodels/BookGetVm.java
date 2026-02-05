package trantantai.trantantai.viewmodels;

import trantantai.trantantai.entities.Book;
import jakarta.validation.constraints.NotNull;

public record BookGetVm(
    String id,
    String title,
    String author,
    Double price,
    String categoryId,
    String categoryName
) {
    public static BookGetVm from(@NotNull Book book) {
        String categoryName = book.getCategory() != null ? book.getCategory().getName() : null;
        return new BookGetVm(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPrice(),
            book.getCategoryId(),
            categoryName
        );
    }
}
