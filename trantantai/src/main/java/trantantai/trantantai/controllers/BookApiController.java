package trantantai.trantantai.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.services.BookService;
import trantantai.trantantai.services.CategoryService;
import trantantai.trantantai.viewmodels.BookGetVm;
import trantantai.trantantai.viewmodels.BookPostVm;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/books")
@CrossOrigin(origins = "*")
public class BookApiController {

    private final BookService bookService;
    private final CategoryService categoryService;

    @Autowired
    public BookApiController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<BookGetVm>> getAllBooks(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {
        List<Book> books = bookService.getAllBooks(pageNo, pageSize, sortBy);
        List<BookGetVm> bookVms = books.stream()
                .map(BookGetVm::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookVms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookGetVm> getBookById(@PathVariable String id) {
        Optional<Book> bookOpt = bookService.getBookById(id);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BookGetVm.from(bookOpt.get()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookGetVm>> searchBooks(@RequestParam String keyword) {
        List<Book> books = bookService.searchBooks(keyword);
        List<BookGetVm> bookVms = books.stream()
                .map(BookGetVm::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookVms);
    }

    @PostMapping
    public ResponseEntity<BookGetVm> createBook(@RequestBody BookPostVm vm) {
        // Validate categoryId exists
        if (categoryService.getCategoryById(vm.categoryId()).isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Book book = new Book();
        book.setTitle(vm.title());
        book.setAuthor(vm.author());
        book.setPrice(vm.price());
        book.setCategoryId(vm.categoryId());

        bookService.addBook(book);

        Book saved = bookService.getBookById(book.getId()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(BookGetVm.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookGetVm> updateBook(@PathVariable String id, @RequestBody BookPostVm vm) {
        // Check if book exists
        Optional<Book> existingBookOpt = bookService.getBookById(id);
        if (existingBookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Validate categoryId exists
        if (categoryService.getCategoryById(vm.categoryId()).isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Book book = existingBookOpt.get();
        book.setTitle(vm.title());
        book.setAuthor(vm.author());
        book.setPrice(vm.price());
        book.setCategoryId(vm.categoryId());

        bookService.updateBook(book);

        Book updated = bookService.getBookById(id).orElseThrow();
        return ResponseEntity.ok(BookGetVm.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        Optional<Book> bookOpt = bookService.getBookById(id);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }
}
