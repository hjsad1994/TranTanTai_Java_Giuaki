# Swagger API Documentation Update

## TL;DR

> **Quick Summary**: Add Swagger annotations to WishlistApiController (7 endpoints) and MoMoController (1 endpoint), update application.properties to expose `/admin/api/**` paths, and enhance OpenApiConfig description.
> 
> **Deliverables**:
> - WishlistApiController with full Swagger documentation
> - MoMoController with Swagger documentation
> - Updated application.properties with expanded path matching
> - Enhanced OpenApiConfig description covering all API groups
> 
> **Estimated Effort**: Quick (< 1 hour)
> **Parallel Execution**: YES - 4 waves
> **Critical Path**: Wave 1 (all modifications) → Wave 2 (verification)

---

## Context

### Original Request
Kiểm tra toàn bộ API backend hiện có và cập nhật vào Swagger UI.

### Interview Summary
**Key Discussions**:
- Security Annotations: KHÔNG - giữ consistent với các controller hiện có
- Detail Level: STANDARD - giống BookApiController (có @Tag, @Operation, @ApiResponse, @Parameter)
- OpenApiConfig: CÓ - cập nhật description để cover tất cả APIs
- Verification: BUILD để verify compile thành công

**Research Findings**:
- WishlistApiController: 7 endpoints, chưa có Swagger annotations
- MoMoController: 1 endpoint (IPN callback), chưa có Swagger annotations
- Current path config only scans `/api/**`, missing `/admin/api/**`
- BookApiController provides the standard pattern to follow

### Metis Review
**Identified Gaps** (addressed):
- `@AuthenticationPrincipal User user` params: Will add `@Parameter(hidden = true)` to hide from Swagger
- MoMo IPN returns 204: Will document correctly with `@ApiResponse(responseCode = "204")`
- ReportApiController visibility: The path fix will expose previously hidden admin APIs (bonus!)
- OpenApiConfig description: Will include all API categories explicitly

---

## Work Objectives

### Core Objective
Add comprehensive Swagger documentation to undocumented REST API controllers and update configuration to expose all API endpoints in Swagger UI.

### Concrete Deliverables
- `WishlistApiController.java` with @Tag, @Operation, @ApiResponse, @Parameter annotations
- `MoMoController.java` with @Tag, @Operation, @ApiResponse annotations
- `application.properties` with updated `springdoc.paths-to-match`
- `OpenApiConfig.java` with comprehensive description

### Definition of Done
- [ ] `./mvnw compile -f trantantai/pom.xml` passes without errors
- [ ] All 4 files modified with correct annotations/configuration

### Must Have
- @Tag annotation on class level for both controllers
- @Operation annotation with summary and description for each endpoint
- @ApiResponse annotations for all response codes
- @Parameter annotations for path variables
- `@Parameter(hidden = true)` for @AuthenticationPrincipal User params
- Updated springdoc.paths-to-match to include `/admin/api/**`

### Must NOT Have (Guardrails)
- NO @SecurityRequirement annotations (keep consistent with existing controllers)
- NO @Schema annotations on ViewModels (BookGetVm, MoMoIpnRequest, etc.)
- NO modifications to already-documented controllers (BookApiController, CategoryApiController, etc.)
- NO modifications to MVC-only controllers (CartController, BookController)
- NO standardizing/fixing response codes across existing controllers
- NO adding test files (build verification only)

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: YES (Maven build)
- **User wants tests**: NO - Build verification only
- **Framework**: Maven compile

### Automated Verification

**Build Verification:**
```bash
# Agent runs from trantantai directory:
cd trantantai && ./mvnw compile -q
# Assert: Exit code 0
# Assert: No compilation errors in output
```

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately - All Independent):
├── Task 1: Add Swagger annotations to WishlistApiController
├── Task 2: Add Swagger annotations to MoMoController
├── Task 3: Update application.properties paths
└── Task 4: Update OpenApiConfig description

Wave 2 (After Wave 1):
└── Task 5: Build verification

Critical Path: Any Wave 1 task → Task 5
Parallel Speedup: ~75% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 5 | 2, 3, 4 |
| 2 | None | 5 | 1, 3, 4 |
| 3 | None | 5 | 1, 2, 4 |
| 4 | None | 5 | 1, 2, 3 |
| 5 | 1, 2, 3, 4 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Agents |
|------|-------|-------------------|
| 1 | 1, 2, 3, 4 | All run in parallel with category="quick" |
| 2 | 5 | Sequential verification |

---

## TODOs

