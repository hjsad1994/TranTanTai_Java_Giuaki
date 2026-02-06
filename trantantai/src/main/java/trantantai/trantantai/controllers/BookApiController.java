package trantantai.trantantai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.services.BookService;
import trantantai.trantantai.services.CategoryService;
import trantantai.trantantai.viewmodels.BookGetVm;
import trantantai.trantantai.viewmodels.BookPostVm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Books", description = "Book management APIs - CRUD operations for books")
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

    @Operation(summary = "Get all books", description = "Retrieves a paginated list of all books")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved book list")
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

    @Operation(summary = "Get book by ID", description = "Retrieves a specific book by its unique identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book found"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookGetVm> getBookById(@Parameter(description = "Book ID", required = true) @PathVariable String id) {
        Optional<Book> bookOpt = bookService.getBookById(id);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(BookGetVm.from(bookOpt.get()));
    }

    @Operation(summary = "Search books", description = "Searches books by keyword in title or author")
    @ApiResponse(responseCode = "200", description = "Search results returned")
    @GetMapping("/search")
    public ResponseEntity<List<BookGetVm>> searchBooks(@Parameter(description = "Search keyword", required = true) @RequestParam String keyword) {
        List<Book> books = bookService.searchBooks(keyword);
        List<BookGetVm> bookVms = books.stream()
                .map(BookGetVm::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookVms);
    }

    @Operation(summary = "Create a new book", description = "Creates a new book with the provided details")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Book created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input - category not found")
    })
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
        book.setImageUrls(vm.imageUrls() != null ? vm.imageUrls() : new ArrayList<>());

        bookService.addBook(book);

        Book saved = bookService.getBookById(book.getId()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(BookGetVm.from(saved));
    }

    @Operation(summary = "Update a book", description = "Updates an existing book with the provided details")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book updated successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input - category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookGetVm> updateBook(@Parameter(description = "Book ID", required = true) @PathVariable String id, @RequestBody BookPostVm vm) {
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
        book.setImageUrls(vm.imageUrls() != null ? vm.imageUrls() : new ArrayList<>());

        bookService.updateBook(book);

        Book updated = bookService.getBookById(id).orElseThrow();
        return ResponseEntity.ok(BookGetVm.from(updated));
    }

    @Operation(summary = "Delete a book", description = "Deletes a book by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@Parameter(description = "Book ID", required = true) @PathVariable String id) {
        Optional<Book> bookOpt = bookService.getBookById(id);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update book quantity", description = "Updates the stock quantity of a book")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Quantity updated successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "400", description = "Invalid quantity")
    })
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<BookGetVm> updateBookQuantity(
            @Parameter(description = "Book ID", required = true) @PathVariable String id,
            @RequestBody java.util.Map<String, Integer> payload) {

        Optional<Book> existingBookOpt = bookService.getBookById(id);
        if (existingBookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Integer newQuantity = payload.get("quantity");
        if (newQuantity == null || newQuantity < 0) {
            return ResponseEntity.badRequest().build();
        }

        Book book = existingBookOpt.get();
        book.setQuantity(newQuantity);
        bookService.updateBook(book);

        Book updated = bookService.getBookById(id).orElseThrow();
        return ResponseEntity.ok(BookGetVm.from(updated));
    }
}
