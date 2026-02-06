package trantantai.trantantai.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import trantantai.trantantai.repositories.ICategoryRepository;

/**
 * Validator for @ValidCategoryId annotation.
 * Checks if the categoryId exists in the database.
 * Category is REQUIRED - null/empty values will fail validation.
 */
public class ValidCategoryIdValidator implements ConstraintValidator<ValidCategoryId, String> {

    @Autowired
    private ICategoryRepository categoryRepository;

    @Override
    public void initialize(ValidCategoryId constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String categoryId, ConstraintValidatorContext context) {
        // Category is required - null or empty is invalid
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return false;
        }
        
        // Check if category exists in database
        return categoryRepository.existsById(categoryId);
    }
}
