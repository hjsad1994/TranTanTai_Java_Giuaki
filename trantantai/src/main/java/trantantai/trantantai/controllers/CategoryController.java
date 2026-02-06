package trantantai.trantantai.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.entities.Category;
import trantantai.trantantai.services.CategoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // List all categories
    @GetMapping
    public String listCategories(@NotNull Model model) {
        List<Category> categories = categoryService.getAllCategories();

        // Build a map of categoryId -> bookCount for the view
        Map<String, Long> bookCountMap = new HashMap<>();
        long totalBooks = 0;
        for (Category category : categories) {
            long count = categoryService.countBooksInCategory(category.getId());
            bookCountMap.put(category.getId(), count);
            totalBooks += count;
        }

        model.addAttribute("categories", categories);
        model.addAttribute("bookCountMap", bookCountMap);
        model.addAttribute("totalBooks", totalBooks);
        return "category/list";
    }

    // Show add category form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "category/add";
    }

    // Process add category form
    @PostMapping("/add")
    public String addCategory(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "category/add";
        }
        categoryService.addCategory(category);
        return "redirect:/categories";
    }

    // Show edit category form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        model.addAttribute("category", category);
        return "category/edit";
    }

    // Process edit category form
    @PostMapping("/edit/{id}")
    public String updateCategory(
            @PathVariable String id,
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "category/edit";
        }
        category.setId(id);
        categoryService.updateCategory(category);
        return "redirect:/categories";
    }

    // Delete category with cascade (deletes all books in category)
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable String id) {
        categoryService.deleteCategoryWithCascade(id);
        return "redirect:/categories";
    }
}
