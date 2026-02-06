package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Review statistics for a book.
 */
@Schema(description = "Review statistics for a book")
public record ReviewStatisticsVm(
    @Schema(description = "Average rating (0-5)", example = "4.2")
    double averageRating,
    
    @Schema(description = "Total number of reviews", example = "42")
    long totalCount,
    
    @Schema(description = "Count of reviews per star rating {5: 10, 4: 5, ...}")
    Map<Integer, Long> countByStar
) {}
