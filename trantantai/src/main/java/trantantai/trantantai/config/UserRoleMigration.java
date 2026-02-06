package trantantai.trantantai.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import trantantai.trantantai.constants.Role;
import trantantai.trantantai.entities.RoleEntity;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.repositories.IRoleRepository;
import trantantai.trantantai.repositories.IUserRepository;

import java.util.List;
import java.util.Set;

@Component
@Order(2) // Run AFTER DataInitializer (which is Order 1)
public class UserRoleMigration implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;

    public UserRoleMigration(IUserRepository userRepository, IRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        // Get USER role (should exist from DataInitializer)
        RoleEntity userRole = roleRepository.findByName(Role.USER.name())
                .orElseThrow(() -> new RuntimeException("USER role not found! DataInitializer must run first."));

        // Find all users
        List<User> allUsers = userRepository.findAll();
        
        int migratedCount = 0;
        for (User user : allUsers) {
            // Skip admin user (already has ADMIN role)
            if ("admin".equals(user.getUsername())) {
                continue;
            }
            
            // If user has no roles, assign USER role
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.setRoles(Set.of(userRole));
                userRepository.save(user);
                migratedCount++;
                System.out.println(">>> Migrated user: " + user.getUsername() + " → added USER role");
            }
        }
        
        if (migratedCount > 0) {
            System.out.println(">>> Migration complete: Added USER role to " + migratedCount + " existing users");
        }
    }
}
