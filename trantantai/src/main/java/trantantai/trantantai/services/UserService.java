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

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

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
            roleRepository.findByName(Role.USER.name())
                    .ifPresent(role -> user.setRoles(Set.of(role)));
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
     * @return Saved or existing user
     */
    public User saveOauthUser(String email, String username, Provider provider) {
        // Check if user already exists with this email
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            // Link existing account - return existing user (they can use both methods)
            return existingUser.get();
        }
        
        // Create new OAuth user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(generateUniqueUsername(username));
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Placeholder password for OAuth users
        newUser.setProvider(provider);
        setDefaultRole(newUser);
        return userRepository.save(newUser);
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
