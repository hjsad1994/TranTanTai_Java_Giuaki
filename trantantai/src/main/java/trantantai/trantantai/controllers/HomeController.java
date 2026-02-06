package trantantai.trantantai.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.services.BookService;
import trantantai.trantantai.services.CategoryService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class HomeController {

    private final BookService bookService;
    private final CategoryService categoryService;

    @Autowired
    public HomeController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String home(Model model) {
        // Get featured books (first 8 books)
        List<Book> featuredBooks = bookService.getAllBooks(0, 8, "title");
        model.addAttribute("featuredBooks", featuredBooks);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "home/index";
    }

    @GetMapping("/book/{id}")
    public String bookDetail(@PathVariable String id, Model model) {
        Optional<Book> bookOpt = bookService.getBookById(id);
        if (bookOpt.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("book", bookOpt.get());

        // Get related books (same category, excluding current book)
        List<Book> allBooks = bookService.getAllBooks(0, 10, "title");
        Book currentBook = bookOpt.get();
        List<Book> relatedBooks = allBooks.stream()
                .filter(b -> !b.getId().equals(id))
                .filter(b -> currentBook.getCategoryId() != null &&
                            currentBook.getCategoryId().equals(b.getCategoryId()))
                .limit(4)
                .toList();
        model.addAttribute("relatedBooks", relatedBooks);

        return "book/detail";
    }
}
