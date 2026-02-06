package trantantai.trantantai.viewmodels;

import trantantai.trantantai.entities.Category;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category response model")
public record CategoryGetVm(
    @Schema(description = "Category unique identifier", example = "507f1f77bcf86cd799439012")
    String id,
    @Schema(description = "Category name", example = "Programming")
    String name
) {
    public static CategoryGetVm from(@NotNull Category category) {
        return new CategoryGetVm(
            category.getId(),
            category.getName()
        );
    }
}
