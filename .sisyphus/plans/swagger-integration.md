# Swagger/OpenAPI Integration for TranTanTai Book Store

## TL;DR

> **Quick Summary**: Integrate SpringDoc OpenAPI 3 (Swagger UI) into Spring Boot 4.0.2 project to replace the custom API Demo interface with professional interactive API documentation.
> 
> **Deliverables**:
> - SpringDoc OpenAPI dependency added to pom.xml
> - OpenApiConfig.java with API metadata configuration
> - Both API controllers annotated with OpenAPI annotations
> - All 4 ViewModels annotated with @Schema
> - SecurityConfig updated for Swagger paths (authenticated access)
> - API Demo section replaced with Swagger UI link button
> 
> **Estimated Effort**: Medium (4-6 hours)
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 0 (Spike) → Task 1 (Dependency) → Tasks 2,3,4 (Parallel) → Task 5,6 (Parallel) → Task 7 (Verify)

---

## Context

### Original Request
Chuyển phần API Demo giao diện sang sử dụng Swagger API để dễ quản lý các API. (Replace API Demo interface with Swagger UI for better API management)

### Interview Summary
**Key Discussions**:
- **Spring Boot Version**: Keep 4.0.2, try springdoc-openapi 2.8.x despite known compatibility issues
- **API Demo Handling**: Replace with a button linking to /swagger-ui.html
- **Authentication**: Swagger UI requires login (authenticated users only)
- **Metadata**: Default values - "TranTanTai Book Store API v1.0.0"

**Research Findings**:
- springdoc-openapi v2.8.15 has known issues with Spring Boot 4.x (Jackson 2→3, package reorganization)
- May need exclusions or workarounds if ClassNotFoundException occurs
- Standard paths: /swagger-ui.html, /v3/api-docs

### Metis Review
**Identified Gaps** (addressed):
- **Fallback plan**: Added spike task (Task 0) to validate compatibility early
- **Authentication flow**: Swagger UI will share web app session (form login), documented in Task 5
- **CSRF handling**: Will be addressed in SecurityConfig updates
- **Validation constraints**: @Schema annotations will expose validation rules from Jakarta Validation

---

## Work Objectives

### Core Objective
Replace the custom HTML-based API Demo interface with SpringDoc OpenAPI 3 (Swagger UI) to provide professional, interactive API documentation for the TranTanTai Book Store application.

### Concrete Deliverables
1. `pom.xml` - Updated with springdoc-openapi-starter-webmvc-ui dependency
2. `OpenApiConfig.java` - New configuration class with @OpenAPIDefinition
3. `BookApiController.java` - Annotated with @Tag, @Operation, @ApiResponse, @Parameter
4. `CategoryApiController.java` - Annotated with @Tag, @Operation, @ApiResponse, @Parameter
5. `BookGetVm.java`, `BookPostVm.java`, `CategoryGetVm.java`, `CategoryPostVm.java` - Annotated with @Schema
6. `SecurityConfig.java` - Updated to permit Swagger static resources (authenticated access)
7. `book/list.html` - API Demo section replaced with Swagger UI link button

### Definition of Done
- [ ] Application starts without errors: `mvn spring-boot:run` exits clean
- [ ] Swagger UI loads at `/swagger-ui.html` after login
- [ ] All 11 endpoints visible (6 Book + 5 Category)
- [ ] All 4 ViewModels visible in Schemas section
- [ ] "Try It Out" works for GET /api/v1/books
- [ ] API Demo section removed, replaced with working Swagger button
- [ ] No JavaScript errors in browser console

### Must Have
- SpringDoc OpenAPI 3 integration with Swagger UI
- All REST endpoints documented with proper annotations
- All ViewModels documented with @Schema
- Swagger UI accessible only to authenticated users
- Clean button linking to Swagger UI in book list page

### Must NOT Have (Guardrails)
- ❌ No changes to controller business logic (only add annotations)
- ❌ No changes to ViewModel structure (only add @Schema annotations)
- ❌ No changes to authentication/authorization rules (except Swagger paths)
- ❌ No new API endpoints
- ❌ No refactoring of existing code
- ❌ No MongoDB entity modifications
- ❌ No additional documentation tools (only SpringDoc)
- ❌ No scope expansion beyond 11 existing endpoints

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: YES (existing tests: BookApiControllerTest, CategoryApiControllerTest)
- **User wants tests**: Manual-only for this integration task
- **Framework**: Maven (mvn spring-boot:run, mvn test)

### Manual Verification Approach

