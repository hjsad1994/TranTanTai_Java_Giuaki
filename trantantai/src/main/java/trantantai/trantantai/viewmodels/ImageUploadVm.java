package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Image upload response model.
 * Returned after successful image upload to Cloudinary.
 */
@Schema(description = "Image upload response model")
public record ImageUploadVm(
    @Schema(description = "Cloudinary public ID (used for deletion)")
    String publicId,
    
    @Schema(description = "Full URL to the uploaded image (CDN)")
    String url,
    
    @Schema(description = "Original filename")
    String originalFilename,
    
    @Schema(description = "File size in bytes")
    long size
) {}
