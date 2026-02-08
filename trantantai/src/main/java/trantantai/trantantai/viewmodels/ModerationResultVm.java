package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Moderation result response model from OpenAI API.
 * Contains information about content moderation flags and confidence scores.
 */
@Schema(description = "Content moderation result from OpenAI API")
public record ModerationResultVm(
    @Schema(description = "Whether the content was flagged as harmful", example = "false")
    boolean flagged,
    
    @Schema(
        description = "Map of category names to boolean flags indicating which categories triggered",
        example = "{\"hate\": false, \"violence\": false, \"sexual\": false, \"harassment\": false}"
    )
    Map<String, Boolean> categories,
    
    @Schema(
        description = "Map of category names to confidence scores (0.0 to 1.0)",
        example = "{\"hate\": 0.001, \"violence\": 0.002, \"sexual\": 0.0001, \"harassment\": 0.003}"
    )
    Map<String, Double> categoryScores
) {
    /**
     * Create a ModerationResultVm from OpenAI API response.
     * 
     * @param apiResponse The raw API response map from OpenAI moderation endpoint
     * @return ModerationResultVm instance parsed from the response
     */
    @SuppressWarnings("unchecked")
    public static ModerationResultVm from(Map<String, Object> apiResponse) {
        // Extract the first result from the results array
        var results = (java.util.List<Map<String, Object>>) apiResponse.get("results");
        var firstResult = results.get(0);
        
        // Extract fields from the first result
        boolean flagged = (Boolean) firstResult.get("flagged");
        Map<String, Boolean> categories = (Map<String, Boolean>) firstResult.get("categories");
        Map<String, Double> categoryScores = (Map<String, Double>) firstResult.get("category_scores");
        
        return new ModerationResultVm(flagged, categories, categoryScores);
    }
}
