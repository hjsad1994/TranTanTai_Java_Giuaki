package trantantai.trantantai.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import trantantai.trantantai.entities.Review;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Review entity.
 * Provides methods for CRUD operations and custom queries.
 */
@Repository
public interface IReviewRepository extends MongoRepository<Review, String> {

    /**
     * Find all reviews for a book, paginated, sorted by creation date descending (newest first).
     */
    Page<Review> findByBookIdOrderByCreatedAtDesc(String bookId, Pageable pageable);

    /**
     * Find all reviews for a book, sorted by creation date descending (newest first).
     */
    List<Review> findByBookIdOrderByCreatedAtDesc(String bookId);

    /**
     * Find a review by book ID and user ID.
     * Used to check if user has already reviewed this book.
     */
    Optional<Review> findByBookIdAndUserId(String bookId, String userId);

    /**
     * Check if a review exists for the given book and user.
     */
    boolean existsByBookIdAndUserId(String bookId, String userId);

    /**
     * Count total reviews for a book.
     */
    long countByBookId(String bookId);

    /**
     * Count reviews for a book with a specific rating.
     * Used for rating breakdown statistics.
     */
    long countByBookIdAndRating(String bookId, int rating);
}
