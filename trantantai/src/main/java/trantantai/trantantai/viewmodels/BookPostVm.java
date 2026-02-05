package trantantai.trantantai.viewmodels;

import trantantai.trantantai.entities.Book;
import jakarta.validation.constraints.NotNull;

public record BookPostVm(
    String title,
    String author,
    Double price,
    String categoryId
) {
    public static BookPostVm from(@NotNull Book book) {
        return new BookPostVm(
            book.getTitle(),
            book.getAuthor(),
            book.getPrice(),
            book.getCategoryId()
        );
    }
}
