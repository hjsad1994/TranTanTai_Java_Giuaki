# MongoDB Role-Based Authorization Implementation

## TL;DR

> **Quick Summary**: Implement role-based authorization (ADMIN/USER) for a Spring Boot + MongoDB bookstore app. ADMIN manages books/categories but cannot access cart; USER can browse and use cart but cannot manage books/categories.
> 
> **Deliverables**:
> - Role enum and RoleEntity MongoDB document
> - User-Role relationship with proper authorities mapping
> - SecurityConfig with role-based URL authorization matrix
> - Role-based UI visibility (menus, buttons)
> - Error pages (403, 404, 500)
> - DataInitializer to seed roles and admin user
> 
> **Estimated Effort**: Medium (3-4 hours)
> **Parallel Execution**: YES - 4 waves
> **Critical Path**: Task 1 → Task 2 → Task 4 → Task 7 → Task 12

---

## Context

### Original Request
Adapt JPA/MySQL role-based authorization patterns to work with MongoDB for a Spring Boot bookstore application. Key business rule: ADMIN manages inventory but CANNOT use cart; USER can shop but CANNOT manage inventory.

### Interview Summary
**Key Discussions**:
- **MongoDB Design**: Separate `Role` collection chosen (mirrors JPA/MySQL for student learning)
- **Role Storage**: Enum `ADMIN(1), USER(2)` for code; String `name` in MongoDB documents
- **OAuth Users**: Get USER role by default (same as local registration)
- **Admin Seeding**: Create `admin/admin123` with ADMIN role on startup

**Research Findings**:
- `User.java` currently returns hardcoded `ROLE_USER` at line 61-63
- `SecurityConfig.java` has basic authenticated() rules, no role differentiation
- `layout.html` already has `xmlns:sec` but no `sec:authorize` conditions
- `book/list.html` shows all buttons (Edit, Delete, Add to cart) to everyone
- No error pages exist in templates
- `OAuthService.java` calls `userService.saveOauthUser()` - needs role assignment

### Authorization Matrix (Final)
| Resource | ADMIN | USER | Anonymous |
|----------|-------|------|-----------|
| `/`, `/login`, `/register` | N/A | N/A | ✅ |
| `/books`, `/books/search` | ✅ | ✅ | ❌ |
| `/books/add`, `/books/edit/**`, `/books/delete/**` | ✅ | ❌ | ❌ |
| `/books/add-to-cart` | ❌ | ✅ | ❌ |
| `/cart/**` | ❌ | ✅ | ❌ |
| `/categories/**` | ✅ | ❌ | ❌ |
| `/api/**` | ✅ | ✅ | ❌ |

---

## Work Objectives

### Core Objective
Implement MongoDB-compatible role-based authorization that restricts ADMIN from cart access and USER from book/category management.

### Concrete Deliverables
1. `trantantai/src/main/java/trantantai/trantantai/constants/Role.java` - Role enum
2. `trantantai/src/main/java/trantantai/trantantai/entities/RoleEntity.java` - MongoDB document
3. `trantantai/src/main/java/trantantai/trantantai/repositories/IRoleRepository.java` - Repository
4. Updated `User.java` with roles field and dynamic getAuthorities()
5. Updated `UserService.java` with setDefaultRole() method
6. Updated `SecurityConfig.java` with role-based authorization matrix
7. Updated `UserController.java` calling setDefaultRole
8. `trantantai/src/main/java/trantantai/trantantai/config/DataInitializer.java` - Seeds roles & admin
9. `trantantai/src/main/java/trantantai/trantantai/controllers/ExceptionController.java` - Error routing
10. Error templates: `errors/403.html`, `errors/404.html`, `errors/500.html`
11. Updated `layout.html` with role-based navigation
12. Updated `book/list.html` with role-based button visibility

### Definition of Done
- [x] Login as `admin/admin123` → can access `/books/add`, `/categories`; cannot access `/cart`
- [x] Register new user → can access `/cart`, `/books/add-to-cart`; cannot access `/books/add`, `/categories`
- [x] OAuth login → gets USER role, same permissions as registered user
- [x] Access denied shows 403 page with helpful message
- [x] UI shows/hides appropriate navigation items and action buttons based on role

### Must Have
- Separate Role collection (not embedded) - for JPA/MySQL pattern similarity
- String-based role names in MongoDB (not numeric IDs)
- `ROLE_ADMIN` and `ROLE_USER` authority prefixes for Spring Security
- Admin user seeded on startup
- Error pages styled with Bootstrap (consistent with app theme)

### Must NOT Have (Guardrails)
- No role hierarchy (ADMIN does NOT inherit USER permissions for cart)
- No user role management UI
- No password reset functionality
- No hardcoding role logic in controllers (use SecurityConfig only)
- No breaking existing authentication flow (form login, OAuth2, remember-me)
- No changing MongoDB connection settings

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO
- **User wants tests**: NO - Manual verification only
- **Framework**: N/A

### Manual Verification Procedures

Each task includes verification steps the agent can execute using:
- **Browser automation**: Playwright skill for UI verification
- **Bash commands**: `mvn spring-boot:run`, curl for API testing
- **MongoDB queries**: Verify data via application logs or REST endpoints

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately) - Foundation:
├── Task 1: Create Role enum (constants/Role.java)
├── Task 3: Create IRoleRepository.java
└── Task 9: Create error page templates (403, 404, 500)