- [ ] 1. Add Swagger Annotations to WishlistApiController

  **What to do**:
  - Add import statements for Swagger annotations
  - Add `@Tag(name = "Wishlist", description = "Wishlist management APIs - Add, remove, and manage user wishlists")` at class level
  - Add `@Operation`, `@ApiResponse`, `@Parameter` to each of 7 endpoints
  - Add `@Parameter(hidden = true)` to all `@AuthenticationPrincipal User user` parameters
  - Document 401 responses for authenticated endpoints
  - Document 200 responses with descriptions

  **Must NOT do**:
  - Add @SecurityRequirement annotations
  - Add @Schema annotations
  - Modify JavaDoc comments
  - Change any business logic

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single file modification with clear patterns to follow
  - **Skills**: None needed
    - Standard Java annotations, no specialized tooling required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4)
  - **Blocks**: Task 5
  - **Blocked By**: None (can start immediately)

  **References**:
  
  **Pattern References** (existing code to follow):
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java:23-27` - @Tag annotation pattern
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java:38-49` - @Operation, @ApiResponse for GET list endpoint
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java:52-64` - @Operation, @ApiResponses, @Parameter for GET by ID
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java:134-148` - DELETE endpoint pattern
  
  **File to Modify**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/WishlistApiController.java` - Target file with 7 endpoints

  **Endpoints to Annotate**:
  | Method | Path | Summary | Responses |
  |--------|------|---------|-----------|
  | GET | / | Get user wishlist | 200 (list), 401 (unauthorized) |
  | POST | /{bookId} | Add book to wishlist | 200 (success), 401 (unauthorized) |
  | DELETE | /{bookId} | Remove book from wishlist | 200 (success), 401 (unauthorized) |
  | POST | /{bookId}/toggle | Toggle book in wishlist | 200 (success), 401 (unauthorized) |
  | GET | /{bookId}/check | Check if book in wishlist | 200 (status) |
  | GET | /count | Get wishlist count | 200 (count) |
  | DELETE | / | Clear wishlist | 200 (success), 401 (unauthorized) |

  **Acceptance Criteria**:
  - [ ] Import statements added: `io.swagger.v3.oas.annotations.Operation`, `io.swagger.v3.oas.annotations.Parameter`, `io.swagger.v3.oas.annotations.responses.ApiResponse`, `io.swagger.v3.oas.annotations.responses.ApiResponses`, `io.swagger.v3.oas.annotations.tags.Tag`
  - [ ] `@Tag` annotation present on class
  - [ ] All 7 methods have `@Operation` annotation
  - [ ] All `@AuthenticationPrincipal User user` parameters have `@Parameter(hidden = true)`
  - [ ] All `@PathVariable String bookId` parameters have `@Parameter(description = "Book ID", required = true)`
  - [ ] File compiles: `javac` syntax check passes

  **Commit**: YES
  - Message: `docs(swagger): add Swagger annotations to WishlistApiController`
  - Files: `trantantai/src/main/java/trantantai/trantantai/controllers/WishlistApiController.java`

---

- [ ] 2. Add Swagger Annotations to MoMoController

  **What to do**:
  - Add import statements for Swagger annotations
  - Add `@Tag(name = "MoMo Payment", description = "MoMo payment gateway integration - IPN callback handling")` at class level
  - Add `@Operation` and `@ApiResponse` to the IPN endpoint
  - Document 204 No Content response (MoMo expects this)

  **Must NOT do**:
  - Add @SecurityRequirement annotations
  - Add @Schema annotations to MoMoIpnRequest
  - Modify the endpoint logic
  - Change System.out.println statements

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single file, single endpoint modification
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3, 4)
  - **Blocks**: Task 5
  - **Blocked By**: None (can start immediately)

  **References**:
  
  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java:23-27` - @Tag pattern
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java:77-100` - POST endpoint pattern

  **File to Modify**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/MoMoController.java` - Target file with 1 endpoint

  **Endpoint to Annotate**:
  | Method | Path | Summary | Responses |
  |--------|------|---------|-----------|
  | POST | /ipn | MoMo IPN callback | 204 (processed successfully) |

  **Acceptance Criteria**:
  - [ ] Import statements added for Swagger annotations
  - [ ] `@Tag(name = "MoMo Payment", ...)` present on class
  - [ ] `@Operation(summary = "Handle MoMo IPN callback", description = "Receives payment notification from MoMo gateway and updates order payment status")` on handleIpn method
  - [ ] `@ApiResponse(responseCode = "204", description = "IPN processed successfully")` present
  - [ ] File compiles without errors

  **Commit**: YES
  - Message: `docs(swagger): add Swagger annotations to MoMoController`
  - Files: `trantantai/src/main/java/trantantai/trantantai/controllers/MoMoController.java`

---

