package trantantai.trantantai.viewmodels;

import trantantai.trantantai.entities.Category;
import jakarta.validation.constraints.NotNull;

public record CategoryPostVm(
    String name
) {
    public static CategoryPostVm from(@NotNull Category category) {
        return new CategoryPostVm(
            category.getName()
        );
    }
}
