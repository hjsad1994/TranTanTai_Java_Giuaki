package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.constants.Provider;
import trantantai.trantantai.repositories.IUserRepository;
import trantantai.trantantai.repositories.IRoleRepository;
import trantantai.trantantai.entities.RoleEntity;
import trantantai.trantantai.constants.Role;

import java.util.Set;
import java.util.logging.Logger;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IRoleRepository roleRepository;

    @Autowired
    public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder, IRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public void setDefaultRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Optional<RoleEntity> roleOpt = roleRepository.findByName(Role.USER.name());
            if (roleOpt.isPresent()) {
                user.setRoles(Set.of(roleOpt.get()));
                logger.info("Set default role USER for user: " + user.getEmail());
            } else {
                logger.severe("CRITICAL: Role 'USER' not found in database! " +
                        "Please ensure roles are initialized in the database.");
            }
        }
    }

    /**
     * Save a new user with encoded password.
     */
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        setDefaultRole(user);
        return userRepository.save(user);
    }

    /**
     * Find user by username.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Check if email already exists.
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Save or update OAuth user. Links existing accounts by email.
     * @param email User's email from OAuth provider
     * @param username Suggested username (from email prefix)
     * @param provider OAuth provider (GOOGLE)
     * @return Saved or existing user with roles properly loaded
     */
    public User saveOauthUser(String email, String username, Provider provider) {
        // Check if user already exists with this email
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            logger.info("Found existing OAuth user: " + email + ", roles: " +
                    (user.getRoles() != null ? user.getRoles().size() : "null"));

            // Always ensure user has USER role (for legacy users without roles)
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                logger.info("Setting default role for existing OAuth user: " + email);
                setDefaultRole(user);
                user = userRepository.save(user);
            }

            // Re-fetch to ensure DBRef roles are properly loaded
            User reloadedUser = userRepository.findById(user.getId()).orElse(user);
            logger.info("Reloaded OAuth user: " + email + ", roles: " +
                    (reloadedUser.getRoles() != null ? reloadedUser.getRoles().size() : "null"));

            if (reloadedUser.getRoles() != null) {
                reloadedUser.getRoles().forEach(role ->
                    logger.info("OAuth user " + email + " has role: " + role.getName())
                );
            }

            return reloadedUser;
        }

        // Create new OAuth user
        logger.info("Creating new OAuth user: " + email);
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(generateUniqueUsername(username));
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Placeholder password for OAuth users
        newUser.setProvider(provider);
        setDefaultRole(newUser);
        User savedUser = userRepository.save(newUser);

        // Re-fetch to ensure DBRef roles are properly loaded
        User reloadedUser = userRepository.findById(savedUser.getId()).orElse(savedUser);
        logger.info("Created new OAuth user: " + email + ", roles: " +
                (reloadedUser.getRoles() != null ? reloadedUser.getRoles().size() : "null"));

        return reloadedUser;
    }
    
    /**
     * Generate unique username by appending numbers if needed.
     * Example: john.doe -> john.doe1 -> john.doe2
     */
    private String generateUniqueUsername(String baseUsername) {
        // Clean up username (remove special chars except dots and underscores)
        String cleanUsername = baseUsername.replaceAll("[^a-zA-Z0-9._]", "");
        
        // Ensure minimum length
        if (cleanUsername.length() < 4) {
            cleanUsername = cleanUsername + "user";
        }
        
        String username = cleanUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = cleanUsername + counter;
            counter++;
        }
        
        return username;
    }

    /**
     * Load user by username for Spring Security authentication.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
