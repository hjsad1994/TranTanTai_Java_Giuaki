package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import trantantai.trantantai.constants.Provider;

/**
 * Custom OAuth2 User Service for processing Google OAuth2 login.
 * Extends DefaultOAuth2UserService to add custom user persistence logic.
 */
@Service
public class OAuthService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Autowired
    public OAuthService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Load user from OAuth2 provider and persist to database.
     * This method is called after successful OAuth2 authentication.
     * 
     * @param userRequest The OAuth2 user request containing client registration info
     * @return The OAuth2User object for Spring Security
     * @throws OAuth2AuthenticationException if authentication fails
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to parent to load user from OAuth2 provider (Google)
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Extract user information from Google attributes
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        // Generate username from email prefix
        // Example: john.doe@gmail.com -> john.doe
        String username = extractUsernameFromEmail(email, name);

        // Save or update user in database
        // This handles both new users and account linking
        userService.saveOauthUser(email, username, Provider.GOOGLE);

        // Return the OAuth2User for Spring Security
        // Spring Security will use this for authentication context
        return oauth2User;
    }
    
    /**
     * Extract username from email address.
     * Falls back to name or generates default if email is null.
     */
    private String extractUsernameFromEmail(String email, String name) {
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        if (name != null && !name.isBlank()) {
            return name.toLowerCase().replace(" ", ".");
        }
        return "googleuser";
    }
}
