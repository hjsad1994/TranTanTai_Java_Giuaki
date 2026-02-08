package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Wishlist;
import trantantai.trantantai.repositories.IWishlistRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private IWishlistRepository wishlistRepository;

    @Autowired
    private BookService bookService;

    /**
     * Add a book to user's wishlist
     */
    public Wishlist addToWishlist(String userId, String bookId) {
        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            return wishlistRepository.findByUserIdAndBookId(userId, bookId).orElse(null);
        }

        Wishlist wishlist = new Wishlist(userId, bookId);
        return wishlistRepository.save(wishlist);
    }

    /**
     * Remove a book from user's wishlist
     */
    public void removeFromWishlist(String userId, String bookId) {
        wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    /**
     * Check if a book is in user's wishlist
     */
    public boolean isInWishlist(String userId, String bookId) {
        return wishlistRepository.existsByUserIdAndBookId(userId, bookId);
    }

    /**
     * Get all wishlist items for a user with book details
     */
    public List<Book> getWishlistBooks(String userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByAddedAtDesc(userId);

        return wishlists.stream()
                .map(w -> {
                    Optional<Book> book = bookService.getBookById(w.getBookId());
                    return book.orElse(null);
                })
                .filter(book -> book != null)
                .collect(Collectors.toList());
    }

    /**
     * Get wishlist entries for a user
     */
    public List<Wishlist> getWishlistEntries(String userId) {
        return wishlistRepository.findByUserIdOrderByAddedAtDesc(userId);
    }

    /**
     * Get wishlist count for a user
     */
    public long getWishlistCount(String userId) {
        return wishlistRepository.countByUserId(userId);
    }

    /**
     * Clear all wishlist items for a user
     */
    public void clearWishlist(String userId) {
        wishlistRepository.deleteByUserId(userId);
    }

    /**
     * Toggle a book in wishlist (add if not present, remove if present)
     */
    public boolean toggleWishlist(String userId, String bookId) {
        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
            return false; // Removed
        } else {
            wishlistRepository.save(new Wishlist(userId, bookId));
            return true; // Added
        }
    }

    /**
     * Remove a book from all users' wishlists (when book is deleted)
     */
    public void removeBookFromAllWishlists(String bookId) {
        wishlistRepository.deleteByBookId(bookId);
    }

    /**
     * Remove multiple books from all wishlists (when category is deleted)
     */
    public void removeBooksFromAllWishlists(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return;
        }
        for (String bookId : bookIds) {
            wishlistRepository.deleteByBookId(bookId);
        }
    }
}
