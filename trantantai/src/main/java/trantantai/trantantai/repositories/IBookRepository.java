package trantantai.trantantai.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import trantantai.trantantai.entities.Book;

import java.util.List;

@Repository
public interface IBookRepository extends MongoRepository<Book, String> {
    
    // Find books by category ID
    List<Book> findByCategoryId(String categoryId);

    // Find books by category ID with pagination
    List<Book> findByCategoryId(String categoryId, Pageable pageable);
    
    // Paginated find all
    Page<Book> findAll(Pageable pageable);
    
    // Search by title containing keyword (case-insensitive)
    List<Book> findByTitleContainingIgnoreCase(String keyword);
    
    // Search by author containing keyword (case-insensitive)
    List<Book> findByAuthorContainingIgnoreCase(String keyword);
    
    // Search books by multiple category IDs
    List<Book> findByCategoryIdIn(List<String> categoryIds);
    
    // Delete all books by category ID (for cascade delete)
    long deleteByCategoryId(String categoryId);
    
    // Count books by category ID
    long countByCategoryId(String categoryId);
}
