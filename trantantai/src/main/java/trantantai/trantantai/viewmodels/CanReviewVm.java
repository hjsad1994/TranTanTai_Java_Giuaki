package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response model indicating whether a user can review a book.
 */
@Schema(description = "User's ability to review a book")
public record CanReviewVm(
    @Schema(description = "Whether user can submit a review")
    boolean canReview,
    
    @Schema(description = "Whether user has already reviewed this book")
    boolean hasReviewed,
    
    @Schema(description = "Reason if cannot review: 'not_purchased' or 'already_reviewed' or null if can review")
    String reason
) {
    /**
     * Create response for user who can review.
     */
    public static CanReviewVm forCanReview() {
        return new CanReviewVm(true, false, null);
    }

    /**
     * Create response for user who has already reviewed.
     */
    public static CanReviewVm forAlreadyReviewed() {
        return new CanReviewVm(false, true, "already_reviewed");
    }

    /**
     * Create response for user who hasn't purchased.
     */
    public static CanReviewVm forNotPurchased() {
        return new CanReviewVm(false, false, "not_purchased");
    }
}
