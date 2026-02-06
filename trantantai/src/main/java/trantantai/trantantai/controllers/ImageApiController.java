package trantantai.trantantai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.services.ImageUploadService;
import trantantai.trantantai.services.OpenAIModerationService;
import trantantai.trantantai.validators.ImageValidator;
import trantantai.trantantai.viewmodels.ImageUploadVm;
import trantantai.trantantai.viewmodels.ModerationResultVm;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST API Controller for image uploads.
 * Handles image upload to Cloudinary for books (ADMIN) and reviews (USER).
 */
@Tag(name = "Images", description = "Image upload APIs - Upload images for books and reviews")
@RestController
@RequestMapping("/api/v1/images")
public class ImageApiController {

    private static final Logger logger = Logger.getLogger(ImageApiController.class.getName());

    private final ImageUploadService imageUploadService;
    private final ImageValidator imageValidator;
    private final OpenAIModerationService moderationService;

    @Autowired
    public ImageApiController(ImageUploadService imageUploadService, 
                              ImageValidator imageValidator,
                              OpenAIModerationService moderationService) {
        this.imageUploadService = imageUploadService;
        this.imageValidator = imageValidator;
        this.moderationService = moderationService;
    }

    /**
     * Upload an image for a book (ADMIN only).
     * Returns the Cloudinary URL to be added to the book's imageUrls.
     */
    @Operation(summary = "Upload book image", 
               description = "Uploads an image for a book. Requires ADMIN role. Returns the image URL to add to book.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file (wrong type or too large)"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User is not ADMIN"),
        @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/books", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBookImage(
            @Parameter(description = "Image file (JPEG, PNG, GIF, WebP, max 5MB)", required = true)
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Vui lòng đăng nhập"));
        }

        try {
            // Validate image
            imageValidator.validateImage(file);

            // Upload to Cloudinary
            Map<String, Object> result = imageUploadService.uploadImage(file, "books");

            // Build response
            ImageUploadVm response = new ImageUploadVm(
                    imageUploadService.getPublicId(result),
                    imageUploadService.getSecureUrl(result),
                    file.getOriginalFilename(),
                    imageUploadService.getFileSize(result)
            );

            logger.info("Book image uploaded by admin: " + user.getUsername() +
                       ", publicId: " + response.publicId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to upload book image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể tải ảnh lên. Vui lòng thử lại."));
        }
    }

    /**
     * Upload an image for a review (USER only).
     * Returns the Cloudinary URL to be included when creating a review.
     */
    @Operation(summary = "Upload review image", 
               description = "Uploads an image for a review. Requires USER role. Returns the image URL to include in review.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file (wrong type or too large)"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User is not USER role"),
        @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadReviewImage(
            @Parameter(description = "Image file (JPEG, PNG, GIF, WebP, max 5MB)", required = true)
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Vui lòng đăng nhập"));
        }

        try {
            // Validate image
            imageValidator.validateImage(file);

            // Moderate image content using OpenAI BEFORE uploading to Cloudinary
            try {
                byte[] imageBytes = file.getBytes();
                String mimeType = file.getContentType();
                ModerationResultVm moderationResult = moderationService.moderateImage(imageBytes, mimeType);
                if (moderationResult.flagged()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Hình ảnh chứa nội dung không phù hợp"));
                }
            } catch (IllegalStateException e) {
                // Fail closed: if moderation service is unavailable, reject the upload
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Không thể xác minh hình ảnh. Vui lòng thử lại sau."));
            }

            // Upload to Cloudinary with user-specific folder
            Map<String, Object> result = imageUploadService.uploadImage(file, "reviews");

            // Build response
            ImageUploadVm response = new ImageUploadVm(
                    imageUploadService.getPublicId(result),
                    imageUploadService.getSecureUrl(result),
                    file.getOriginalFilename(),
                    imageUploadService.getFileSize(result)
            );

            logger.info("Review image uploaded by user: " + user.getUsername() +
                       ", publicId: " + response.publicId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to upload review image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể tải ảnh lên. Vui lòng thử lại."));
        }
    }

    /**
     * Delete an image from Cloudinary.
     * Only ADMIN can delete book images, users can potentially delete their review images.
     */
    @Operation(summary = "Delete image", 
               description = "Deletes an image from Cloudinary by public ID. Requires authentication.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid public ID"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "500", description = "Deletion failed")
    })
    @DeleteMapping("/{publicId}")
    public ResponseEntity<?> deleteImage(
            @Parameter(description = "Cloudinary public ID of the image", required = true)
            @PathVariable String publicId,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Vui lòng đăng nhập"));
        }

        if (publicId == null || publicId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Thiếu mã hình ảnh"));
        }

        try {
            boolean deleted = imageUploadService.deleteImage(publicId);

            if (deleted) {
                logger.info("Image deleted by user: " + user.getUsername() +
                           ", publicId: " + publicId);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy hình ảnh hoặc đã bị xóa"));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to delete image: " + publicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể xóa hình ảnh. Vui lòng thử lại."));
        }
    }
}