Wave 2 (After Wave 1) - Entity Layer:
├── Task 2: Create RoleEntity.java (depends: Task 1 enum names)
└── Task 10: Create ExceptionController.java (depends: Task 9 templates exist)

Wave 3 (After Wave 2) - Service/Config Layer:
├── Task 4: Update User.java with roles field (depends: Task 2 RoleEntity)
├── Task 5: Update UserService.java (depends: Task 4 User.roles)
├── Task 6: Update OAuthService.java (depends: Task 5 setDefaultRole)
└── Task 8: Create DataInitializer.java (depends: Task 2, Task 3, Task 5)

Wave 4 (After Wave 3) - Integration & UI:
├── Task 7: Update SecurityConfig.java (depends: Task 4 authorities working)
├── Task 11: Update UserController.java (depends: Task 5 setDefaultRole)
├── Task 12: Update layout.html (depends: Task 7 roles working)
├── Task 13: Update book/list.html (depends: Task 7 roles working)
└── Task 14: Update application.properties (can run anytime, put last for testing)

Critical Path: Task 1 → Task 2 → Task 4 → Task 5 → Task 7 → Task 12
Parallel Speedup: ~50% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2 | 3, 9 |
| 2 | 1 | 4, 8 | 10 |
| 3 | None | 8 | 1, 9 |
| 4 | 2 | 5, 7, 8 | None |
| 5 | 4 | 6, 8, 11 | None |
| 6 | 5 | None | 8, 11 |
| 7 | 4 | 12, 13 | 8, 11 |
| 8 | 2, 3, 5 | None | 6, 7, 11 |
| 9 | None | 10 | 1, 3 |
| 10 | 9 | None | 2 |
| 11 | 5 | None | 6, 7, 8, 12, 13 |
| 12 | 7 | None | 13, 14 |
| 13 | 7 | None | 12, 14 |
| 14 | None | None | Any (low priority) |

---

## TODOs

### Wave 1: Foundation (Start Immediately)

- [x] 1. Create Role enum in constants package

  **What to do**:
  - Create `constants/Role.java` with enum values `ADMIN(1)` and `USER(2)`
  - Include `id` field (int) and constructor
  - Follow existing `Provider.java` enum pattern in same package

  **Must NOT do**:
  - Do not add Spring annotations (it's a plain enum)
  - Do not add JPA annotations

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple file creation, single enum definition, <10 lines
  - **Skills**: None required
    - No complex domain knowledge needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 3, 9)
  - **Blocks**: Task 2 (RoleEntity uses enum names)
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/constants/Provider.java` - Enum pattern to follow (simple enum without id works too, but user requested ADMIN(1), USER(2) style)

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/constants/Role.java` (CREATE NEW)

  **Expected Code Structure**:
  ```java
  package trantantai.trantantai.constants;

  public enum Role {
      ADMIN(1),
      USER(2);

      private final int id;

      Role(int id) {
          this.id = id;
      }

      public int getId() {
          return id;
      }
  }
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify file exists and compiles
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify enum values exist in compiled class
  find target/classes -name "Role.class" | head -1
  # Assert: File exists
  ```

  **Commit**: YES (groups with Tasks 2, 3)
  - Message: `feat(auth): add Role enum and RoleEntity for MongoDB role management`
  - Files: `constants/Role.java`, `entities/RoleEntity.java`, `repositories/IRoleRepository.java`
  - Pre-commit: `mvn compile -q`

---

- [x] 3. Create IRoleRepository interface

  **What to do**:
  - Create `repositories/IRoleRepository.java` extending `MongoRepository<RoleEntity, String>`
  - Add `Optional<RoleEntity> findByName(String name)` method
  - Follow existing repository patterns (`IUserRepository.java`, `IBookRepository.java`)

  **Must NOT do**:
  - Do not add custom query implementations
  - Do not add unnecessary methods

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple interface, follows established pattern, <20 lines
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 9)
  - **Blocks**: Task 8 (DataInitializer needs repository)
  - **Blocked By**: None (can reference RoleEntity by name before it exists)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/repositories/IUserRepository.java` - Repository pattern to follow
  - `trantantai/src/main/java/trantantai/trantantai/repositories/ICategoryRepository.java` - Alternative reference

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/repositories/IRoleRepository.java` (CREATE NEW)

  **Expected Code Structure**:
  ```java
  package trantantai.trantantai.repositories;

  import org.springframework.data.mongodb.repository.MongoRepository;
  import org.springframework.stereotype.Repository;
  import trantantai.trantantai.entities.RoleEntity;

  import java.util.Optional;

  @Repository
  public interface IRoleRepository extends MongoRepository<RoleEntity, String> {
      Optional<RoleEntity> findByName(String name);
  }
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify file exists
  test -f trantantai/src/main/java/trantantai/trantantai/repositories/IRoleRepository.java
  # Assert: Exit code 0
  ```

  **Commit**: YES (groups with Tasks 1, 2)
  - Message: `feat(auth): add Role enum and RoleEntity for MongoDB role management`
  - Files: Combined commit with Task 1, 2
  - Pre-commit: `mvn compile -q`