- [ ] 3. Update application.properties Swagger Path Configuration

  **What to do**:
  - Update `springdoc.paths-to-match` from `/api/**` to `/api/**,/admin/api/**`
  - This will expose ReportApiController and InventoryApiController endpoints in Swagger UI

  **Must NOT do**:
  - Modify any other properties
  - Change swagger-ui.path or api-docs.path
  - Add new springdoc properties

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single line change in config file
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 4)
  - **Blocks**: Task 5
  - **Blocked By**: None (can start immediately)

  **References**:
  
  **File to Modify**:
  - `trantantai/src/main/resources/application.properties` - Line 28

  **Change Required**:
  ```properties
  # Before:
  springdoc.paths-to-match=/api/**
  
  # After:
  springdoc.paths-to-match=/api/**,/admin/api/**
  ```

  **Acceptance Criteria**:
  - [ ] Line 28 updated to: `springdoc.paths-to-match=/api/**,/admin/api/**`
  - [ ] No other properties modified
  - [ ] File syntax valid (no trailing spaces or encoding issues)

  **Commit**: YES
  - Message: `config(swagger): expand paths-to-match to include admin API endpoints`
  - Files: `trantantai/src/main/resources/application.properties`

---

- [ ] 4. Update OpenApiConfig Description

  **What to do**:
  - Update the `description` field in `@OpenAPIDefinition` to mention all API categories
  - New description should cover: Books, Categories, Reviews, Images, Wishlists, Payments, Inventory, Reports

  **Must NOT do**:
  - Change the title, version, or contact info
  - Add new servers
  - Add security schemes
  - Add any Java code or beans

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single string update in config annotation
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3)
  - **Blocks**: Task 5
  - **Blocked By**: None (can start immediately)

  **References**:
  
  **File to Modify**:
  - `trantantai/src/main/java/trantantai/trantantai/config/OpenApiConfig.java` - Lines 14-15

  **Change Required**:
  ```java
  // Before:
  description = "REST API documentation for TranTanTai Book Store application. " +
                "Provides endpoints for managing books and categories.",
  
  // After:
  description = "REST API documentation for TranTanTai Book Store application. " +
                "Provides endpoints for: Books, Categories, Reviews, Images, Wishlists, " +
                "Payments (MoMo), Inventory management, and Admin Reports.",
  ```

  **Acceptance Criteria**:
  - [ ] Description updated to include all API categories
  - [ ] Mentions: Books, Categories, Reviews, Images, Wishlists, Payments, Inventory, Reports
  - [ ] Java string syntax valid (proper escaping and concatenation)
  - [ ] File compiles without errors

  **Commit**: YES
  - Message: `docs(swagger): update OpenApiConfig description to cover all API groups`
  - Files: `trantantai/src/main/java/trantantai/trantantai/config/OpenApiConfig.java`

---

- [ ] 5. Build Verification

  **What to do**:
  - Run Maven compile to verify all changes are syntactically correct
  - Ensure no compilation errors from the Swagger annotations

  **Must NOT do**:
  - Run tests (user requested build only)
  - Start the application
  - Make any file modifications

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single command verification
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 2 (sequential)
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 1, 2, 3, 4

  **References**:
  
  **Verification Command**:
  - Working directory: `trantantai/`
  - Command: `./mvnw compile -q` (or `mvnw.cmd compile -q` on Windows)

  **Acceptance Criteria**:
  - [ ] Command executed: `./mvnw compile` (or `mvnw.cmd compile` on Windows)
  - [ ] Exit code: 0
  - [ ] No `[ERROR]` messages in output
  - [ ] All modified Java files compile successfully

  **Commit**: NO (verification only)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `docs(swagger): add Swagger annotations to WishlistApiController` | WishlistApiController.java | N/A |
| 2 | `docs(swagger): add Swagger annotations to MoMoController` | MoMoController.java | N/A |
| 3 | `config(swagger): expand paths-to-match to include admin API endpoints` | application.properties | N/A |
| 4 | `docs(swagger): update OpenApiConfig description to cover all API groups` | OpenApiConfig.java | N/A |
| 5 | - | - | ./mvnw compile |

**Alternative**: Single commit after all tasks:
- Message: `docs(swagger): complete API documentation for Wishlist and MoMo controllers`
- Files: All 4 modified files
- After: Task 5 verification passes

---

## Success Criteria

### Verification Commands
```bash
# From trantantai directory
./mvnw compile -q  # Expected: Exit code 0, no errors
```

### Final Checklist
- [ ] WishlistApiController has @Tag and all 7 endpoints documented
- [ ] MoMoController has @Tag and 1 endpoint documented
- [ ] application.properties includes `/admin/api/**` path
- [ ] OpenApiConfig description mentions all API categories
- [ ] Maven compile passes without errors
- [ ] NO @SecurityRequirement annotations added
- [ ] NO @Schema annotations added
- [ ] NO modifications to already-documented controllers
