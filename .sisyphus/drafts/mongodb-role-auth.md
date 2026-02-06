# Draft: MongoDB Role-Based Authorization

## Requirements (CONFIRMED)
- ADMIN: can edit/delete/add books, manage categories, CANNOT access cart
- USER: can view books, use cart, CANNOT edit/delete books or manage categories
- Both: can view book list, search, access API
- Error pages needed: 403, 404, 500
- UI conditionally shows buttons/menus based on role

## Technical Context
- Spring Boot + MongoDB (not JPA/MySQL)
- User entity implements UserDetails
- Currently returns hardcoded ROLE_USER (line 61-63 in User.java)
- OAuth2 login exists (Google provider) via OAuthService.java
- Thymeleaf templates with Spring Security integration

## User Decisions (from interview)

### 1. MongoDB Role Design
**CONFIRMED**: Separate `Role` collection (Option A) - mirrors JPA/MySQL structure for students

### 2. Role Enum Values
**CONFIRMED**: 
- Enum constants in `constants/Role.java`: `ADMIN(1), USER(2)` for type-safe code
- MongoDB stores role **name** as String (not numeric ID)
- Role document: `{ "name": "ADMIN", "description": "Administrator" }`

### 3. OAuth Users Default Role
**CONFIRMED**: USER role (same as local registration)

### 4. DataInitializer
**CONFIRMED**: Create both:
- Seed ADMIN and USER role documents
- Create default admin: `admin` / `admin123` with ADMIN role

### 5. Test Strategy
**CONFIRMED**: No automated tests - manual verification steps

### 6. Scope Boundaries
**INCLUDE**:
- Role entity and repository
- User-Role relationship  
- SecurityConfig authorization rules
- Default role assignment on registration/OAuth
- UI role-based visibility
- Error pages (403, 404, 500)

**EXCLUDE**:
- Password reset functionality
- User profile management
- Admin dashboard for user management
- Role hierarchy

## Codebase Analysis (Verified)

### Files to Modify
1. `entities/User.java` - Add Set<Role> roles field, update getAuthorities()
2. `config/SecurityConfig.java` - Role-based authorization matrix
3. `services/UserService.java` - Add setDefaultRole(), update save/saveOauthUser
4. `services/OAuthService.java` - May need update for role assignment
5. `controllers/UserController.java` - Call setDefaultRole after registration
6. `templates/layout.html` - sec:authorize for nav items
7. `templates/book/list.html` - sec:authorize for buttons
8. `application.properties` - Add server.error.path

### Files to Create
1. `constants/Role.java` - Enum with ADMIN(1), USER(2)
2. `entities/RoleEntity.java` - MongoDB @Document for roles collection
3. `repositories/IRoleRepository.java` - MongoRepository for RoleEntity
4. `config/DataInitializer.java` - Seed roles and admin user
5. `controllers/ExceptionController.java` - Error routing
6. `templates/errors/403.html` - Access denied
7. `templates/errors/404.html` - Not found
8. `templates/errors/500.html` - Server error

## Authorization Matrix (Final)
| Resource | ADMIN | USER |
|----------|-------|------|
| `/books/edit/**`, `/books/delete/**`, `/books/add` | ✅ | ❌ |
| `/books` (view list), `/books/search` | ✅ | ✅ |
| `/cart/**`, `/books/add-to-cart` | ❌ | ✅ |
| `/categories/**` | ✅ | ❌ |
| `/api/**` | ✅ | ✅ |
