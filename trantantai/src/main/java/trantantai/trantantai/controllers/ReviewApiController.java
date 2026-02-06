package trantantai.trantantai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.entities.Review;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.services.ReviewService;
import trantantai.trantantai.viewmodels.CanReviewVm;
import trantantai.trantantai.viewmodels.ReviewGetVm;
import trantantai.trantantai.viewmodels.ReviewPostVm;
import trantantai.trantantai.viewmodels.ReviewStatisticsVm;

import java.util.Map;

/**
 * REST API Controller for product reviews.
 * Handles review CRUD operations and statistics.
 */
@Tag(name = "Reviews", description = "Product review APIs - Create and view product reviews with ratings")
@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Get reviews for a book (paginated).
     */
    @Operation(summary = "Get reviews for a book", description = "Retrieves paginated reviews for a specific book, sorted by newest first")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews")
    @GetMapping("/{bookId}")
    public ResponseEntity<Page<ReviewGetVm>> getReviews(
            @Parameter(description = "Book ID", required = true) 
            @PathVariable String bookId,
            @Parameter(description = "Page number (0-indexed)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Review> reviews = reviewService.getReviewsByBookId(bookId, page, size);
        Page<ReviewGetVm> reviewVms = reviews.map(ReviewGetVm::from);
        return ResponseEntity.ok(reviewVms);
    }

    /**
     * Get review statistics for a book.
     */
    @Operation(summary = "Get review statistics", description = "Retrieves average rating and rating breakdown for a book")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    @GetMapping("/{bookId}/statistics")
    public ResponseEntity<ReviewStatisticsVm> getStatistics(
            @Parameter(description = "Book ID", required = true) 
            @PathVariable String bookId) {
        
        ReviewService.ReviewStatistics stats = reviewService.getReviewStatistics(bookId);
        ReviewStatisticsVm statsVm = new ReviewStatisticsVm(
                stats.getAverageRating(),
                stats.getTotalCount(),
                stats.getCountByStar()
        );
        return ResponseEntity.ok(statsVm);
    }

    /**
     * Check if current user can review a book.
     * Requires authentication.
     */
    @Operation(summary = "Check if user can review", description = "Checks if the authenticated user can submit a review for a book (must have purchased)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully checked review eligibility"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/{bookId}/can-review")
    public ResponseEntity<CanReviewVm> canReview(
            @Parameter(description = "Book ID", required = true) 
            @PathVariable String bookId,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = user.getId();

        // Check if user can review (has purchased) - users can review multiple times
        if (reviewService.canUserReview(userId, bookId)) {
            return ResponseEntity.ok(CanReviewVm.forCanReview());
        }

        // User hasn't purchased
        return ResponseEntity.ok(CanReviewVm.forNotPurchased());
    }

    /**
     * Submit a new review.
     * Requires authentication and purchase verification.
     * Users can submit multiple reviews for the same product (no limit).
     */
    @Operation(summary = "Submit a review", description = "Submits a new review for a book. Requires user to have purchased and received the book. Users can submit multiple reviews.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Review created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User has not purchased this product")
    })
    @PostMapping
    public ResponseEntity<?> createReview(
            @Valid @RequestBody ReviewPostVm reviewPostVm,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Vui lòng đăng nhập để đánh giá"));
        }

        String userId = user.getId();
        String bookId = reviewPostVm.bookId();

        // Check if user can review (has purchased) - no limit on number of reviews
        if (!reviewService.canUserReview(userId, bookId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Bạn cần mua sản phẩm này để đánh giá"));
        }

        try {
            Review review = reviewService.addReview(
                    bookId,
                    userId,
                    reviewPostVm.rating(),
                    reviewPostVm.comment(),
                    reviewPostVm.imageUrls()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(ReviewGetVm.from(review));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