---

- [x] 9. Create error page templates (403, 404, 500)

  **What to do**:
  - Create `templates/errors/` directory
  - Create `403.html` - Access Denied page with Bootstrap styling
  - Create `404.html` - Not Found page with Bootstrap styling  
  - Create `500.html` - Server Error page with Bootstrap styling
  - Use layout fragments (header, footer) for consistency
  - Include helpful messages and "Go Home" link

  **Must NOT do**:
  - Do not create complex designs (keep simple, Bootstrap-based)
  - Do not add custom CSS (use existing Bootstrap classes)

  **Recommended Agent Profile**:
  - **Category**: `artistry`
    - Reason: HTML template creation, needs consistent styling with existing templates
  - **Skills**: `frontend-ui-ux`
    - Reason: Ensures error pages are user-friendly and visually consistent

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Task 10 (ExceptionController routes to these templates)
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - `trantantai/src/main/resources/templates/layout.html` - Layout fragments to use (header, footer, link-css)
  - `trantantai/src/main/resources/templates/book/list.html` - Template structure example

  **Target Files**:
  - `trantantai/src/main/resources/templates/errors/403.html` (CREATE NEW)
  - `trantantai/src/main/resources/templates/errors/404.html` (CREATE NEW)
  - `trantantai/src/main/resources/templates/errors/500.html` (CREATE NEW)

  **Expected Structure for 403.html**:
  ```html
  <!DOCTYPE html>
  <html xmlns:th="http://www.thymeleaf.org" lang="en">
  <head>
      <title>Access Denied</title>
      <th:block th:replace="~{layout::link-css}"></th:block>
  </head>
  <body>
  <th:block th:replace="~{layout::header}"></th:block>
  <div class="container mt-5">
      <div class="row justify-content-center">
          <div class="col-md-6 text-center">
              <h1 class="display-1 text-danger">403</h1>
              <h2>Access Denied</h2>
              <p class="lead">You don't have permission to access this resource.</p>
              <a href="/" class="btn btn-primary">Go Home</a>
          </div>
      </div>
  </div>
  <th:block th:replace="~{layout::footer}"></th:block>
  </body>
  </html>
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify all three files exist
  test -f trantantai/src/main/resources/templates/errors/403.html && \
  test -f trantantai/src/main/resources/templates/errors/404.html && \
  test -f trantantai/src/main/resources/templates/errors/500.html
  # Assert: Exit code 0
  
  # Verify 403.html contains key elements
  grep -q "403" trantantai/src/main/resources/templates/errors/403.html && \
  grep -q "Access Denied" trantantai/src/main/resources/templates/errors/403.html
  # Assert: Exit code 0
  ```

  **Commit**: YES (groups with Task 10)
  - Message: `feat(errors): add custom error pages (403, 404, 500) with Bootstrap styling`
  - Files: `templates/errors/403.html`, `templates/errors/404.html`, `templates/errors/500.html`
  - Pre-commit: None (HTML files)

---

### Wave 2: Entity Layer

- [x] 2. Create RoleEntity MongoDB document

  **What to do**:
  - Create `entities/RoleEntity.java` with `@Document(collection = "role")`
  - Fields: `id` (String, @Id), `name` (String, @Indexed unique), `description` (String)
  - Add constructors (default + all-args), getters/setters
  - Follow existing entity patterns (Book.java, Category.java)

  **Must NOT do**:
  - Do not use numeric IDs (MongoDB uses String ObjectId)
  - Do not add JPA annotations (@Entity, @Table)
  - Do not embed roles in User (use separate collection)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard entity creation following established patterns
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 10)
  - **Blocks**: Tasks 4, 8 (User and DataInitializer need RoleEntity)
  - **Blocked By**: Task 1 (needs Role enum for reference in description)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/Category.java` - Similar simple entity pattern
  - `trantantai/src/main/java/trantantai/trantantai/entities/User.java:20-30` - MongoDB annotations (@Document, @Id, @Indexed)

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/RoleEntity.java` (CREATE NEW)

  **Expected Code Structure**:
  ```java
  package trantantai.trantantai.entities;

  import org.springframework.data.annotation.Id;
  import org.springframework.data.mongodb.core.index.Indexed;
  import org.springframework.data.mongodb.core.mapping.Document;

  @Document(collection = "role")
  public class RoleEntity {
      @Id
      private String id;

      @Indexed(unique = true)
      private String name;  // "ADMIN" or "USER"

      private String description;

      // Default constructor
      public RoleEntity() {}

      // All-args constructor
      public RoleEntity(String name, String description) {
          this.name = name;
          this.description = description;
      }

      // Getters and setters...
  }
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify compilation with new entity
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify @Document annotation present
  grep -q "@Document" src/main/java/trantantai/trantantai/entities/RoleEntity.java
  # Assert: Exit code 0
  ```

  **Commit**: YES (groups with Tasks 1, 3)
  - Message: `feat(auth): add Role enum and RoleEntity for MongoDB role management`
  - Files: Combined with Task 1, 3
  - Pre-commit: `mvn compile -q`

