package trantantai.trantantai.viewmodels;

import trantantai.trantantai.entities.Category;
import jakarta.validation.constraints.NotNull;

public record CategoryGetVm(
    String id,
    String name
) {
    public static CategoryGetVm from(@NotNull Category category) {
        return new CategoryGetVm(
            category.getId(),
            category.getName()
        );
    }
}