Each TODO includes EXECUTABLE verification procedures using browser and terminal:

**Verification Tools:**
| Type | Tool | Method |
|------|------|--------|
| Application Startup | Terminal (mvn) | Check for errors, port binding |
| Swagger UI | Browser | Navigate to /swagger-ui.html |
| Authentication | Browser | Verify login redirect |
| API Testing | Swagger UI "Try It Out" | Execute requests |
| UI Changes | Browser | Verify button exists and works |

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 0 (Spike - Must Complete First):
└── Task 0: Validate SpringDoc compatibility with Spring Boot 4.0.2

Wave 1 (After Spike Passes):
└── Task 1: Add SpringDoc dependency to pom.xml

Wave 2 (After Dependency Added - Parallel):
├── Task 2: Create OpenApiConfig.java
├── Task 3: Annotate BookApiController
└── Task 4: Annotate CategoryApiController

Wave 3 (After Controllers Done - Parallel):
├── Task 5: Update SecurityConfig for Swagger paths
└── Task 6: Annotate all ViewModels with @Schema

Wave 4 (After Security & Schemas):
└── Task 7: Replace API Demo section with Swagger link

Wave 5 (Final):
└── Task 8: End-to-end verification and cleanup

Critical Path: Task 0 → Task 1 → Task 2 → Task 5 → Task 7 → Task 8
Parallel Speedup: ~35% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 0 | None | 1 | None (spike) |
| 1 | 0 | 2, 3, 4 | None |
| 2 | 1 | 5 | 3, 4 |
| 3 | 1 | 7 | 2, 4 |
| 4 | 1 | 7 | 2, 3 |
| 5 | 2 | 7 | 6 |
| 6 | 1 | 8 | 5 |
| 7 | 3, 4, 5 | 8 | None |
| 8 | 6, 7 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Approach |
|------|-------|---------------------|
| 0 | 0 | Sequential - must validate before proceeding |
| 1 | 1 | Sequential - foundation task |
| 2 | 2, 3, 4 | Parallel (3 agents) |
| 3 | 5, 6 | Parallel (2 agents) |
| 4 | 7 | Sequential |
| 5 | 8 | Sequential - final verification |

---

## TODOs

### - [ ] 0. Spike: Validate SpringDoc Compatibility with Spring Boot 4.0.2

**What to do**:
- Add springdoc-openapi-starter-webmvc-ui 2.8.15 dependency to pom.xml
- Run `mvn clean compile` to check for dependency resolution
- Run `mvn spring-boot:run` to check for runtime errors
- If errors occur, document them and try exclusions/workarounds
- If unresolvable, STOP and escalate to user

**Must NOT do**:
- Don't add any annotations yet
- Don't modify any Java files
- Don't spend more than 1 hour troubleshooting

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Short validation task with clear pass/fail outcome
- **Skills**: [`git-master`]
  - `git-master`: To revert changes if spike fails

**Parallelization**:
- **Can Run In Parallel**: NO
- **Parallel Group**: Wave 0 (standalone)
- **Blocks**: Task 1 (cannot proceed without validation)
- **Blocked By**: None

**References**:

**Pattern References**:
- `trantantai/pom.xml:32-91` - Current dependencies section, add after line 73 (OAuth2 client)

**Documentation References**:
- SpringDoc GitHub Issues: https://github.com/springdoc/springdoc-openapi/issues/3196 (Spring Boot 4 WebFlux issue)
- SpringDoc GitHub Issues: https://github.com/springdoc/springdoc-openapi/issues/3200 (Jackson 3 compatibility)

**WHY Each Reference Matters**:
- `pom.xml` - Need to understand existing dependency structure to add new dependency correctly
- GitHub issues - Document known compatibility problems and potential workarounds

**Acceptance Criteria**:

**Automated Verification (Terminal):**
```bash
# Step 1: Compile check
cd trantantai && mvn clean compile
# Assert: BUILD SUCCESS (no dependency resolution errors)

# Step 2: Startup check
mvn spring-boot:run &
# Wait 30 seconds for startup
sleep 30
curl -s http://localhost:8080/v3/api-docs | head -20
# Assert: Returns JSON with "openapi":"3.x.x"
# Kill the app
pkill -f "spring-boot:run"
```

**If Errors Occur:**
```
ESCALATION PROTOCOL:
1. Document exact error message
2. Try adding Jackson exclusions to springdoc dependency
3. If still fails after 1 hour, STOP and notify user with options:
   a) Downgrade Spring Boot to 3.5.x
   b) Wait for springdoc 3.x
   c) Abandon Swagger integration
```

