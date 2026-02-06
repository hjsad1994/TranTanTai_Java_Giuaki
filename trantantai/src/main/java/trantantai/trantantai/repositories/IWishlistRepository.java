package trantantai.trantantai.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import trantantai.trantantai.entities.Wishlist;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWishlistRepository extends MongoRepository<Wishlist, String> {

    /**
     * Find all wishlist items for a user
     */
    List<Wishlist> findByUserIdOrderByAddedAtDesc(String userId);

    /**
     * Find a specific wishlist entry by user and book
     */
    Optional<Wishlist> findByUserIdAndBookId(String userId, String bookId);

    /**
     * Check if a book is in user's wishlist
     */
    boolean existsByUserIdAndBookId(String userId, String bookId);

    /**
     * Delete a book from user's wishlist
     */
    void deleteByUserIdAndBookId(String userId, String bookId);

    /**
     * Delete all wishlist items for a user
     */
    void deleteByUserId(String userId);

    /**
     * Count wishlist items for a user
     */
    long countByUserId(String userId);
}
