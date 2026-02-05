package trantantai.trantantai.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Category;
import trantantai.trantantai.repositories.IBookRepository;
import trantantai.trantantai.services.CategoryService;
import trantantai.trantantai.viewmodels.CategoryGetVm;
import trantantai.trantantai.viewmodels.CategoryPostVm;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = "*")
public class CategoryApiController {

    private final CategoryService categoryService;
    private final IBookRepository bookRepository;

    @Autowired
    public CategoryApiController(CategoryService categoryService, IBookRepository bookRepository) {
        this.categoryService = categoryService;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public ResponseEntity<List<CategoryGetVm>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryGetVm> categoryVms = categories.stream()
                .map(CategoryGetVm::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryVms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryGetVm> getCategoryById(@PathVariable String id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category
                .map(cat -> ResponseEntity.ok(CategoryGetVm.from(cat)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryGetVm> createCategory(@Valid @RequestBody CategoryPostVm categoryPostVm) {
        Category category = new Category();
        category.setName(categoryPostVm.name());
        categoryService.addCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryGetVm.from(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryGetVm> updateCategory(@PathVariable String id, @Valid @RequestBody CategoryPostVm categoryPostVm) {
        Optional<Category> existingCategory = categoryService.getCategoryById(id);
        if (existingCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Category category = existingCategory.get();
        category.setName(categoryPostVm.name());
        categoryService.updateCategory(category);
        return ResponseEntity.ok(CategoryGetVm.from(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        // Check if category exists
        if (categoryService.getCategoryById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if category has books
        List<Book> books = bookRepository.findByCategoryId(id);
        if (!books.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }
}