**Evidence to Capture:**
- [ ] Screenshot or copy of `mvn compile` output
- [ ] Screenshot or copy of `mvn spring-boot:run` startup logs
- [ ] JSON response from /v3/api-docs (first 20 lines)

**Commit**: NO (spike only - will be included in Task 1)

---

### - [ ] 1. Add SpringDoc OpenAPI Dependency to pom.xml

**What to do**:
- Add springdoc-openapi-starter-webmvc-ui 2.8.15 dependency
- Place after OAuth2 client dependency (line ~73)
- Verify dependency resolution with `mvn dependency:tree`

**Must NOT do**:
- Don't add any other dependencies
- Don't modify Spring Boot version
- Don't add BOM (keep it simple for now)

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Single file edit with clear outcome
- **Skills**: None needed

**Parallelization**:
- **Can Run In Parallel**: NO
- **Parallel Group**: Wave 1 (foundation)
- **Blocks**: Tasks 2, 3, 4, 5, 6
- **Blocked By**: Task 0 (spike must pass)

**References**:

**Pattern References**:
- `trantantai/pom.xml:32-91` - Dependencies section structure

**Code to Add:**
```xml
<!-- SpringDoc OpenAPI 3 - Swagger UI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.15</version>
</dependency>
```

**WHY Each Reference Matters**:
- `pom.xml` - Must follow existing formatting and placement conventions

**Acceptance Criteria**:

**Automated Verification (Terminal):**
```bash
cd trantantai
# Verify dependency added
grep -A2 "springdoc-openapi" pom.xml
# Assert: Shows groupId, artifactId, version

# Verify resolution
mvn dependency:tree | grep springdoc
# Assert: Shows springdoc-openapi-starter-webmvc-ui:2.8.15
```

**Evidence to Capture:**
- [ ] `mvn dependency:tree` output showing springdoc resolved

**Commit**: YES
- Message: `build(deps): add springdoc-openapi 2.8.15 for Swagger UI`
- Files: `trantantai/pom.xml`
- Pre-commit: `mvn compile`

---

### - [ ] 2. Create OpenApiConfig.java with API Metadata

**What to do**:
- Create new file: `src/main/java/trantantai/trantantai/config/OpenApiConfig.java`
- Add @Configuration and @OpenAPIDefinition annotations
- Configure API metadata: title, version, description
- Configure server URL for localhost

**Must NOT do**:
- Don't add security schemes yet (will use session-based auth)
- Don't customize Swagger UI properties (use defaults)
- Don't add Bean-based customization (keep annotation-based)

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Single new file with straightforward content
- **Skills**: None needed

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 2 (with Tasks 3, 4)
- **Blocks**: Task 5
- **Blocked By**: Task 1

**References**:

**Pattern References**:
- `trantantai/src/main/java/trantantai/trantantai/config/SecurityConfig.java` - Configuration class pattern in this project
- `trantantai/src/main/java/trantantai/trantantai/config/MongoConfig.java` - Another config class example

**External References**:
- SpringDoc docs: https://springdoc.org/#getting-started

**Code Template:**
```java
package trantantai.trantantai.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "TranTanTai Book Store API",
        version = "v1.0.0",
        description = "REST API documentation for TranTanTai Book Store application. " +
                      "Provides endpoints for managing books and categories.",
        contact = @Contact(
            name = "TranTanTai",
            email = "trantantai@example.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development Server")
    }
)
public class OpenApiConfig {
    // Configuration via annotations - no additional beans needed
}
```

**WHY Each Reference Matters**:
- `SecurityConfig.java` - Shows @Configuration pattern used in this project
- `MongoConfig.java` - Shows another config class for consistency

**Acceptance Criteria**:

**Automated Verification (Terminal):**
```bash
# Verify file exists
ls trantantai/src/main/java/trantantai/trantantai/config/OpenApiConfig.java
# Assert: File exists

# Verify compiles
cd trantantai && mvn compile
# Assert: BUILD SUCCESS

# Verify metadata appears in OpenAPI spec
mvn spring-boot:run &
sleep 30
curl -s http://localhost:8080/v3/api-docs | grep -E '"title"|"version"'
# Assert: Shows "TranTanTai Book Store API" and "v1.0.0"
pkill -f "spring-boot:run"
```

**Evidence to Capture:**
- [ ] OpenApiConfig.java file content
- [ ] /v3/api-docs response showing title and version