---

- [x] 10. Create ExceptionController for error routing

  **What to do**:
  - Create `controllers/ExceptionController.java` with `@ControllerAdvice`
  - Handle `AccessDeniedException` → return `errors/403`
  - Add `@Controller` with `/error` mapping for general errors
  - Implement `ErrorController` interface for Spring Boot error handling

  **Must NOT do**:
  - Do not override all exceptions (only access denied + general errors)
  - Do not add complex logging (keep simple)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard Spring controller with known patterns
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 2)
  - **Blocks**: None
  - **Blocked By**: Task 9 (error templates must exist)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/HomeController.java` - Controller pattern
  - Spring Boot ErrorController interface documentation

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/ExceptionController.java` (CREATE NEW)

  **Expected Code Structure**:
  ```java
  package trantantai.trantantai.controllers;

  import jakarta.servlet.RequestDispatcher;
  import jakarta.servlet.http.HttpServletRequest;
  import org.springframework.boot.web.servlet.error.ErrorController;
  import org.springframework.http.HttpStatus;
  import org.springframework.security.access.AccessDeniedException;
  import org.springframework.stereotype.Controller;
  import org.springframework.web.bind.annotation.ControllerAdvice;
  import org.springframework.web.bind.annotation.ExceptionHandler;
  import org.springframework.web.bind.annotation.RequestMapping;

  @Controller
  @ControllerAdvice
  public class ExceptionController implements ErrorController {

      @ExceptionHandler(AccessDeniedException.class)
      public String handleAccessDenied() {
          return "errors/403";
      }

      @RequestMapping("/error")
      public String handleError(HttpServletRequest request) {
          Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
          if (status != null) {
              int statusCode = Integer.parseInt(status.toString());
              if (statusCode == HttpStatus.NOT_FOUND.value()) {
                  return "errors/404";
              } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                  return "errors/403";
              } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                  return "errors/500";
              }
          }
          return "errors/500";
      }
  }
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify compilation
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify ErrorController implementation
  grep -q "implements ErrorController" src/main/java/trantantai/trantantai/controllers/ExceptionController.java
  # Assert: Exit code 0
  ```

  **Commit**: YES (groups with Task 9)
  - Message: `feat(errors): add custom error pages (403, 404, 500) with Bootstrap styling`
  - Files: Combined with Task 9
  - Pre-commit: `mvn compile -q`

---

### Wave 3: Service/Config Layer

