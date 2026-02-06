package trantantai.trantantai.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.services.CartService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom logout handler to save cart to database before session invalidation.
 */
@Component
public class CustomLogoutHandler implements LogoutHandler {

    private static final Logger logger = Logger.getLogger(CustomLogoutHandler.class.getName());

    private final CartService cartService;

    @Autowired
    public CustomLogoutHandler(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication == null) {
            logger.info("No authentication found during logout - skipping cart save");
            return;
        }

        try {
            String userId = extractUserId(authentication);
            if (userId != null) {
                cartService.saveCartToDatabase(request.getSession(false), userId);
                logger.info("Cart saved for user during logout: " + userId);
            }
        } catch (Exception e) {
            // Log but don't throw - don't block logout on cart save failure
            logger.log(Level.WARNING, "Failed to save cart during logout", e);
        }
    }

    private String extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return ((User) principal).getId();
        }
        
        if (principal instanceof OAuth2User) {
            // For OAuth2 users, try to get user ID from attributes
            OAuth2User oauth2User = (OAuth2User) principal;
            Object email = oauth2User.getAttribute("email");
            if (email != null) {
                return email.toString(); // Use email as identifier for OAuth users
            }
        }
        
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            return ((org.springframework.security.core.userdetails.User) principal).getUsername();
        }
        
        logger.warning("Could not extract user ID from principal: " + principal.getClass().getName());
        return null;
    }
}