**Commit**: YES (groups with Task 3, 4)
- Message: `feat(swagger): add OpenAPI configuration with API metadata`
- Files: `trantantai/src/main/java/trantantai/trantantai/config/OpenApiConfig.java`
- Pre-commit: `mvn compile`

---

### - [ ] 3. Annotate BookApiController with OpenAPI Annotations

**What to do**:
- Add @Tag annotation to class for grouping
- Add @Operation annotation to each of 6 endpoints
- Add @ApiResponse annotations for success and error codes
- Add @Parameter annotations for path variables and query params

**Must NOT do**:
- Don't modify method signatures
- Don't change return types
- Don't alter business logic
- Don't add new endpoints

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Annotation additions only, no logic changes
- **Skills**: None needed

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 2 (with Tasks 2, 4)
- **Blocks**: Task 7
- **Blocked By**: Task 1

**References**:

**Pattern References**:
- `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java:1-116` - Full current implementation

**External References**:
- SpringDoc annotations: https://springdoc.org/#migrating-from-springfox

**Annotations to Add:**

```java
// Class level
@Tag(name = "Books", description = "Book management APIs - CRUD operations for books")

// GET /api/v1/books
@Operation(
    summary = "Get all books",
    description = "Retrieves a paginated list of all books"
)
@ApiResponse(responseCode = "200", description = "Successfully retrieved book list")

// GET /api/v1/books/{id}
@Operation(
    summary = "Get book by ID",
    description = "Retrieves a specific book by its unique identifier"
)
@ApiResponse(responseCode = "200", description = "Book found")
@ApiResponse(responseCode = "404", description = "Book not found")
@Parameter(name = "id", description = "Book ID", required = true)

// GET /api/v1/books/search
@Operation(
    summary = "Search books",
    description = "Searches books by keyword in title or author"
)
@ApiResponse(responseCode = "200", description = "Search results returned")
@Parameter(name = "keyword", description = "Search keyword", required = true)

// POST /api/v1/books
@Operation(
    summary = "Create a new book",
    description = "Creates a new book with the provided details"
)
@ApiResponse(responseCode = "201", description = "Book created successfully")
@ApiResponse(responseCode = "400", description = "Invalid input - category not found")

// PUT /api/v1/books/{id}
@Operation(
    summary = "Update a book",
    description = "Updates an existing book with the provided details"
)
@ApiResponse(responseCode = "200", description = "Book updated successfully")
@ApiResponse(responseCode = "404", description = "Book not found")
@ApiResponse(responseCode = "400", description = "Invalid input - category not found")

// DELETE /api/v1/books/{id}
@Operation(
    summary = "Delete a book",
    description = "Deletes a book by its ID"
)
@ApiResponse(responseCode = "204", description = "Book deleted successfully")
@ApiResponse(responseCode = "404", description = "Book not found")
```

**WHY Each Reference Matters**:
- `BookApiController.java` - Must understand current structure to add annotations without breaking code

**Acceptance Criteria**:

**Automated Verification (Terminal):**
```bash
cd trantantai

# Verify annotations added (check for @Tag)
grep -c "@Tag" src/main/java/trantantai/trantantai/controllers/BookApiController.java
# Assert: Returns 1

# Verify @Operation count (should be 6)
grep -c "@Operation" src/main/java/trantantai/trantantai/controllers/BookApiController.java
# Assert: Returns 6

# Verify compiles
mvn compile
# Assert: BUILD SUCCESS
```

**Evidence to Capture:**
- [ ] grep counts for @Tag, @Operation
- [ ] mvn compile output

**Commit**: YES (groups with Task 2, 4)
- Message: `docs(swagger): add OpenAPI annotations to BookApiController`
- Files: `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java`
- Pre-commit: `mvn compile`

---

### - [ ] 4. Annotate CategoryApiController with OpenAPI Annotations

**What to do**:
- Add @Tag annotation to class for grouping
- Add @Operation annotation to each of 5 endpoints
- Add @ApiResponse annotations for success and error codes
- Add @Parameter annotations for path variables

**Must NOT do**:
- Don't modify method signatures
- Don't change return types
- Don't alter business logic
- Don't add new endpoints

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Annotation additions only, no logic changes
- **Skills**: None needed

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 2 (with Tasks 2, 3)
- **Blocks**: Task 7
- **Blocked By**: Task 1

**References**:

**Pattern References**:
- `trantantai/src/main/java/trantantai/trantantai/controllers/CategoryApiController.java:1-88` - Full current implementation

