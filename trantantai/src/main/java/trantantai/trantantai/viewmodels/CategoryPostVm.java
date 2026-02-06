package trantantai.trantantai.viewmodels;

import trantantai.trantantai.entities.Category;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category creation/update request model")
public record CategoryPostVm(
    @Schema(description = "Category name", example = "Programming", requiredMode = Schema.RequiredMode.REQUIRED)
    String name
) {
    public static CategoryPostVm from(@NotNull Category category) {
        return new CategoryPostVm(
            category.getName()
        );
    }
}
