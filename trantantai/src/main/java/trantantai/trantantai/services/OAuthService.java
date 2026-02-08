package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import trantantai.trantantai.constants.Provider;
import trantantai.trantantai.entities.User;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class OAuthService extends DefaultOAuth2UserService {

    private static final Logger logger = Logger.getLogger(OAuthService.class.getName());

    private final UserService userService;

    @Autowired
    public OAuthService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String username = extractUsernameFromEmail(email, name);

        User dbUser = userService.saveOauthUser(email, username, Provider.GOOGLE);

        Set<GrantedAuthority> authorities = new HashSet<>();
        if (dbUser.getRoles() != null && !dbUser.getRoles().isEmpty()) {
            dbUser.getRoles().forEach(role -> {
                String authority = "ROLE_" + role.getName();
                authorities.add(new SimpleGrantedAuthority(authority));
                logger.info("OAuth user " + email + " granted authority: " + authority);
            });
        } else {
            // Fallback: if no roles in DB, grant default USER role
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            logger.warning("OAuth user " + email + " has no roles in DB, granting default ROLE_USER");
        }

        logger.info("OAuth user " + email + " authenticated with authorities: " + authorities);

        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
    }
    
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