**Annotations to Add:**

```java
// Class level
@Tag(name = "Categories", description = "Category management APIs - CRUD operations for book categories")

// GET /api/v1/categories
@Operation(
    summary = "Get all categories",
    description = "Retrieves a list of all book categories"
)
@ApiResponse(responseCode = "200", description = "Successfully retrieved category list")

// GET /api/v1/categories/{id}
@Operation(
    summary = "Get category by ID",
    description = "Retrieves a specific category by its unique identifier"
)
@ApiResponse(responseCode = "200", description = "Category found")
@ApiResponse(responseCode = "404", description = "Category not found")

// POST /api/v1/categories
@Operation(
    summary = "Create a new category",
    description = "Creates a new book category"
)
@ApiResponse(responseCode = "201", description = "Category created successfully")
@ApiResponse(responseCode = "400", description = "Invalid input")

// PUT /api/v1/categories/{id}
@Operation(
    summary = "Update a category",
    description = "Updates an existing category"
)
@ApiResponse(responseCode = "200", description = "Category updated successfully")
@ApiResponse(responseCode = "404", description = "Category not found")

// DELETE /api/v1/categories/{id}
@Operation(
    summary = "Delete a category",
    description = "Deletes a category. Fails if category has associated books."
)
@ApiResponse(responseCode = "204", description = "Category deleted successfully")
@ApiResponse(responseCode = "404", description = "Category not found")
@ApiResponse(responseCode = "409", description = "Category has associated books - cannot delete")
```

**WHY Each Reference Matters**:
- `CategoryApiController.java` - Must understand current structure, especially the conflict handling in DELETE

**Acceptance Criteria**:

**Automated Verification (Terminal):**
```bash
cd trantantai

# Verify annotations added
grep -c "@Tag" src/main/java/trantantai/trantantai/controllers/CategoryApiController.java
# Assert: Returns 1

grep -c "@Operation" src/main/java/trantantai/trantantai/controllers/CategoryApiController.java
# Assert: Returns 5

mvn compile
# Assert: BUILD SUCCESS
```

**Evidence to Capture:**
- [ ] grep counts for @Tag, @Operation
- [ ] mvn compile output

**Commit**: YES (groups with Task 2, 3)
- Message: `docs(swagger): add OpenAPI annotations to CategoryApiController`
- Files: `trantantai/src/main/java/trantantai/trantantai/controllers/CategoryApiController.java`
- Pre-commit: `mvn compile`

---

### - [ ] 5. Update SecurityConfig for Swagger UI Access (Authenticated)

**What to do**:
- Add Swagger UI static resource paths to security configuration
- Permit access to CSS/JS resources for Swagger UI
- Keep Swagger UI behind authentication (authenticated users only)
- Ensure /v3/api-docs is also accessible to authenticated users

**Must NOT do**:
- Don't make Swagger UI publicly accessible
- Don't change existing authorization rules
- Don't modify OAuth2 configuration
- Don't disable CSRF (Swagger UI handles it)

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Small security config update
- **Skills**: None needed

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 3 (with Task 6)
- **Blocks**: Task 7
- **Blocked By**: Task 2 (need OpenApiConfig first)

**References**:

**Pattern References**:
- `trantantai/src/main/java/trantantai/trantantai/config/SecurityConfig.java:28-52` - Current authorization rules

**Changes to Make:**

```java
// Add these paths to permitAll for static resources only
// The actual Swagger UI page will still require authentication

.authorizeHttpRequests(auth -> auth
    // Existing public endpoints...
    .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/error", "/errors/**").permitAll()
    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
    
    // ADD: Swagger UI static resources (CSS, JS, images) - must be public for UI to render
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
    // Note: This permits the API docs JSON but user still needs to login 
    // to execute API calls since /api/** requires authentication
    
    // ... rest of existing rules
)
```

**WHY Each Reference Matters**:
- `SecurityConfig.java` - Must understand current security rules to add Swagger paths correctly without breaking existing auth

**Acceptance Criteria**:

**Automated Verification (Terminal):**
```bash
cd trantantai

# Verify paths added
grep -E "swagger-ui|v3/api-docs" src/main/java/trantantai/trantantai/config/SecurityConfig.java
# Assert: Shows requestMatchers with swagger paths

mvn compile
# Assert: BUILD SUCCESS

# Start app and test unauthenticated access to static resources
mvn spring-boot:run &
sleep 30

# Swagger UI static resources should load
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui/index.html
# Assert: Returns 200 (page loads)

# API calls should still require auth
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/books
# Assert: Returns 302 (redirect to login) or 401/403

pkill -f "spring-boot:run"
```

