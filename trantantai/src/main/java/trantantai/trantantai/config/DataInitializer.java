package trantantai.trantantai.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import trantantai.trantantai.constants.Role;
import trantantai.trantantai.entities.RoleEntity;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.repositories.IRoleRepository;
import trantantai.trantantai.repositories.IUserRepository;

import java.util.Set;

@Component
@Order(1) // Run first to ensure roles exist before UserRoleMigration
public class DataInitializer implements CommandLineRunner {

    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(IRoleRepository roleRepository, 
                          IUserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Initialize roles
        RoleEntity adminRole = roleRepository.findByName(Role.ADMIN.name())
            .orElseGet(() -> roleRepository.save(
                new RoleEntity(Role.ADMIN.name(), "Administrator")));
        
        RoleEntity userRole = roleRepository.findByName(Role.USER.name())
            .orElseGet(() -> roleRepository.save(
                new RoleEntity(Role.USER.name(), "Regular User")));

        // Initialize admin user
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@hutech.edu.vn");
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            System.out.println(">>> Created admin user: admin/admin123");
        }
    }
}