- [x] 4. Update User.java with roles field

  **What to do**:
  - Add `Set<RoleEntity> roles` field with `@DBRef` annotation for MongoDB reference
  - Initialize as empty HashSet in field declaration
  - Update `getAuthorities()` to dynamically map roles to `SimpleGrantedAuthority`
  - Add getter/setter for roles
  - Update constructors to include roles parameter

  **Must NOT do**:
  - Do not remove existing fields or methods
  - Do not break UserDetails interface implementation
  - Do not use embedded documents (use @DBRef for separate collection)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Core entity modification, affects security system, needs careful integration
  - **Skills**: None specifically required
    - Standard Spring Security + MongoDB patterns

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential in Wave 3
  - **Blocks**: Tasks 5, 7, 8 (all need User.roles to exist)
  - **Blocked By**: Task 2 (RoleEntity must exist)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/User.java:60-63` - Current hardcoded getAuthorities() to replace
  - `trantantai/src/main/java/trantantai/trantantai/entities/Book.java` - @DBRef usage example (Category reference)

  **API/Type References**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/RoleEntity.java` - Role entity to reference

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/User.java` (MODIFY)

  **Key Changes**:
  ```java
  // Add import
  import org.springframework.data.mongodb.core.mapping.DBRef;
  import java.util.HashSet;
  import java.util.Set;
  import java.util.stream.Collectors;

  // Add field after 'provider' field (around line 44)
  @DBRef
  private Set<RoleEntity> roles = new HashSet<>();

  // Replace getAuthorities() method (lines 60-63)
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
      return roles.stream()
          .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
          .collect(Collectors.toSet());
  }

  // Add getter/setter
  public Set<RoleEntity> getRoles() { return roles; }
  public void setRoles(Set<RoleEntity> roles) { this.roles = roles; }
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify compilation
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify @DBRef annotation added
  grep -q "@DBRef" src/main/java/trantantai/trantantai/entities/User.java
  # Assert: Exit code 0
  
  # Verify roles field exists
  grep -q "Set<RoleEntity> roles" src/main/java/trantantai/trantantai/entities/User.java
  # Assert: Exit code 0
  ```

  **Commit**: YES (individual - critical change)
  - Message: `feat(auth): add roles field to User entity with dynamic authorities`
  - Files: `entities/User.java`
  - Pre-commit: `mvn compile -q`

---

- [x] 5. Update UserService.java with setDefaultRole method

  **What to do**:
  - Inject `IRoleRepository` via constructor
  - Add `setDefaultRole(User user)` method that assigns USER role
  - Update `save(User user)` to call `setDefaultRole()` before saving
  - Update `saveOauthUser()` to call `setDefaultRole()` for new OAuth users

  **Must NOT do**:
  - Do not change password encoding logic
  - Do not modify username generation logic
  - Do not throw exception if role not found (log warning, continue)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Service layer modification affecting user registration flow
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential in Wave 3 (after Task 4)
  - **Blocks**: Tasks 6, 8, 11
  - **Blocked By**: Task 4 (User.roles must exist)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/services/UserService.java:31-34` - Current save() method
  - `trantantai/src/main/java/trantantai/trantantai/services/UserService.java:57-74` - saveOauthUser() method

  **API/Type References**:
  - `trantantai/src/main/java/trantantai/trantantai/repositories/IRoleRepository.java` - Role repository to inject
  - `trantantai/src/main/java/trantantai/trantantai/constants/Role.java` - Role.USER.name() for lookup

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/services/UserService.java` (MODIFY)

  **Key Changes**:
  ```java
  // Add import
  import trantantai.trantantai.repositories.IRoleRepository;
  import trantantai.trantantai.entities.RoleEntity;
  import trantantai.trantantai.constants.Role;
  import java.util.Set;

  // Add field and update constructor
  private final IRoleRepository roleRepository;

  @Autowired
  public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder, 
                     IRoleRepository roleRepository) {
      this.userRepository = userRepository;
      this.passwordEncoder = passwordEncoder;
      this.roleRepository = roleRepository;
  }

  // Add new method
  public void setDefaultRole(User user) {
      if (user.getRoles() == null || user.getRoles().isEmpty()) {
          roleRepository.findByName(Role.USER.name())
              .ifPresent(role -> user.setRoles(Set.of(role)));
      }
  }

  // Update save() method
  public User save(User user) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      setDefaultRole(user);
      return userRepository.save(user);
  }

  // Update saveOauthUser() - add setDefaultRole call before save (line ~73)
  setDefaultRole(newUser);
  return userRepository.save(newUser);
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify compilation
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify setDefaultRole method exists
  grep -q "setDefaultRole" src/main/java/trantantai/trantantai/services/UserService.java
  # Assert: Exit code 0
  
  # Verify IRoleRepository injection
  grep -q "IRoleRepository" src/main/java/trantantai/trantantai/services/UserService.java
  # Assert: Exit code 0
  ```

  **Commit**: YES (groups with Task 6)
  - Message: `feat(auth): add default role assignment for new users and OAuth users`
  - Files: `services/UserService.java`, `services/OAuthService.java`
  - Pre-commit: `mvn compile -q`

---

- [x] 6. Update OAuthService.java for role assignment

  **What to do**:
  - Verify `saveOauthUser()` in UserService now handles role assignment
  - No changes needed in OAuthService if UserService.saveOauthUser() calls setDefaultRole()
  - This task confirms the integration works correctly

  **Must NOT do**:
  - Do not duplicate role assignment logic in OAuthService
  - Do not change OAuth2User return logic

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Verification task, minimal or no changes needed
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 11 after Task 5)
  - **Blocks**: None
  - **Blocked By**: Task 5 (UserService.saveOauthUser must be updated)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/services/OAuthService.java:46-48` - Where saveOauthUser is called

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/services/OAuthService.java` (VERIFY - likely no changes)

  **Expected Outcome**:
  - File unchanged if UserService.saveOauthUser() properly calls setDefaultRole()
  - Role assignment happens transparently through UserService

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify OAuth flow still compiles
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify OAuthService still calls saveOauthUser
  grep -q "saveOauthUser" src/main/java/trantantai/trantantai/services/OAuthService.java
  # Assert: Exit code 0
  ```

  **Commit**: YES (groups with Task 5)
  - Message: `feat(auth): add default role assignment for new users and OAuth users`
  - Files: May be no changes to this file
  - Pre-commit: `mvn compile -q`

---

