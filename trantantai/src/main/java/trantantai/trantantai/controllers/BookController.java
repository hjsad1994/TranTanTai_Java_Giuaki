package trantantai.trantantai.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.daos.Item;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.services.BookService;
import trantantai.trantantai.services.CartService;
import trantantai.trantantai.services.CategoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final CartService cartService;

    @Autowired
    public BookController(BookService bookService, CategoryService categoryService, CartService cartService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @GetMapping
    public String showAllBooks(
            @NotNull Model model,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String category) {

        List<Book> books;
        long totalBooks;

        if (category != null && !category.isEmpty()) {
            books = bookService.getBooksByCategory(category, pageNo, pageSize, sortBy);
            totalBooks = bookService.countBooksByCategory(category);
        } else {
            books = bookService.getAllBooks(pageNo, pageSize, sortBy);
            totalBooks = bookService.getTotalBooks();
        }

        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);

        model.addAttribute("books", books);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("totalPages", totalPages > 0 ? totalPages - 1 : 0);
        model.addAttribute("totalBooks", totalBooks);

        return "book/list";
    }

    @GetMapping("/search")
    public String searchBooks(
            @RequestParam String keyword,
            Model model) {
        List<Book> books = bookService.searchBooks(keyword);
        model.addAttribute("books", books);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/list";
    }

    // Show add book form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/add";
    }

    // Process add book form with validation
    @PostMapping("/add")
    public String addBook(
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/add";
        }
        bookService.addBook(book);
        return "redirect:/books";
    }

    // Show edit book form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/edit";
    }

    // Process edit book form with validation
    @PostMapping("/edit/{id}")
    public String updateBook(
            @PathVariable String id,
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
        book.setId(id);
        bookService.updateBook(book);
        return "redirect:/books";
    }

    // Delete book
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable String id) {
        bookService.deleteBookById(id);
        return "redirect:/books";
    }

    // Add to cart - returns JSON for AJAX
    @PostMapping("/add-to-cart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(HttpSession session,
                            @RequestParam String id,
                            @RequestParam String name,
                            @RequestParam double price,
                            @RequestParam(defaultValue = "1") int quantity) {
        var cart = cartService.getCart(session);
        cart.addItems(new Item(id, name, price, quantity));
        cartService.updateCart(session, cart);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã thêm vào giỏ hàng!");
        response.put("cartCount", cartService.getSumQuantity(session));
        response.put("cartTotal", cartService.getSumPrice(session));

        return ResponseEntity.ok(response);
    }
}
