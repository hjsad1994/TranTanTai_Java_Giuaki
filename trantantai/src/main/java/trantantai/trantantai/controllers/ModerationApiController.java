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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import trantantai.trantantai.services.OpenAIModerationService;
import trantantai.trantantai.viewmodels.ModerationResultVm;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST API Controller for content moderation.
 * Provides endpoints for ADMIN to test text and image moderation.
 * Uses OpenAI Moderation API to detect harmful content.
 */
@Tag(name = "Content Moderation", description = "Content moderation APIs for ADMIN - Test text and image moderation using OpenAI")
@RestController
@RequestMapping("/admin/api/moderation")
@CrossOrigin(origins = "*")
public class ModerationApiController {

    private static final Logger logger = Logger.getLogger(ModerationApiController.class.getName());
    private static final long MAX_IMAGE_SIZE = 20 * 1024 * 1024; // 20MB

    private final OpenAIModerationService moderationService;

    @Autowired
    public ModerationApiController(OpenAIModerationService moderationService) {
        this.moderationService = moderationService;
    }

    /**
     * Moderate text content.
     */
    @Operation(
        summary = "Moderate text content",
        description = "Checks text content for harmful categories using OpenAI Moderation API. " +
                      "Categories include: hate, harassment, self-harm, sexual, violence, etc."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Moderation completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ModerationResultVm.class),
                examples = {
                    @ExampleObject(
                        name = "Safe content",
                        value = "{\"flagged\": false, \"categories\": {\"hate\": false, \"violence\": false}, \"categoryScores\": {\"hate\": 0.001, \"violence\": 0.002}}"
                    ),
                    @ExampleObject(
                        name = "Flagged content",
                        value = "{\"flagged\": true, \"categories\": {\"hate\": true, \"violence\": false}, \"categoryScores\": {\"hate\": 0.95, \"violence\": 0.01}}"
                    )
                }
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input - text is empty"),
        @ApiResponse(responseCode = "503", description = "Moderation service unavailable")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Text content to moderate",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Normal review",
                    summary = "Safe review text",
                    value = "{\"text\": \"Sách rất hay, nội dung dễ hiểu và bổ ích!\"}"
                ),
                @ExampleObject(
                    name = "Test harmful content",
                    summary = "Test with potentially harmful text",
                    value = "{\"text\": \"I hate this product\"}"
                )
            }
        )
    )
    @PostMapping("/text")
    public ResponseEntity<?> moderateText(@RequestBody TextModerationRequest request) {
        if (request.text() == null || request.text().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Text content is required"));
        }

        try {
            ModerationResultVm result = moderationService.moderateText(request.text());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            logger.log(Level.WARNING, "Moderation service unavailable: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Moderation service unavailable", "message", e.getMessage()));
        }
    }

    /**
     * Moderate image content.
     */
    @Operation(
        summary = "Moderate image content",
        description = "Checks image content for harmful categories using OpenAI Moderation API. " +
                      "Supports JPEG, PNG, GIF, WEBP formats. Max size: 20MB."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Moderation completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ModerationResultVm.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input - no image or unsupported format"),
        @ApiResponse(responseCode = "413", description = "Image too large (max 20MB)"),
        @ApiResponse(responseCode = "503", description = "Moderation service unavailable")
    })
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> moderateImage(
            @Parameter(
                description = "Image file to moderate (JPEG, PNG, GIF, WEBP, max 20MB)",
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Image file is required"));
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "Image too large", "maxSize", "20MB"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Unsupported image format", "supported", "JPEG, PNG, GIF, WEBP"));
        }

        try {
            byte[] imageBytes = file.getBytes();
            ModerationResultVm result = moderationService.moderateImage(imageBytes, contentType);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read image file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to read image file"));
        } catch (IllegalStateException e) {
            logger.log(Level.WARNING, "Moderation service unavailable: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Moderation service unavailable", "message", e.getMessage()));
        }
    }

    /**
     * Get moderation categories info.
     */
    @Operation(
        summary = "Get moderation categories",
        description = "Returns information about all moderation categories and their descriptions"
    )
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getModerationCategories() {
        Map<String, String> categories = Map.ofEntries(
            Map.entry("hate", "Content that expresses, incites, or promotes hate based on race, gender, ethnicity, religion, nationality, sexual orientation, disability status, or caste"),
            Map.entry("hate/threatening", "Hateful content that also includes violence or serious harm towards the targeted group"),
            Map.entry("harassment", "Content that expresses, incites, or promotes harassing language towards any target"),
            Map.entry("harassment/threatening", "Harassment content that also includes violence or serious harm towards any target"),
            Map.entry("self-harm", "Content that promotes, encourages, or depicts acts of self-harm"),
            Map.entry("self-harm/intent", "Content where the speaker expresses that they are engaging or intend to engage in acts of self-harm"),
            Map.entry("self-harm/instructions", "Content that encourages performing acts of self-harm or gives instructions on how to commit such acts"),
            Map.entry("sexual", "Content meant to arouse sexual excitement or promote sexual services"),
            Map.entry("sexual/minors", "Sexual content that includes an individual who is under 18 years old"),
            Map.entry("violence", "Content that depicts death, violence, or physical injury"),
            Map.entry("violence/graphic", "Content that depicts death, violence, or physical injury in graphic detail")
        );

        return ResponseEntity.ok(Map.of(
            "categories", categories,
            "totalCategories", categories.size(),
            "note", "OpenAI Moderation API evaluates content against these categories. A content is flagged if it violates any category."
        ));
    }

    /**
     * Check if the content type is a valid image type.
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    /**
     * Request body for text moderation.
     */
    @Schema(description = "Text moderation request")
    public record TextModerationRequest(
        @Schema(
            description = "Text content to moderate",
            example = "This is a sample review text to check for harmful content",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        String text
    ) {}
}
