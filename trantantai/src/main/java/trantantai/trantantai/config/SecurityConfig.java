package trantantai.trantantai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import trantantai.trantantai.services.OAuthService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomLogoutHandler customLogoutHandler;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;

    @Autowired
    public SecurityConfig(CustomLogoutHandler customLogoutHandler, 
                         CustomAuthSuccessHandler customAuthSuccessHandler) {
        this.customLogoutHandler = customLogoutHandler;
        this.customAuthSuccessHandler = customAuthSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                           UserDetailsService userDetailsService,
                                           OAuthService oAuthService) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/momo/**")
                .ignoringRequestMatchers("/api/v1/images/**")
                .ignoringRequestMatchers("/admin/api/**")
                .ignoringRequestMatchers("/api/wishlist/**")
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/error", "/errors/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/api/momo/**").permitAll()
                // Ignore Chrome DevTools and other well-known paths
                .requestMatchers("/.well-known/**").permitAll()

                // ADMIN only - Admin Panel
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // ADMIN only - Swagger UI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/v3/api-docs/**", "/v3/api-docs.yaml", "/webjars/**").hasRole("ADMIN")

                // All authenticated users - cart operations (MUST come BEFORE /books/add/** to avoid conflict!)
                .requestMatchers("/cart/**").authenticated()
                .requestMatchers("/books/add-to-cart").authenticated()

                // All authenticated users - order history
                .requestMatchers("/orders/**").authenticated()

                // ADMIN only - book management
                .requestMatchers("/books/add", "/books/add/**").hasRole("ADMIN")
                .requestMatchers("/books/edit/**").hasRole("ADMIN")
                .requestMatchers("/books/delete/**").hasRole("ADMIN")

                // ADMIN only - category management
                .requestMatchers("/categories/**").hasRole("ADMIN")

                // Both roles - viewing books and API
                .requestMatchers("/books", "/books/search").authenticated()
                .requestMatchers("/api/**").authenticated()

                // Default - require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(customAuthSuccessHandler)
                .failureUrl("/login?error=true")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuthService)
                )
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/errors/403")
            )
            .rememberMe(remember -> remember
                .key("uniqueAndSecretKey")
                .tokenValiditySeconds(86400)
                .userDetailsService(userDetailsService)
            );

        return http.build();
    }
}
