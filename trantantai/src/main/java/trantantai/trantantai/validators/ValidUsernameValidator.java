package trantantai.trantantai.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import trantantai.trantantai.repositories.IUserRepository;

/**
 * Validator for @ValidUsername annotation.
 * Checks if the username already exists in the database.
 */
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    @Autowired
    private IUserRepository userRepository;

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        // Let @NotBlank handle null/empty validation
        if (username == null || username.trim().isEmpty()) {
            return true;
        }
        
        // Check if username already exists in database
        return !userRepository.existsByUsername(username);
    }
}
