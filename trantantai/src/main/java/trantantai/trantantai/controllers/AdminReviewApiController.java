package trantantai.trantantai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.entities.Review;
import trantantai.trantantai.repositories.IReviewRepository;
import trantantai.trantantai.viewmodels.ReviewGetVm;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Admin Review Management", description = "Admin APIs for managing reviews - List, view details, delete reviews")
@RestController
@RequestMapping("/admin/api/reviews")
@CrossOrigin(origins = "*")
public class AdminReviewApiController {

    private final IReviewRepository reviewRepository;

    @Autowired
    public AdminReviewApiController(IReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Operation(
        summary = "Get all reviews (paginated)",
        description = "Retrieves a paginated list of all reviews across all books. " +
                      "Sorted by creation date descending (newest first) by default."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews")
    @GetMapping
    public ResponseEntity<Page<ReviewGetVm>> getAllReviews(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviews = reviewRepository.findAll(pageable);
        Page<ReviewGetVm> reviewVms = reviews.map(ReviewGetVm::from);
        return ResponseEntity.ok(reviewVms);
    }

    @Operation(
        summary = "Get reviews by book ID",
        description = "Retrieves all reviews for a specific book"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews")
    })
    @GetMapping("/book/{bookId}")
    public ResponseEntity<Page<ReviewGetVm>> getReviewsByBook(
            @Parameter(description = "Book ID", example = "507f1f77bcf86cd799439011", required = true)
            @PathVariable String bookId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, pageable);
        Page<ReviewGetVm> reviewVms = reviews.map(ReviewGetVm::from);
        return ResponseEntity.ok(reviewVms);
    }

    @Operation(
        summary = "Get review by ID",
        description = "Retrieves a specific review by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review found"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewGetVm> getReviewById(
            @Parameter(description = "Review ID", example = "507f1f77bcf86cd799439099", required = true)
            @PathVariable String reviewId) {

        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        return reviewOpt
                .map(review -> ResponseEntity.ok(ReviewGetVm.from(review)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Delete a review",
        description = "Permanently deletes a review by its ID. This action cannot be undone."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Review deleted successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"message\": \"Review deleted successfully\", \"deletedReviewId\": \"507f1f77bcf86cd799439099\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @Parameter(description = "Review ID", example = "507f1f77bcf86cd799439099", required = true)
            @PathVariable String reviewId) {

        if (!reviewRepository.existsById(reviewId)) {
            return ResponseEntity.notFound().build();
        }

        reviewRepository.deleteById(reviewId);
        return ResponseEntity.ok(Map.of(
            "message", "Review deleted successfully",
            "deletedReviewId", reviewId
        ));
    }

    @Operation(
        summary = "Delete all reviews for a book",
        description = "Permanently deletes ALL reviews for a specific book. Use with caution!"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Reviews deleted successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"message\": \"All reviews for book deleted\", \"bookId\": \"507f1f77bcf86cd799439011\", \"deletedCount\": 5}"
                )
            )
        )
    })
    @DeleteMapping("/book/{bookId}")
    public ResponseEntity<Map<String, Object>> deleteReviewsByBook(
            @Parameter(description = "Book ID", example = "507f1f77bcf86cd799439011", required = true)
            @PathVariable String bookId) {

        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
        long count = reviews.size();
        reviewRepository.deleteAll(reviews);

        return ResponseEntity.ok(Map.of(
            "message", "All reviews for book deleted",
            "bookId", bookId,
            "deletedCount", count
        ));
    }

    @Operation(
        summary = "Get review statistics",
        description = "Retrieves overview statistics for all reviews in the system"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Statistics retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = "{\"totalReviews\": 150, \"averageRating\": 4.2, \"ratingDistribution\": {\"1\": 5, \"2\": 10, \"3\": 25, \"4\": 50, \"5\": 60}}"
            )
        )
    )
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReviewStatistics() {
        List<Review> allReviews = reviewRepository.findAll();
        long totalReviews = allReviews.size();

        if (totalReviews == 0) {
            return ResponseEntity.ok(Map.of(
                "totalReviews", 0,
                "averageRating", 0.0,
                "ratingDistribution", Map.of("1", 0, "2", 0, "3", 0, "4", 0, "5", 0)
            ));
        }

        double avgRating = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        avgRating = Math.round(avgRating * 10.0) / 10.0;

        Map<String, Long> ratingDistribution = Map.of(
            "1", allReviews.stream().filter(r -> r.getRating() == 1).count(),
            "2", allReviews.stream().filter(r -> r.getRating() == 2).count(),
            "3", allReviews.stream().filter(r -> r.getRating() == 3).count(),
            "4", allReviews.stream().filter(r -> r.getRating() == 4).count(),
            "5", allReviews.stream().filter(r -> r.getRating() == 5).count()
        );

        return ResponseEntity.ok(Map.of(
            "totalReviews", totalReviews,
            "averageRating", avgRating,
            "ratingDistribution", ratingDistribution
        ));
    }
}
