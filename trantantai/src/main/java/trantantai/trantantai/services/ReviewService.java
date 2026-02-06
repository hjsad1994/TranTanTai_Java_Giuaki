package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import trantantai.trantantai.entities.Review;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.repositories.IInvoiceRepository;
import trantantai.trantantai.repositories.IReviewRepository;
import trantantai.trantantai.repositories.IUserRepository;
import trantantai.trantantai.viewmodels.ModerationResultVm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service for managing product reviews.
 * Handles review CRUD, purchase verification, and statistics.
 */
@Service
public class ReviewService {

    private static final Logger logger = Logger.getLogger(ReviewService.class.getName());

    private final IReviewRepository reviewRepository;
    private final IInvoiceRepository invoiceRepository;
    private final IUserRepository userRepository;
    private final OpenAIModerationService moderationService;

    @Autowired
    public ReviewService(IReviewRepository reviewRepository, 
                         IInvoiceRepository invoiceRepository,
                         IUserRepository userRepository,
                         OpenAIModerationService moderationService) {
        this.reviewRepository = reviewRepository;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.moderationService = moderationService;
    }

    /**
     * Get paginated reviews for a book, sorted by creation date descending.
     */
    public Page<Review> getReviewsByBookId(String bookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, pageable);
    }

    /**
     * Get all reviews for a book (non-paginated).
     */
    public List<Review> getAllReviewsByBookId(String bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    /**
     * Add a new review.
     * 
     * @param bookId the book ID
     * @param userId the user ID
     * @param rating the star rating (1-5)
     * @param comment the review comment
     * @return the created review
     * @throws IllegalStateException if user cannot review or has already reviewed
     */
    public Review addReview(String bookId, String userId, int rating, String comment) {
        return addReview(bookId, userId, rating, comment, null);
    }

    /**
     * Add a new review with optional images.
     * Users can submit multiple reviews for the same product (no limit).
     * 
     * @param bookId the book ID
     * @param userId the user ID
     * @param rating the star rating (1-5)
     * @param comment the review comment
     * @param imageUrls list of image URLs (max 3)
     * @return the created review
     * @throws IllegalStateException if user has not purchased the product
     */
    public Review addReview(String bookId, String userId, int rating, String comment, List<String> imageUrls) {
        // Validate user can review (must have purchased the product)
        if (!canUserReview(userId, bookId)) {
            throw new IllegalStateException("User has not purchased this product or order not delivered");
        }

        // No limit on number of reviews per user per product

        // Validate max 3 images (defense in depth)
        if (imageUrls != null && imageUrls.size() > 3) {
            throw new IllegalArgumentException("Chỉ được tải tối đa 3 ảnh");
        }

        // Moderate comment text using OpenAI
        if (comment != null && !comment.isBlank()) {
            try {
                ModerationResultVm moderationResult = moderationService.moderateText(comment);
                if (moderationResult.flagged()) {
                    throw new IllegalArgumentException("Nội dung đánh giá không phù hợp");
                }
            } catch (IllegalStateException e) {
                // Fail closed: if moderation service is unavailable, reject the content
                throw new IllegalStateException("Không thể xác minh nội dung. Vui lòng thử lại sau.");
            }
        }

        // Get username for denormalization
        String username = userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("Anonymous");

        // Sanitize comment (basic XSS protection)
        String sanitizedComment = sanitizeHtml(comment);

        // Create and save review
        Review review = new Review(bookId, userId, rating, sanitizedComment, username);
        review.setImageUrls(imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>());
        Review savedReview = reviewRepository.save(review);

        logger.info("Created review for book " + bookId + " by user " + userId + " with rating " + rating + 
                   " and " + (imageUrls != null ? imageUrls.size() : 0) + " images");
        return savedReview;
    }

    /**
     * Check if a user can review a book.
     * User can review if they have a DELIVERED order containing the book.
     */
    public boolean canUserReview(String userId, String bookId) {
        if (userId == null || bookId == null) {
            return false;
        }
        return invoiceRepository.existsByUserIdAndDeliveredOrderContainingBook(userId, bookId);
    }

    /**
     * Check if a user has already reviewed a book.
     */
    public boolean hasUserReviewed(String userId, String bookId) {
        if (userId == null || bookId == null) {
            return false;
        }
        return reviewRepository.existsByBookIdAndUserId(bookId, userId);
    }

    /**
     * Get the user's existing review for a book, if any.
     */
    public Optional<Review> getUserReview(String userId, String bookId) {
        return reviewRepository.findByBookIdAndUserId(bookId, userId);
    }

    /**
     * Get review statistics for a book.
     */
    public ReviewStatistics getReviewStatistics(String bookId) {
        long totalCount = reviewRepository.countByBookId(bookId);
        
        if (totalCount == 0) {
            Map<Integer, Long> emptyCounts = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                emptyCounts.put(i, 0L);
            }
            return new ReviewStatistics(0.0, 0, emptyCounts);
        }

        // Calculate count by star
        Map<Integer, Long> countByStar = new HashMap<>();
        long totalRatingSum = 0;
        for (int star = 1; star <= 5; star++) {
            long count = reviewRepository.countByBookIdAndRating(bookId, star);
            countByStar.put(star, count);
            totalRatingSum += count * star;
        }

        // Calculate average
        double averageRating = (double) totalRatingSum / totalCount;
        averageRating = Math.round(averageRating * 10.0) / 10.0; // Round to 1 decimal place

        return new ReviewStatistics(averageRating, totalCount, countByStar);
    }

    /**
     * Basic HTML sanitization to prevent XSS.
     * In production, use a proper library like OWASP Java HTML Sanitizer.
     */
    private String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Inner class for review statistics.
     */
    public static class ReviewStatistics {
        private final double averageRating;
        private final long totalCount;
        private final Map<Integer, Long> countByStar;

        public ReviewStatistics(double averageRating, long totalCount, Map<Integer, Long> countByStar) {
            this.averageRating = averageRating;
            this.totalCount = totalCount;
            this.countByStar = countByStar;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public Map<Integer, Long> getCountByStar() {
            return countByStar;
        }
    }
}
