# Draft: User Registration/Login Functionality

## Requirements (confirmed from user request)

### 1. User Entity (trantantai.trantantai.entities.User)
- @Document(collection = "user")
- Implements UserDetails (Spring Security)
- Fields:
  - String id (@Id)
  - String username (@Indexed, unique, @NotBlank, @Size, @ValidUsername)
  - String password (@NotBlank)
  - String email (@Indexed, unique, @NotBlank, @Email)
  - String phone (@Pattern for digits, length 10)
- Implements all UserDetails methods
- Manual getters, setters, constructors, equals, hashCode, toString (NO Lombok)

### 2. ValidUsername Validator
- ValidUsername.java annotation in `validators` package
- ValidUsernameValidator.java - checks if username already exists in database

### 3. IUserRepository
- Location: `repositories` package
- extends MongoRepository<User, String>
- Optional<User> findByUsername(String username)
- Boolean existsByUsername(String username)

### 4. UserService
- Location: `services` package
- Implements UserDetailsService
- Constructor injection with @Autowired
- save(User user) - encodes password with BCryptPasswordEncoder
- findByUsername(String username) - returns Optional<User>
- loadUserByUsername(String username) - for Spring Security

### 5. UserController
- Location: `controllers` package
- @RequestMapping("/")
- GET /login - return login page
- GET /register - return register form with new User()
- POST /register - validate and save user, redirect to login

### 6. SecurityConfig
- Location: `config` package
- @Configuration @EnableWebSecurity @EnableMethodSecurity
- UserDetailsService bean
- PasswordEncoder bean (BCryptPasswordEncoder)
- DaoAuthenticationProvider bean
- SecurityFilterChain bean with:
  - permitAll: /css/**, /js/**, /, /register, /error
  - authenticated: /books/edit, /books/delete, /books, /books/add, /api/**
  - Form login with /login
  - Logout handling to /
  - Remember-me
  - Session management

### 7. Update Invoice Entity
- Add userId field (String)
- Add getter/setter
- Update constructors
- Update toString

### 8. Update CartService
- Inject SecurityContextHolder to get current user
- Set userId on Invoice during saveCart()

### 9. Templates
- user/login.html - login form
- user/register.html - registration form

### 10. Update layout.html
- Add security namespace: xmlns:sec="http://www.thymeleaf.org/extras/spring-security"
- Show Login/Register when anonymous
- Show username + Logout when authenticated

### 11. Dependencies (pom.xml)
- spring-boot-starter-security
- thymeleaf-extras-springsecurity6

---

## Technical Decisions (from codebase analysis)

### Entity Pattern (must follow Book.java exactly)
- @Document(collection = "user")
- @Id String id
- @Indexed for username and email (unique)
- Validation: @NotBlank, @Size, @Email, @Pattern
- Manual getters/setters (NO Lombok)
- equals() based on ID only
- hashCode() returns getClass().hashCode()
- toString() includes all fields

### Repository Pattern (must follow IBookRepository.java exactly)
- Interface extends MongoRepository<User, String>
- @Repository annotation
- Query methods: findByUsername(), existsByUsername()

### Service Pattern (must follow BookService.java exactly)
- @Service annotation
- Constructor injection with @Autowired
- private final fields

### Controller Pattern (must follow BookController.java exactly)
- @Controller annotation
- Constructor injection with @Autowired
- @GetMapping, @PostMapping
- @Valid @ModelAttribute for form binding
- BindingResult for validation errors
- Model for adding attributes
- Return view name or "redirect:/"

### Validator Pattern (must follow ValidCategoryId exactly)
- Annotation with @Target, @Retention, @Constraint, @Documented
- Validator class implements ConstraintValidator
- @Autowired repository in validator

### Template Pattern (must follow add.html exactly)
- xmlns:th="http://www.thymeleaf.org"
- th:replace for layout fragments
- Bootstrap 5.3.3 classes
- th:field for form binding
- th:errors for validation messages
- th:classappend for is-invalid on errors

---

## Research Findings

### Spring Boot Version
- Spring Boot 4.0.2 with Java 25
- Uses Jakarta EE (jakarta.validation, jakarta.servlet)

### Current Dependencies (pom.xml)
- spring-boot-starter-webmvc
- spring-boot-starter-data-mongodb
- spring-boot-starter-thymeleaf
- spring-boot-starter-validation
- NO security dependency yet

### Template Structure
- layout.html has fragments: link-css, custom-css, header, footer, content
- Other templates use th:replace to include fragments
- Bootstrap 5.3.3 from CDN

### Invoice/Cart Flow
- CartService.saveCart() creates Invoice and saves to MongoDB
- Needs to add userId from authenticated user

---

## Scope Boundaries

### INCLUDE
- User entity with UserDetails implementation
- ValidUsername custom validator
- IUserRepository interface
- UserService with UserDetailsService
- UserController for login/register
- SecurityConfig with full security chain
- Invoice entity update (add userId)
- CartService update (set userId)
- login.html and register.html templates
- layout.html updates for auth UI
- pom.xml security dependencies

### EXCLUDE
- Password reset functionality
- Email verification
- User profile page
- Admin roles/authorization
- OAuth2/social login
- API token authentication
- Remember-me persistent tokens (use simple remember-me)
- CSRF configuration (use defaults)

---

## Decisions Made (Auto-Resolved)

1. **Controller Routing**: Create separate UserController at "/" for /login and /register
   - Reason: Follows existing pattern (BookController→/books, CategoryController→/categories)
   - HomeController.home() continues to handle GET "/" → home/index

2. **Email uniqueness validation**: Use @Email annotation only (service layer handles uniqueness)
   - Reason: Simpler approach - custom validator not essential for unique email

3. **Thymeleaf Security Version**: Use thymeleaf-extras-springsecurity6
   - Reason: Compatible with Spring Security 6.x in Spring Boot 4.x

4. **Invoice User Binding**: Auto-bind userId from SecurityContext during checkout
   - Reason: Standard pattern for user-associated data

---

## Test Strategy Decision
- **Infrastructure exists**: YES (spring-boot-starter-webmvc-test in pom.xml)
- **Test approach**: Manual verification via browser
- **QA approach**: Detailed browser verification steps for each task
