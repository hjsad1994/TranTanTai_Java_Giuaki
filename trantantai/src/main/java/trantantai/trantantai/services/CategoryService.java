package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Category;
import trantantai.trantantai.repositories.IBookRepository;
import trantantai.trantantai.repositories.ICategoryRepository;
import trantantai.trantantai.repositories.IWishlistRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final ICategoryRepository categoryRepository;
    private final IBookRepository bookRepository;
    private final UserCartService userCartService;
    private final IWishlistRepository wishlistRepository;

    @Autowired
    public CategoryService(ICategoryRepository categoryRepository, IBookRepository bookRepository,
                           UserCartService userCartService, IWishlistRepository wishlistRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.userCartService = userCartService;
        this.wishlistRepository = wishlistRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    public void updateCategory(Category category) {
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        existingCategory.setName(category.getName());
        categoryRepository.save(existingCategory);
    }

    /**
     * Delete category by ID (without cascade - for backward compatibility).
     * @deprecated Use {@link #deleteCategoryWithCascade(String)} instead
     */
    @Deprecated
    public void deleteCategoryById(String id) {
        categoryRepository.deleteById(id);
    }

    /**
     * Delete category and all its books (cascade delete).
     * Also removes affected books from all user carts and wishlists.
     * This is transactional to ensure atomic operation.
     * @param id the category ID to delete
     * @return number of books deleted
     */
    @Transactional
    public long deleteCategoryWithCascade(String id) {
        // First, get all book IDs in this category to clean up carts and wishlists
        List<Book> booksInCategory = bookRepository.findByCategoryId(id);
        List<String> bookIds = booksInCategory.stream()
                .map(Book::getId)
                .collect(Collectors.toList());

        // Remove these books from all user carts and wishlists
        if (!bookIds.isEmpty()) {
            userCartService.removeBooksFromAllCarts(bookIds);
            for (String bookId : bookIds) {
                wishlistRepository.deleteByBookId(bookId);
            }
        }

        // Delete all books in this category
        long deletedBooks = bookRepository.deleteByCategoryId(id);

        // Then delete the category itself
        categoryRepository.deleteById(id);

        return deletedBooks;
    }

    /**
     * Count books in a category.
     * @param categoryId the category ID
     * @return number of books
     */
    public long countBooksInCategory(String categoryId) {
        return bookRepository.countByCategoryId(categoryId);
    }
}