**Evidence to Capture:**
- [ ] SecurityConfig.java changes
- [ ] HTTP status codes from curl tests

**Commit**: YES
- Message: `security: permit Swagger UI static resources for authenticated access`
- Files: `trantantai/src/main/java/trantantai/trantantai/config/SecurityConfig.java`
- Pre-commit: `mvn compile`

---

### - [ ] 6. Annotate All ViewModels with @Schema

**What to do**:
- Add @Schema annotations to all 4 ViewModel records
- Document each field with description and example values
- Expose validation constraints where applicable

**Files to modify:**
1. `BookGetVm.java` - Add @Schema to record and each field
2. `BookPostVm.java` - Add @Schema to record and each field
3. `CategoryGetVm.java` - Add @Schema to record and each field
4. `CategoryPostVm.java` - Add @Schema to record and each field

**Must NOT do**:
- Don't change field types or names
- Don't add new fields
- Don't modify the `from()` factory methods
- Don't change validation annotations

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Straightforward annotation additions
- **Skills**: None needed

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 3 (with Task 5)
- **Blocks**: Task 8
- **Blocked By**: Task 1

**References**:

**Pattern References**:
- `trantantai/src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java:1-26`
- `trantantai/src/main/java/trantantai/trantantai/viewmodels/BookPostVm.java:1-21`
- `trantantai/src/main/java/trantantai/trantantai/viewmodels/CategoryGetVm.java:1-17`
- `trantantai/src/main/java/trantantai/trantantai/viewmodels/CategoryPostVm.java:1-15`

**Annotations to Add:**

```java
// BookGetVm.java
@Schema(description = "Book response model")
public record BookGetVm(
    @Schema(description = "Book unique identifier", example = "507f1f77bcf86cd799439011")
    String id,
    @Schema(description = "Book title", example = "Clean Code")
    String title,
    @Schema(description = "Book author", example = "Robert C. Martin")
    String author,
    @Schema(description = "Book price in USD", example = "29.99")
    Double price,
    @Schema(description = "Category ID", example = "507f1f77bcf86cd799439012")
    String categoryId,
    @Schema(description = "Category name", example = "Programming")
    String categoryName
) { ... }

// BookPostVm.java
@Schema(description = "Book creation/update request model")
public record BookPostVm(
    @Schema(description = "Book title", example = "Clean Code", required = true)
    String title,
    @Schema(description = "Book author", example = "Robert C. Martin", required = true)
    String author,
    @Schema(description = "Book price in USD", example = "29.99", required = true)
    Double price,
    @Schema(description = "Category ID (must exist)", example = "507f1f77bcf86cd799439012", required = true)
    String categoryId
) { ... }

// CategoryGetVm.java
@Schema(description = "Category response model")
public record CategoryGetVm(
    @Schema(description = "Category unique identifier", example = "507f1f77bcf86cd799439012")
    String id,
    @Schema(description = "Category name", example = "Programming")
    String name
) { ... }

// CategoryPostVm.java
@Schema(description = "Category creation/update request model")
public record CategoryPostVm(
    @Schema(description = "Category name", example = "Programming", required = true)
    String name
) { ... }
```

**WHY Each Reference Matters**:
- ViewModels are Java Records - @Schema on records requires placing annotation before `public record`

**Acceptance Criteria**:

**Automated Verification (Terminal):**
```bash
cd trantantai

# Verify @Schema added to all 4 files
for f in BookGetVm BookPostVm CategoryGetVm CategoryPostVm; do
  grep -c "@Schema" src/main/java/trantantai/trantantai/viewmodels/${f}.java
done
# Assert: Each returns at least 1

mvn compile
# Assert: BUILD SUCCESS

# Verify schemas appear in OpenAPI spec
mvn spring-boot:run &
sleep 30
curl -s http://localhost:8080/v3/api-docs | grep -o '"BookGetVm"\|"BookPostVm"\|"CategoryGetVm"\|"CategoryPostVm"' | sort -u
# Assert: All 4 schemas listed
pkill -f "spring-boot:run"
```

**Evidence to Capture:**
- [ ] @Schema counts for each file
- [ ] OpenAPI spec showing all 4 schema names

**Commit**: YES
- Message: `docs(swagger): add @Schema annotations to all ViewModels`
- Files: `trantantai/src/main/java/trantantai/trantantai/viewmodels/*.java`
- Pre-commit: `mvn compile`

