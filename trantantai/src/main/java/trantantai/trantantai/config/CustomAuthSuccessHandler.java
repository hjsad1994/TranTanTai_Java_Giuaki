package trantantai.trantantai.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.services.CartService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom authentication success handler to restore cart from database after login.
 */
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = Logger.getLogger(CustomAuthSuccessHandler.class.getName());

    private final CartService cartService;
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    @Autowired
    public CustomAuthSuccessHandler(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            String userId = extractUserId(authentication);
            if (userId != null) {
                cartService.restoreCartFromDatabase(request.getSession(), userId);
                logger.info("Cart restored for user after login: " + userId);
            }
        } catch (Exception e) {
            // Log but don't throw - don't block login on cart restore failure
            logger.log(Level.WARNING, "Failed to restore cart after login", e);
        }

        // Redirect to saved request or default URL
        String targetUrl = determineTargetUrl(request, response);
        response.sendRedirect(targetUrl);
    }

    private String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest != null) {
            String redirectUrl = savedRequest.getRedirectUrl();
            // Skip unwanted URLs (Chrome DevTools, favicon, etc.)
            if (redirectUrl != null && !isUnwantedUrl(redirectUrl)) {
                return redirectUrl;
            }
        }

        return "/"; // Default redirect
    }

    /**
     * Check if URL should be ignored for redirect after login.
     */
    private boolean isUnwantedUrl(String url) {
        if (url == null) return true;
        String lowerUrl = url.toLowerCase();
        return lowerUrl.contains("/.well-known/")
            || lowerUrl.contains("/favicon")
            || lowerUrl.contains("/error")
            || lowerUrl.endsWith(".json")
            || lowerUrl.endsWith(".ico")
            || lowerUrl.endsWith(".png")
            || lowerUrl.endsWith(".jpg")
            || lowerUrl.endsWith(".css")
            || lowerUrl.endsWith(".js");
    }

    private String extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return ((User) principal).getId();
        }
        
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            return ((org.springframework.security.core.userdetails.User) principal).getUsername();
        }
        
        logger.warning("Could not extract user ID from principal: " + principal.getClass().getName());
        return null;
    }
}
