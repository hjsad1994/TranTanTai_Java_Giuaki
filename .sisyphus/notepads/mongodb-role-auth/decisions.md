# MongoDB Role-Based Authorization - Decisions

This notepad tracks architectural and implementation decisions.

## [2026-02-05T14:17:55.429Z] Initial Decisions

### From Plan
- Role storage: Separate MongoDB collection (not embedded)
- Role names: String-based ("ADMIN", "USER")
- Authority prefix: "ROLE_ADMIN", "ROLE_USER" for Spring Security
- Admin seeding: On startup via CommandLineRunner
- OAuth users: Get USER role by default


## Implementation Complete - All 14 Tasks Finished
**Date:** 2026-02-05
**Status:** Implementation Complete ✓

### Tasks Completed
**Wave 1 (Foundation):**
✓ Task 1: Role enum (constants/Role.java)
✓ Task 3: IRoleRepository interface
✓ Task 9: Error page templates (403, 404, 500)

**Wave 2 (Entity Layer):**
✓ Task 2: RoleEntity MongoDB document
✓ Task 10: ExceptionController for error routing

**Wave 3 (Service/Config Layer):**
✓ Task 4: User.java with roles field and dynamic authorities
✓ Task 5: UserService with setDefaultRole method
✓ Task 6: OAuthService role assignment integration (verified)
✓ Task 8: DataInitializer for seeding roles and admin user

**Wave 4 (Integration & UI):**
✓ Task 7: SecurityConfig with role-based authorization matrix
✓ Task 11: UserController role assignment (verified)
✓ Task 12: layout.html with role-based navigation
✓ Task 13: book/list.html with role-based button visibility
✓ Task 14: application.properties with error path

### Architecture Summary
**Data Layer:**
- Role collection: Separate MongoDB collection with ADMIN/USER roles
- User-Role relationship: @DBRef linking User to RoleEntity
- Seeding: DataInitializer creates roles and admin user on startup

**Security Layer:**
- Authorization: SecurityConfig hasRole() rules for URL-level protection
- Authorities: User.getAuthorities() dynamically maps roles to Spring Security
- Error Handling: Custom 403/404/500 pages via ExceptionController

**Presentation Layer:**
- Navigation: Thymeleaf sec:authorize controls menu visibility by role
- Actions: Book list buttons filtered by role (Edit/Delete vs Add to cart)
- Consistent: UI matches backend authorization rules exactly

### Next Phase: Manual QA Testing
Implementation complete. Requires hands-on browser testing to verify:
1. Admin user can manage books/categories, cannot access cart
2. Regular user can use cart, cannot manage books/categories
3. OAuth users get USER role automatically
4. Error pages display correctly for access denied scenarios