---

### - [ ] 7. Replace API Demo Section with Swagger UI Link Button

**What to do**:
- Remove the entire API Demo section (lines 136-243) from book/list.html
- Add a simple, styled button that links to /swagger-ui.html
- Place button in a similar location for easy access
- Use Bootstrap styling consistent with existing UI

**Must NOT do**:
- Don't change any other parts of the template
- Don't modify the book list functionality
- Don't add JavaScript for the new button (just a simple link)
- Don't change pagination or other features

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: HTML template modification only
- **Skills**: None needed

**Parallelization**:
- **Can Run In Parallel**: NO
- **Parallel Group**: Wave 4 (after controllers and security done)
- **Blocks**: Task 8
- **Blocked By**: Tasks 3, 4, 5

**References**:

**Pattern References**:
- `trantantai/src/main/resources/templates/book/list.html:136-243` - API Demo section to remove
- `trantantai/src/main/resources/templates/book/list.html:143-163` - Button styling pattern to follow

**HTML to Replace With:**

```html
<!-- Swagger API Documentation Link -->
<div class="swagger-link mt-5 mb-4">
    <h4>API Documentation</h4>
    <p class="text-muted">Explore and test the REST API using Swagger UI</p>
    <a href="/swagger-ui.html" class="btn btn-primary" target="_blank">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-code-slash me-2" viewBox="0 0 16 16">
            <path d="M10.478 1.647a.5.5 0 1 0-.956-.294l-4 13a.5.5 0 0 0 .956.294zM4.854 4.146a.5.5 0 0 1 0 .708L1.707 8l3.147 3.146a.5.5 0 0 1-.708.708l-3.5-3.5a.5.5 0 0 1 0-.708l3.5-3.5a.5.5 0 0 1 .708 0m6.292 0a.5.5 0 0 0 0 .708L14.293 8l-3.147 3.146a.5.5 0 0 0 .708.708l3.5-3.5a.5.5 0 0 0 0-.708l-3.5-3.5a.5.5 0 0 0-.708 0"/>
        </svg>
        Open Swagger UI
    </a>
</div>
```

**WHY Each Reference Matters**:
- `list.html:136-243` - Exact location and content to remove
- `list.html:143-163` - Button styling pattern for consistency

**Acceptance Criteria**:

**Automated Verification (Browser via Playwright):**
```
# Agent executes via playwright browser automation:
1. Navigate to: http://localhost:8080/login
2. Fill: input[name="username"] with test credentials
3. Fill: input[name="password"] with test credentials
4. Click: button[type="submit"]
5. Navigate to: http://localhost:8080/books
6. Assert: Element with text "Open Swagger UI" exists
7. Assert: Old API Demo buttons ("Tải sách từ API") NOT present
8. Click: link "Open Swagger UI"
9. Wait for: New tab/window with Swagger UI
10. Assert: Swagger UI page loads with "TranTanTai Book Store API" title
11. Screenshot: .sisyphus/evidence/task-7-swagger-link.png
```

**Automated Verification (Terminal):**
```bash
cd trantantai

# Verify old API Demo section removed
grep -c "api-demo\|Tải sách từ API\|api-result" src/main/resources/templates/book/list.html
# Assert: Returns 0 (no matches)

# Verify new button added
grep -c "swagger-ui.html\|Open Swagger UI" src/main/resources/templates/book/list.html
# Assert: Returns at least 1
```

**Evidence to Capture:**
- [ ] grep showing old content removed
- [ ] grep showing new button added
- [ ] Screenshot of book list page with Swagger button

**Commit**: YES
- Message: `feat(ui): replace API Demo with Swagger UI link button`
- Files: `trantantai/src/main/resources/templates/book/list.html`
- Pre-commit: None (HTML only)

---

### - [ ] 8. End-to-End Verification and Final Cleanup

**What to do**:
- Start application and perform full verification
- Test all Swagger UI features
- Verify authentication flow
- Test "Try It Out" on at least 2 endpoints
- Capture evidence screenshots
- Clean up any temporary files

**Must NOT do**:
- Don't make additional code changes
- Don't expand scope
- Don't add new features

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Verification task only
- **Skills**: [`playwright`]
  - `playwright`: For browser-based verification

**Parallelization**:
- **Can Run In Parallel**: NO
- **Parallel Group**: Wave 5 (final)
- **Blocks**: None
- **Blocked By**: Tasks 6, 7

**References**:

All previous task outputs as evidence.