- [x] 8. Create DataInitializer for seeding roles and admin user

  **What to do**:
  - Create `config/DataInitializer.java` implementing `CommandLineRunner`
  - On startup: Create ADMIN and USER role documents if not exist
  - Create admin user (`admin`/`admin123`) with ADMIN role if not exist
  - Use `@Component` annotation
  - Add logging for initialization actions

  **Must NOT do**:
  - Do not delete existing data
  - Do not recreate roles/admin if already exist (idempotent)
  - Do not hardcode plain text password (use PasswordEncoder)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Startup configuration affecting data state
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 6, 7, 11 - after Task 5)
  - **Blocks**: None (runs on app startup)
  - **Blocked By**: Tasks 2, 3, 5 (RoleEntity, IRoleRepository, UserService)

  **References**:

  **Pattern References**:
  - Spring Boot CommandLineRunner pattern
  - `trantantai/src/main/java/trantantai/trantantai/services/UserService.java` - Password encoding pattern

  **API/Type References**:
  - `trantantai/src/main/java/trantantai/trantantai/repositories/IRoleRepository.java`
  - `trantantai/src/main/java/trantantai/trantantai/repositories/IUserRepository.java`
  - `trantantai/src/main/java/trantantai/trantantai/constants/Role.java`

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/config/DataInitializer.java` (CREATE NEW)

  **Expected Code Structure**:
  ```java
  package trantantai.trantantai.config;

  import org.springframework.boot.CommandLineRunner;
  import org.springframework.security.crypto.password.PasswordEncoder;
  import org.springframework.stereotype.Component;
  import trantantai.trantantai.constants.Role;
  import trantantai.trantantai.entities.RoleEntity;
  import trantantai.trantantai.entities.User;
  import trantantai.trantantai.repositories.IRoleRepository;
  import trantantai.trantantai.repositories.IUserRepository;

  import java.util.Set;

  @Component
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
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify compilation
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify CommandLineRunner implementation
  grep -q "implements CommandLineRunner" src/main/java/trantantai/trantantai/config/DataInitializer.java
  # Assert: Exit code 0
  ```

  **Commit**: YES (individual - data initialization)
  - Message: `feat(auth): add DataInitializer to seed roles and admin user on startup`
  - Files: `config/DataInitializer.java`
  - Pre-commit: `mvn compile -q`

---

### Wave 4: Integration & UI

- [x] 7. Update SecurityConfig.java with role-based authorization

  **What to do**:
  - Replace generic `.authenticated()` with specific role-based rules
  - ADMIN only: `/books/add`, `/books/edit/**`, `/books/delete/**`, `/categories/**`
  - USER only: `/cart/**`, `/books/add-to-cart`
  - Both roles: `/books`, `/books/search`, `/api/**`
  - Add `.exceptionHandling()` for access denied redirect

  **Must NOT do**:
  - Do not remove OAuth2 configuration
  - Do not remove remember-me configuration
  - Do not change login/logout URLs
  - Do not use hasAnyRole - be explicit with hasRole for each path

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Security configuration - critical for access control
  - **Skills**: None required
    - Standard Spring Security patterns

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 11, 12, 13)
  - **Blocks**: Tasks 12, 13 (UI relies on roles working)
  - **Blocked By**: Task 4 (User.getAuthorities() must return proper roles)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/config/SecurityConfig.java:28-34` - Current authorization rules to replace

  **Documentation References**:
  - Spring Security hasRole() vs hasAuthority() - hasRole() auto-prepends "ROLE_"

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/config/SecurityConfig.java` (MODIFY)

  **Key Changes**:
  ```java
  // Replace lines 28-34 with:
  .authorizeHttpRequests(auth -> auth
      // Public endpoints
      .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/error", "/errors/**").permitAll()
      .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
      
      // ADMIN only - book management
      .requestMatchers("/books/add", "/books/add/**").hasRole("ADMIN")
      .requestMatchers("/books/edit/**").hasRole("ADMIN")
      .requestMatchers("/books/delete/**").hasRole("ADMIN")
      
      // ADMIN only - category management
      .requestMatchers("/categories/**").hasRole("ADMIN")
      
      // USER only - cart operations (ADMIN cannot access cart!)
      .requestMatchers("/cart/**").hasRole("USER")
      .requestMatchers("/books/add-to-cart").hasRole("USER")
      
      // Both roles - viewing books and API
      .requestMatchers("/books", "/books/search").authenticated()
      .requestMatchers("/api/**").authenticated()
      
      // Default - require authentication
      .anyRequest().authenticated()
  )
  // Add after logout config (around line 56):
  .exceptionHandling(ex -> ex
      .accessDeniedPage("/errors/403")
  )
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify compilation
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify role-based rules exist
  grep -q "hasRole" src/main/java/trantantai/trantantai/config/SecurityConfig.java
  # Assert: Exit code 0
  
  # Verify cart restricted to USER
  grep -q 'requestMatchers.*cart.*hasRole.*USER' src/main/java/trantantai/trantantai/config/SecurityConfig.java
  # Assert: Exit code 0
  ```

  **Commit**: YES (individual - security critical)
  - Message: `feat(auth): implement role-based URL authorization matrix in SecurityConfig`
  - Files: `config/SecurityConfig.java`
  - Pre-commit: `mvn compile -q`

---

- [x] 11. Update UserController.java to use default role

  **What to do**:
  - Verify that `userService.save(user)` now handles role assignment automatically
  - No changes needed if UserService.save() calls setDefaultRole()
  - This task confirms the registration flow works correctly

  **Must NOT do**:
  - Do not add explicit role assignment in controller (service handles it)
  - Do not change validation logic

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Verification task, minimal or no changes
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 7, 12, 13)
  - **Blocks**: None
  - **Blocked By**: Task 5 (UserService.save must call setDefaultRole)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/UserController.java:52-53` - Current save call

  **Target File**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/UserController.java` (VERIFY - likely no changes)

  **Expected Outcome**:
  - File unchanged if UserService.save() properly handles role assignment
  - Registration flow automatically assigns USER role

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify compilation
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify save() is still called
  grep -q "userService.save" src/main/java/trantantai/trantantai/controllers/UserController.java
  # Assert: Exit code 0
  ```

  **Commit**: NO (likely no changes)

---

- [x] 12. Update layout.html with role-based navigation

  **What to do**:
  - Wrap "Add Book" link with `sec:authorize="hasRole('ADMIN')"`
  - Wrap "Categories" link with `sec:authorize="hasRole('ADMIN')"`
  - Wrap "Cart" link with `sec:authorize="hasRole('USER')"`
  - Keep "List Book" visible to all authenticated users
  - Ensure anonymous users only see Home, Login, Register

  **Must NOT do**:
  - Do not change existing authentication-based visibility (isAnonymous, isAuthenticated)
  - Do not remove Bootstrap classes or break layout
  - Do not add inline styles

  **Recommended Agent Profile**:
  - **Category**: `artistry`
    - Reason: Template modification, needs UI consistency
  - **Skills**: `frontend-ui-ux`
    - Reason: Ensures navigation remains user-friendly

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 13, 14)
  - **Blocks**: None
  - **Blocked By**: Task 7 (SecurityConfig must have roles working)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/resources/templates/layout.html:45-57` - Existing sec:authorize usage for auth state
  - `trantantai/src/main/resources/templates/layout.html:26-40` - Navigation items to modify

  **Documentation References**:
  - Thymeleaf Spring Security integration: `sec:authorize="hasRole('ROLENAME')"`

  **Target File**:
  - `trantantai/src/main/resources/templates/layout.html` (MODIFY)

  **Key Changes**:
  ```html
  <!-- Replace navigation items (lines 26-40) with: -->
  <ul class="navbar-nav me-auto mb-2 mb-lg-0">
      <li class="nav-item">
          <a class="nav-link active" aria-current="page" href="/">Home</a>
      </li>
      <!-- Show to all authenticated users -->
      <li class="nav-item" sec:authorize="isAuthenticated()">
          <a class="nav-link" href="/books">List Book</a>
      </li>
      <!-- ADMIN only -->
      <li class="nav-item" sec:authorize="hasRole('ADMIN')">
          <a class="nav-link" href="/books/add">Add Book</a>
      </li>
      <li class="nav-item" sec:authorize="hasRole('ADMIN')">
          <a class="nav-link" href="/categories">Categories</a>
      </li>
      <!-- USER only -->
      <li class="nav-item" sec:authorize="hasRole('USER')">
          <a class="nav-link" href="/cart">Cart</a>
      </li>
  </ul>
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify sec:authorize attributes added
  grep -q "hasRole('ADMIN')" src/main/resources/templates/layout.html
  # Assert: Exit code 0
  
  grep -q "hasRole('USER')" src/main/resources/templates/layout.html
  # Assert: Exit code 0
  ```

  **Commit**: YES (groups with Task 13)
  - Message: `feat(ui): add role-based visibility to navigation and book list buttons`
  - Files: `templates/layout.html`, `templates/book/list.html`
  - Pre-commit: None (HTML files)

---

- [x] 13. Update book/list.html with role-based button visibility

  **What to do**:
  - Add `xmlns:sec` namespace to html tag
  - Wrap Edit/Delete buttons with `sec:authorize="hasRole('ADMIN')"`
  - Wrap "Add to cart" form with `sec:authorize="hasRole('USER')"`
  - Keep table rows and book data visible to all authenticated users

  **Must NOT do**:
  - Do not change table structure
  - Do not modify pagination
  - Do not break form submission logic

  **Recommended Agent Profile**:
  - **Category**: `artistry`
    - Reason: Template modification with conditional rendering
  - **Skills**: `frontend-ui-ux`
    - Reason: Maintains UI consistency in action column

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 12, 14)
  - **Blocks**: None
  - **Blocked By**: Task 7 (SecurityConfig must have roles working)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/resources/templates/book/list.html:46-59` - Action column buttons to modify
  - `trantantai/src/main/resources/templates/layout.html:2-3` - xmlns:sec declaration pattern

  **Target File**:
  - `trantantai/src/main/resources/templates/book/list.html` (MODIFY)

  **Key Changes**:
  ```html
  <!-- Add to <html> tag (line 2): -->
  <html xmlns:th="http://www.thymeleaf.org" 
        xmlns:sec="http://www.thymeleaf.org/extras/spring-security" lang="en">

  <!-- Replace action column content (lines 46-59) with: -->
  <td colspan="2">
      <!-- ADMIN only: Edit and Delete -->
      <span sec:authorize="hasRole('ADMIN')">
          <a class="btn btn-primary" th:href="@{/books/edit/{id}(id=${book.getId()})}">Edit</a>
          <a class="btn btn-danger" th:href="@{/books/delete/{id}(id=${book.getId()})}"
             onclick="return confirm('Are you sure you want to delete this book?')">Delete</a>
      </span>
      <!-- USER only: Add to cart -->
      <form sec:authorize="hasRole('USER')" th:action="@{/books/add-to-cart}" method="post" class="d-inline">
          <input type="hidden" name="id" th:value="${book.getId()}">
          <input type="hidden" name="name" th:value="${book.getTitle()}">
          <input type="hidden" name="price" th:value="${book.getPrice()}">
          <button type="submit" class="btn btn-success"
                  onclick="return confirm('Are you sure you want to add this book to cart?')">
              Add to cart
          </button>
      </form>
  </td>
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify xmlns:sec added
  grep -q 'xmlns:sec' src/main/resources/templates/book/list.html
  # Assert: Exit code 0
  
  # Verify role-based visibility
  grep -q "hasRole('ADMIN')" src/main/resources/templates/book/list.html
  # Assert: Exit code 0
  
  grep -q "hasRole('USER')" src/main/resources/templates/book/list.html
  # Assert: Exit code 0
  ```

  **Commit**: YES (groups with Task 12)
  - Message: `feat(ui): add role-based visibility to navigation and book list buttons`
  - Files: Combined with Task 12
  - Pre-commit: None (HTML files)

---

- [x] 14. Update application.properties with error path

  **What to do**:
  - Add `server.error.path=/error` to application.properties
  - Add `server.error.whitelabel.enabled=false` to disable default error page

  **Must NOT do**:
  - Do not change MongoDB connection settings
  - Do not change OAuth2 settings
  - Do not expose sensitive information in errors

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple config addition, 2 lines
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (any time, low priority)
  - **Blocks**: None
  - **Blocked By**: None (can run anytime)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/resources/application.properties` - Current config file

  **Target File**:
  - `trantantai/src/main/resources/application.properties` (MODIFY)

  **Key Changes**:
  ```properties
  # Add at end of file:

  # Error Handling
  server.error.path=/error
  server.error.whitelabel.enabled=false
  ```

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify error config added
  grep -q "server.error.path" src/main/resources/application.properties
  # Assert: Exit code 0
  
  grep -q "whitelabel.enabled=false" src/main/resources/application.properties
  # Assert: Exit code 0
  ```

  **Commit**: YES (groups with Tasks 9, 10 error handling)
  - Message: `feat(errors): add custom error pages (403, 404, 500) with Bootstrap styling`
  - Files: May combine with error page commits
  - Pre-commit: None (properties file)

---

## Commit Strategy

| After Task(s) | Message | Files | Verification |
|---------------|---------|-------|--------------|
| 1, 2, 3 | `feat(auth): add Role enum and RoleEntity for MongoDB role management` | constants/Role.java, entities/RoleEntity.java, repositories/IRoleRepository.java | `mvn compile -q` |
| 4 | `feat(auth): add roles field to User entity with dynamic authorities` | entities/User.java | `mvn compile -q` |
| 5, 6 | `feat(auth): add default role assignment for new users and OAuth users` | services/UserService.java | `mvn compile -q` |
| 7 | `feat(auth): implement role-based URL authorization matrix in SecurityConfig` | config/SecurityConfig.java | `mvn compile -q` |
| 8 | `feat(auth): add DataInitializer to seed roles and admin user on startup` | config/DataInitializer.java | `mvn compile -q` |
| 9, 10, 14 | `feat(errors): add custom error pages (403, 404, 500) with Bootstrap styling` | templates/errors/*.html, controllers/ExceptionController.java, application.properties | `mvn compile -q` |
| 12, 13 | `feat(ui): add role-based visibility to navigation and book list buttons` | templates/layout.html, templates/book/list.html | N/A |

---

## Success Criteria

### Manual Verification Commands (End-to-End)

```bash
# 1. Start the application
cd trantantai && mvn spring-boot:run

# 2. Open browser to http://localhost:8080
```

### Test Scenarios (Use Playwright Browser Automation)

**Scenario 1: Admin User Flow**
```
1. Navigate to: http://localhost:8080/login
2. Fill: username="admin", password="admin123"
3. Click: Login button
4. Assert: Redirected to /books
5. Assert: Navigation shows "Add Book", "Categories"
6. Assert: Navigation does NOT show "Cart"
7. Navigate to: http://localhost:8080/books
8. Assert: Edit and Delete buttons visible
9. Assert: "Add to cart" button NOT visible
10. Navigate to: http://localhost:8080/cart
11. Assert: Shows 403 Access Denied page
12. Screenshot: .sisyphus/evidence/admin-flow.png
```

**Scenario 2: Regular User Flow**
```
1. Navigate to: http://localhost:8080/register
2. Fill: username="testuser", password="test123", email="test@test.com"
3. Click: Register button
4. Navigate to: http://localhost:8080/login
5. Fill: username="testuser", password="test123"
6. Click: Login button
7. Assert: Redirected to /books
8. Assert: Navigation shows "Cart"
9. Assert: Navigation does NOT show "Add Book", "Categories"
10. Navigate to: http://localhost:8080/books
11. Assert: "Add to cart" button visible
12. Assert: Edit and Delete buttons NOT visible
13. Navigate to: http://localhost:8080/books/add
14. Assert: Shows 403 Access Denied page
15. Screenshot: .sisyphus/evidence/user-flow.png
```

**Scenario 3: Error Pages**
```
1. Navigate to: http://localhost:8080/nonexistent
2. Assert: Shows 404 Not Found page
3. Navigate to: http://localhost:8080/cart (as admin)
4. Assert: Shows 403 Access Denied page
5. Screenshot: .sisyphus/evidence/error-pages.png
```

### Final Checklist
- [x] `admin/admin123` can login and access book management
- [x] `admin/admin123` cannot access cart (sees 403)
- [x] New registered user gets USER role automatically
- [x] USER can access cart and add-to-cart
- [x] USER cannot access book management (sees 403)
- [x] OAuth Google login assigns USER role
- [x] Navigation shows role-appropriate items
- [x] Book list shows role-appropriate action buttons
- [x] Error pages display with Bootstrap styling
- [x] All existing functionality (pagination, search, checkout) still works
