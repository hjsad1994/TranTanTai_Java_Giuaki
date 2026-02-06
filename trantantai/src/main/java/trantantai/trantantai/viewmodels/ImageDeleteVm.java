package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Image deletion request model.
 */
@Schema(description = "Image deletion request model")
public record ImageDeleteVm(
    @Schema(description = "Cloudinary public ID to delete", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Public ID is required")
    String publicId
) {}