**Acceptance Criteria**:

**Full E2E Verification Checklist:**

```
APPLICATION STARTUP:
[ ] mvn spring-boot:run completes without errors
[ ] No ClassNotFoundException in logs
[ ] No NoSuchMethodError in logs
[ ] Application available on port 8080

SWAGGER UI ACCESS:
[ ] Navigate to /swagger-ui.html
[ ] If not logged in, redirects to /login
[ ] After login, Swagger UI loads
[ ] Page title shows "TranTanTai Book Store API"
[ ] API version shows "v1.0.0"

ENDPOINTS VISIBLE:
[ ] "Books" tag visible with 6 endpoints
[ ] "Categories" tag visible with 5 endpoints
[ ] Each endpoint shows correct HTTP method
[ ] Each endpoint shows correct path

SCHEMAS VISIBLE:
[ ] BookGetVm schema visible with all fields
[ ] BookPostVm schema visible with all fields
[ ] CategoryGetVm schema visible with all fields
[ ] CategoryPostVm schema visible with all fields

TRY IT OUT:
[ ] Click "Try it out" on GET /api/v1/books
[ ] Click "Execute"
[ ] Response shows 200 with book list JSON
[ ] Try GET /api/v1/categories
[ ] Response shows 200 with category list JSON

UI CHANGES:
[ ] Navigate to /books
[ ] "Open Swagger UI" button visible
[ ] Old "Tải sách từ API" button NOT present
[ ] Button click opens Swagger UI

BROWSER CONSOLE:
[ ] No JavaScript errors
[ ] No 404 errors for CSS/JS
```

**Automated Verification (Browser via Playwright):**
```
# Full E2E test sequence:
1. Start fresh browser
2. Navigate to http://localhost:8080/swagger-ui.html
3. Verify redirect to /login
4. Login with test credentials
5. After redirect, verify Swagger UI loads
6. Screenshot: .sisyphus/evidence/e2e-swagger-home.png
7. Expand "Books" section
8. Click on GET /api/v1/books
9. Click "Try it out"
10. Click "Execute"
11. Assert: Response code 200
12. Screenshot: .sisyphus/evidence/e2e-try-it-out.png
13. Navigate to http://localhost:8080/books
14. Assert: "Open Swagger UI" button visible
15. Screenshot: .sisyphus/evidence/e2e-book-list.png
```

**Evidence to Capture:**
- [ ] Application startup logs (clean)
- [ ] Screenshot: Swagger UI home page
- [ ] Screenshot: Try It Out successful response
- [ ] Screenshot: Book list page with Swagger button
- [ ] Browser console (no errors)

**Commit**: NO (verification only)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 0 | (no commit - spike) | - | - |
| 1 | `build(deps): add springdoc-openapi 2.8.15 for Swagger UI` | pom.xml | mvn compile |
| 2, 3, 4 | `feat(swagger): add OpenAPI configuration and controller annotations` | OpenApiConfig.java, BookApiController.java, CategoryApiController.java | mvn compile |
| 5 | `security: permit Swagger UI static resources for authenticated access` | SecurityConfig.java | mvn compile |
| 6 | `docs(swagger): add @Schema annotations to all ViewModels` | viewmodels/*.java | mvn compile |
| 7 | `feat(ui): replace API Demo with Swagger UI link button` | book/list.html | - |

---

## Success Criteria

### Verification Commands
```bash
# Full verification sequence
cd trantantai

# 1. Clean build
mvn clean compile
# Expected: BUILD SUCCESS

# 2. Run tests
mvn test
# Expected: All tests pass

# 3. Start application
mvn spring-boot:run &
sleep 30

# 4. Check OpenAPI spec
curl -s http://localhost:8080/v3/api-docs | jq '.info.title, .info.version'
# Expected: "TranTanTai Book Store API", "v1.0.0"

# 5. Count endpoints
curl -s http://localhost:8080/v3/api-docs | jq '.paths | keys | length'
# Expected: At least 7 (unique paths)

# 6. Check schemas
curl -s http://localhost:8080/v3/api-docs | jq '.components.schemas | keys'
# Expected: Contains BookGetVm, BookPostVm, CategoryGetVm, CategoryPostVm

# 7. Stop app
pkill -f "spring-boot:run"
```

### Final Checklist
- [ ] All "Must Have" requirements met
- [ ] All "Must NOT Have" guardrails respected
- [ ] All 8 tasks completed
- [ ] All commits made with proper messages
- [ ] Evidence captured for each verification step
