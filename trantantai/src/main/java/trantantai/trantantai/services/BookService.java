package trantantai.trantantai.services;

import com.mongodb.client.result.UpdateResult;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Category;
import trantantai.trantantai.repositories.IBookRepository;
import trantantai.trantantai.repositories.ICategoryRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final IBookRepository bookRepository;
    private final ICategoryRepository categoryRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public BookService(IBookRepository bookRepository, ICategoryRepository categoryRepository, MongoTemplate mongoTemplate) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Book> getAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        Page<Book> pagedResult = bookRepository.findAll(pageRequest);
        
        List<Book> books = pagedResult.getContent();
        // Populate category for each book
        books.forEach(this::populateCategory);
        
        return books;
    }

    public long getTotalBooks() {
        return bookRepository.count();
    }

    public List<Book> getBooksByCategory(String categoryId, Integer pageNo, Integer pageSize, String sortBy) {
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        List<Book> books = bookRepository.findByCategoryId(categoryId, pageRequest);
        books.forEach(this::populateCategory);
        return books;
    }

    public long countBooksByCategory(String categoryId) {
        return bookRepository.countByCategoryId(categoryId);
    }

    public Optional<Book> getBookById(String id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        bookOpt.ifPresent(this::populateCategory);
        return bookOpt;
    }

    public void addBook(Book book) {
        bookRepository.save(book);
    }

    public void updateBook(@NotNull Book book) {
        Book existingBook = bookRepository.findById(book.getId())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setCategoryId(book.getCategoryId());
        existingBook.setQuantity(book.getQuantity());
        existingBook.setImageUrls(book.getImageUrls());
        bookRepository.save(existingBook);
    }

    public void deleteBookById(String id) {
        bookRepository.deleteById(id);
    }

    /**
     * Atomically decrement book stock.
     * @param bookId the book ID
     * @param quantity the quantity to decrement
     * @return true if decrement successful, false if insufficient stock
     */
    public boolean decrementStock(String bookId, int quantity) {
        Query query = new Query(Criteria.where("_id").is(bookId).and("quantity").gte(quantity));
        Update update = new Update().inc("quantity", -quantity);
        UpdateResult result = mongoTemplate.updateFirst(query, update, Book.class);
        return result.getModifiedCount() > 0;
    }

    /**
     * Atomically increment book stock (for order cancellation).
     * @param bookId the book ID
     * @param quantity the quantity to increment
     * @return true if increment successful
     */
    public boolean incrementStock(String bookId, int quantity) {
        Query query = new Query(Criteria.where("_id").is(bookId));
        Update update = new Update().inc("quantity", quantity);
        UpdateResult result = mongoTemplate.updateFirst(query, update, Book.class);
        return result.getModifiedCount() > 0;
    }

    /**
     * Delete all books by category ID.
     * Used for cascade delete when deleting a category.
     * @param categoryId the category ID
     * @return number of books deleted
     */
    public long deleteBooksByCategoryId(String categoryId) {
        return bookRepository.deleteByCategoryId(categoryId);
    }

    /**
     * Count books by category ID.
     * @param categoryId the category ID
     * @return number of books in the category
     */
    public long countBooksByCategoryId(String categoryId) {
        return bookRepository.countByCategoryId(categoryId);
    }

    /**
     * Enhanced search: searches by title OR author OR category name.
     * Uses Set to avoid duplicate results.
     */
    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Use LinkedHashSet to maintain insertion order and avoid duplicates
        Set<Book> resultSet = new LinkedHashSet<>();
        
        // 1. Search books by title
        List<Book> booksByTitle = bookRepository.findByTitleContainingIgnoreCase(keyword);
        resultSet.addAll(booksByTitle);
        
        // 2. Search books by author
        List<Book> booksByAuthor = bookRepository.findByAuthorContainingIgnoreCase(keyword);
        resultSet.addAll(booksByAuthor);
        
        // 3. Search categories by name, then find books with those categoryIds
        List<Category> matchingCategories = categoryRepository.findByNameContainingIgnoreCase(keyword);
        if (!matchingCategories.isEmpty()) {
            List<String> categoryIds = matchingCategories.stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
            List<Book> booksByCategory = bookRepository.findByCategoryIdIn(categoryIds);
            resultSet.addAll(booksByCategory);
        }
        
        // Convert to list and populate category for each book
        List<Book> result = new ArrayList<>(resultSet);
        result.forEach(this::populateCategory);
        
        return result;
    }

    // Helper method to populate transient category field
    private void populateCategory(Book book) {
        if (book.getCategoryId() != null) {
            categoryRepository.findById(book.getCategoryId())
                    .ifPresent(book::setCategory);
        }
    }
}
