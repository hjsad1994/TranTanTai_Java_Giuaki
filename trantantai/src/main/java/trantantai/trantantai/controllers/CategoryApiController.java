package trantantai.trantantai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.entities.Category;
import trantantai.trantantai.services.CategoryService;
import trantantai.trantantai.viewmodels.CategoryGetVm;
import trantantai.trantantai.viewmodels.CategoryPostVm;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Categories", description = "Category management APIs - CRUD operations for book categories")
@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = "*")
public class CategoryApiController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Get all categories", description = "Retrieves a list of all book categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved category list")
    @GetMapping
    public ResponseEntity<List<CategoryGetVm>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryGetVm> categoryVms = categories.stream()
                .map(CategoryGetVm::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryVms);
    }

    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its unique identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryGetVm> getCategoryById(@Parameter(description = "Category ID", required = true) @PathVariable String id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category
                .map(cat -> ResponseEntity.ok(CategoryGetVm.from(cat)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get book count in category", description = "Returns the number of books in a specific category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}/book-count")
    public ResponseEntity<Map<String, Long>> getBookCount(@Parameter(description = "Category ID", required = true) @PathVariable String id) {
        if (categoryService.getCategoryById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        long count = categoryService.countBooksInCategory(id);
        return ResponseEntity.ok(Map.of("bookCount", count));
    }

    @Operation(summary = "Create a new category", description = "Creates a new book category")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<CategoryGetVm> createCategory(@Valid @RequestBody CategoryPostVm categoryPostVm) {
        Category category = new Category();
        category.setName(categoryPostVm.name());
        categoryService.addCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryGetVm.from(category));
    }

    @Operation(summary = "Update a category", description = "Updates an existing category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryGetVm> updateCategory(@Parameter(description = "Category ID", required = true) @PathVariable String id, @Valid @RequestBody CategoryPostVm categoryPostVm) {
        Optional<Category> existingCategory = categoryService.getCategoryById(id);
        if (existingCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Category category = existingCategory.get();
        category.setName(categoryPostVm.name());
        categoryService.updateCategory(category);
        return ResponseEntity.ok(CategoryGetVm.from(category));
    }

    @Operation(summary = "Delete a category with cascade", description = "Deletes a category and ALL its associated books permanently.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category and its books deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@Parameter(description = "Category ID", required = true) @PathVariable String id) {
        // Check if category exists
        if (categoryService.getCategoryById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Cascade delete: delete all books first, then the category
        long deletedBooks = categoryService.deleteCategoryWithCascade(id);
        
        return ResponseEntity.ok(Map.of(
            "message", "Category deleted successfully",
            "deletedBooks", deletedBooks
        ));
    }
}
