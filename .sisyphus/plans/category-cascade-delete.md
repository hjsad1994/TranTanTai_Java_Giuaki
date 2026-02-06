# Category Cascade Delete & Admin Access Verification

## TL;DR

> **Quick Summary**: Implement cascade deletion for categories (delete all books when category is deleted), add enhanced warning messages showing affected book count, and verify admin cannot access cart.
> 
> **Deliverables**:
> - Modified CategoryService with cascade delete logic
> - Updated CategoryApiController to cascade delete instead of blocking
> - Updated CategoryController and AdminController with cascade delete
> - Enhanced warning dialogs showing book count in both admin and user templates
> - Verification that admin cart access is properly blocked
> 
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: Task 1 → Task 2 → Task 3 → Task 6

---

## Context

### Original Request
Vietnamese translated: "ultrawork modify the APIs and interfaces to ensure they handle everything correctly:
1. Admin should not access shopping cart
2. Add/Edit/Delete functionality is correct
3. IMPORTANT: When deleting a category, show a warning that it will affect products, and when deleting a category, delete all related products"

### Interview Summary
**Key Discussions**:
- User explicitly wants CASCADE delete (delete all books), not blocking behavior
- Warning should show how many books will be affected
- Admin cart access needs verification

**Research Findings**:
- CategoryService.deleteCategoryById: Just calls `categoryRepository.deleteById(id)` - NO cascade logic
- CategoryApiController: Returns 409 CONFLICT if books exist (WRONG behavior per user)
- CategoryController/AdminController: No book checking, potential orphan books
- IBookRepository: Has `findByCategoryId(String)` method - can find books
- Admin layout: NO cart links (already clean)
- Security config: `/cart/**` requires `hasRole("USER")` - CORRECT

### Self-Analysis (Metis-style Gap Analysis)

**Identified Gaps (addressed)**:
1. **Edge case - Empty category**: Already handled - just deletes normally
2. **Transaction safety**: Should add @Transactional to ensure atomic cascade
3. **API response format**: Need to return count of deleted books for transparency
4. **Book count loading**: Need to pass book count to templates for dynamic warning

---

## Work Objectives

### Core Objective
Implement proper cascade deletion for categories that:
1. Shows a warning with the exact number of books that will be deleted
2. Deletes all books in the category first, then deletes the category
3. Works consistently across REST API, Web Controller, and Admin Controller

### Concrete Deliverables
- `CategoryService.java` - New method `deleteCategoryWithBooks(String id)` returning deleted book count
- `BookService.java` - New method `deleteBooksByCategoryId(String categoryId)` returning count
- `IBookRepository.java` - New method `deleteByCategoryId(String categoryId)`
- `CategoryApiController.java` - Modified DELETE to cascade and return book count
- `CategoryController.java` - Modified delete to use cascade service
- `AdminController.java` - Modified delete to use cascade service, pass book counts to template
- `admin/categories/list.html` - Enhanced warning with dynamic book count
- `category/list.html` - Enhanced warning with dynamic book count

### Definition of Done
- [ ] `curl -X DELETE /api/v1/categories/{id}` deletes category AND all its books
- [ ] Admin panel category delete shows "This will delete X books"
- [ ] Category controller delete shows "This will delete X books"
- [ ] After deletion, no orphaned books remain in database
- [ ] Admin cannot access /cart (returns 403)

### Must Have
- Cascade delete functionality (delete books first, then category)
- Book count in warning messages (dynamic, not static text)
- Atomic operation (transaction safety)

### Must NOT Have (Guardrails)
- DO NOT modify security configuration (already correct)
- DO NOT add cart links to admin layout
- DO NOT change existing Book CRUD operations
- DO NOT add soft delete (user wants permanent deletion)
- DO NOT add confirmation modals (keep using native confirm())

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: YES (test files exist but have import errors - not usable)
- **User wants tests**: Not explicitly requested
- **Framework**: Spring Boot Test (exists but broken)
- **QA approach**: Manual-only verification

### Manual Verification Procedures

**For Backend API changes** (using curl/httpie):
```bash
# Test cascade delete via API
# 1. Create test category
curl -X POST http://localhost:8080/api/v1/categories -H "Content-Type: application/json" -d '{"name":"Test Category"}' -u admin:password

# 2. Create books in that category (get categoryId from step 1)
curl -X POST http://localhost:8080/api/v1/books -H "Content-Type: application/json" -d '{"title":"Test Book","author":"Test","price":10,"categoryId":"<id>"}' -u admin:password

# 3. Delete category (should cascade delete books)
curl -X DELETE http://localhost:8080/api/v1/categories/<id> -u admin:password

# 4. Verify books are deleted
curl http://localhost:8080/api/v1/books -u admin:password
# Expected: No books with the deleted categoryId
```

**For Frontend/UI changes** (using playwright skill):
```
# Agent executes via playwright browser automation:
1. Navigate to: http://localhost:8080/login
2. Login as admin user
3. Navigate to: http://localhost:8080/admin/categories
4. Hover over delete button for a category with books
5. Verify delete confirmation text includes book count
6. Screenshot: .sisyphus/evidence/task-5-delete-warning.png
```

**For Admin cart access verification**:
```bash
# Login as admin and try to access cart
curl -X GET http://localhost:8080/cart -u admin:password
# Expected: 403 Forbidden
```

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: Add repository method deleteByCategoryId (no dependencies)
├── Task 4: Update admin/categories/list.html template (no code dependencies)
└── Task 5: Update category/list.html template (no code dependencies)

Wave 2 (After Wave 1):
├── Task 2: Add BookService.deleteBooksByCategoryId (depends: Task 1)
└── Task 3: Add CategoryService.deleteCategoryWithBooks (depends: Task 2)

Wave 3 (After Wave 2):
├── Task 6: Update CategoryApiController (depends: Task 3)
├── Task 7: Update CategoryController (depends: Task 3)
└── Task 8: Update AdminController (depends: Task 3)

Wave 4 (Final Verification):
└── Task 9: Verify admin cart access blocked (no code dependencies)

Critical Path: Task 1 → Task 2 → Task 3 → Task 6
Parallel Speedup: ~35% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2 | 4, 5 |
| 2 | 1 | 3 | 4, 5 |
| 3 | 2 | 6, 7, 8 | 4, 5 |
| 4 | None | None | 1, 5 |
| 5 | None | None | 1, 4 |
| 6 | 3 | 9 | 7, 8 |
| 7 | 3 | 9 | 6, 8 |
| 8 | 3 | 9 | 6, 7 |
| 9 | 6, 7, 8 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Agents |
|------|-------|-------------------|
| 1 | 1, 4, 5 | 3x parallel quick tasks |
| 2 | 2, 3 | Sequential - service layer changes |
| 3 | 6, 7, 8 | 3x parallel controller updates |
| 4 | 9 | Single verification task |

---

## TODOs

- [ ] 1. Add deleteByCategoryId method to IBookRepository

  **What to do**:
  - Add Spring Data MongoDB derived query method: `void deleteByCategoryId(String categoryId)`
  - This enables bulk deletion of books by category

  **Must NOT do**:
  - Do not add @Query annotation (derived query is cleaner)
  - Do not change existing methods

  **Recommended Agent Profile**:
  - **Category**: `quick` - Single file, simple addition
    - Reason: Trivial one-line interface method addition
  - **Skills**: None needed
    - No complex logic, just Spring Data interface convention

  **Skills Evaluated but Omitted**:
  - `typescript-programmer`: Wrong language (Java)
  - `git-master`: Not needed for code change

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 4, 5)
  - **Blocks**: Task 2
  - **Blocked By**: None (can start immediately)

  **References**:
  
  **Pattern References**:
  - `IBookRepository.java:15` - Existing `findByCategoryId` method pattern to follow

  **API/Type References**:
  - Spring Data MongoDB documentation - derived delete query methods

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify method exists in compiled class
  grep -n "deleteByCategoryId" trantantai/src/main/java/trantantai/trantantai/repositories/IBookRepository.java
  # Expected: Line containing "void deleteByCategoryId(String categoryId)"
  ```

  **Commit**: YES
  - Message: `feat(repository): add deleteByCategoryId method to IBookRepository`
  - Files: `IBookRepository.java`
  - Pre-commit: Maven compile check

---

- [ ] 2. Add deleteBooksByCategoryId method to BookService

  **What to do**:
  - Add method: `public int deleteBooksByCategoryId(String categoryId)`
  - Use `bookRepository.findByCategoryId(categoryId)` to get count first
  - Then call `bookRepository.deleteByCategoryId(categoryId)` to delete
  - Return the count of deleted books
  - Add `@Transactional` annotation for atomicity

  **Must NOT do**:
  - Do not iterate and delete one by one (use bulk delete)
  - Do not change existing deleteBookById method

  **Recommended Agent Profile**:
  - **Category**: `quick` - Simple service method
    - Reason: Single method addition with clear logic
  - **Skills**: None needed
    - Standard Spring service pattern

  **Skills Evaluated but Omitted**:
  - `typescript-programmer`: Wrong language

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 2 (sequential)
  - **Blocks**: Task 3
  - **Blocked By**: Task 1

  **References**:
  
  **Pattern References**:
  - `BookService.java:64-66` - Existing `deleteBookById` method pattern
  - `BookService.java:29-38` - Pattern for using repository with return value

  **API/Type References**:
  - `IBookRepository.java:15` - `findByCategoryId` for counting
  - `IBookRepository.java` - New `deleteByCategoryId` from Task 1

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify method exists
  grep -A 5 "deleteBooksByCategoryId" trantantai/src/main/java/trantantai/trantantai/services/BookService.java
  # Expected: Method with @Transactional annotation and return type int
  ```

  **Commit**: YES
  - Message: `feat(service): add deleteBooksByCategoryId method to BookService`
  - Files: `BookService.java`
  - Pre-commit: Maven compile

---

- [ ] 3. Add deleteCategoryWithBooks method to CategoryService

  **What to do**:
  - Inject BookService into CategoryService
  - Add method: `public int deleteCategoryWithBooks(String id)`
  - First: Call `bookService.deleteBooksByCategoryId(id)` and store count
  - Then: Call `categoryRepository.deleteById(id)`
  - Return: The count of deleted books
  - Add `@Transactional` annotation for atomicity

  **Must NOT do**:
  - Do not modify existing `deleteCategoryById` method (keep for backward compatibility)
  - Do not add validation for category existence here (controllers will handle)

  **Recommended Agent Profile**:
  - **Category**: `quick` - Service layer method
    - Reason: Single method with clear cascade logic
  - **Skills**: None needed
    - Standard Spring service pattern

  **Skills Evaluated but Omitted**:
  - `typescript-programmer`: Wrong language

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 2 (sequential after Task 2)
  - **Blocks**: Tasks 6, 7, 8
  - **Blocked By**: Task 2

  **References**:
  
  **Pattern References**:
  - `CategoryService.java:14-19` - Constructor injection pattern for adding BookService
  - `CategoryService.java:40-42` - Existing delete method to NOT modify

  **API/Type References**:
  - `BookService.java` - New `deleteBooksByCategoryId` method from Task 2

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify method exists
  grep -A 10 "deleteCategoryWithBooks" trantantai/src/main/java/trantantai/trantantai/services/CategoryService.java
  # Expected: Method with @Transactional, calls bookService.deleteBooksByCategoryId, returns int
  ```

  **Commit**: YES
  - Message: `feat(service): add deleteCategoryWithBooks cascade method to CategoryService`
  - Files: `CategoryService.java`
  - Pre-commit: Maven compile

---

- [ ] 4. Update admin/categories/list.html with dynamic book count warning

  **What to do**:
  - Modify the delete button onclick to show dynamic book count
  - Change the static warning text to include `${category.books != null ? category.books.size() : 0}` books
  - Update confirm message: "Bạn có chắc muốn xóa danh mục này? Điều này sẽ XÓA VĨNH VIỄN X sách trong danh mục này!"
  - Use Thymeleaf inline expression for dynamic text

  **Must NOT do**:
  - Do not add custom modal (keep native confirm)
  - Do not change button styling
  - Do not change URL pattern

  **Recommended Agent Profile**:
  - **Category**: `quick` - Simple template change
    - Reason: Single line modification in HTML
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: For proper Thymeleaf syntax and UX messaging

  **Skills Evaluated but Omitted**:
  - `typescript-programmer`: Not TypeScript
  - `svelte-programmer`: Not Svelte

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 5)
  - **Blocks**: None
  - **Blocked By**: None (can start immediately)

  **References**:
  
  **Pattern References**:
  - `admin/categories/list.html:92-93` - Existing book count display pattern `${category.books != null ? category.books.size() : 0}`
  - `admin/categories/list.html:102-110` - Current delete button to modify

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify dynamic warning text in template
  grep -n "XÓA VĨNH VIỄN" trantantai/src/main/resources/templates/admin/categories/list.html
  # Expected: Line with Thymeleaf expression for book count
  ```

  **Evidence to Capture**:
  - Screenshot of delete confirmation dialog showing book count

  **Commit**: YES
  - Message: `feat(ui): add dynamic book count to admin category delete warning`
  - Files: `admin/categories/list.html`
  - Pre-commit: None (template)

---

- [ ] 5. Update category/list.html with dynamic book count warning

  **What to do**:
  - Similar to Task 4 but for user-facing category list
  - Modify onclick confirm to show book count
  - Need to ensure controller passes book counts to this template

  **Must NOT do**:
  - Do not add complex JavaScript
  - Do not change layout structure

  **Recommended Agent Profile**:
  - **Category**: `quick` - Simple template change
    - Reason: Single line modification in HTML
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: For proper UX messaging

  **Skills Evaluated but Omitted**:
  - `typescript-programmer`: Not TypeScript

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 4)
  - **Blocks**: None
  - **Blocked By**: None

  **References**:
  
  **Pattern References**:
  - `category/list.html:53-60` - Current delete button to modify
  - `admin/categories/list.html` - Reference for book count pattern after Task 4

  **NOTE**: This template doesn't currently show book counts. The CategoryController needs to provide category objects with populated books list, OR we need to add book counts another way. Check if getAllCategories includes book counts.

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify dynamic warning text in template  
  grep -n "XÓA VĨNH VIỄN" trantantai/src/main/resources/templates/category/list.html
  # Expected: Line with book count in warning
  ```

  **Commit**: YES
  - Message: `feat(ui): add dynamic book count to category delete warning`
  - Files: `category/list.html`
  - Pre-commit: None

---

- [ ] 6. Update CategoryApiController to use cascade delete

  **What to do**:
  - Modify the DELETE `/{id}` endpoint
  - Remove the book existence check that returns 409 CONFLICT
  - Call `categoryService.deleteCategoryWithBooks(id)` instead of `deleteCategoryById`
  - Return the count of deleted books in response body
  - Change response from 204 No Content to 200 OK with JSON body: `{"deletedBooks": count}`

  **Must NOT do**:
  - Do not change the endpoint URL
  - Do not change authentication requirements
  - Do not remove the category existence check (404)

  **Recommended Agent Profile**:
  - **Category**: `quick` - Controller method modification
    - Reason: Straightforward method update
  - **Skills**: None needed
    - Standard Spring REST controller pattern

  **Skills Evaluated but Omitted**:
  - `typescript-programmer`: Wrong language

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 7, 8)
  - **Blocks**: Task 9
  - **Blocked By**: Task 3

  **References**:
  
  **Pattern References**:
  - `CategoryApiController.java:100-115` - Current DELETE method to modify
  - `CategoryApiController.java:69-74` - Pattern for returning ResponseEntity with body

  **API/Type References**:
  - `CategoryService.java` - New `deleteCategoryWithBooks` method from Task 3

  **Acceptance Criteria**:

  **Automated Verification (Bash curl)**:
  ```bash
  # Create category, add book, delete category via API
  # 1. Get existing category with books (assumes test data exists)
  CATEGORY_ID=$(curl -s http://localhost:8080/api/v1/categories -u admin:password | jq -r '.[0].id')
  
  # 2. Delete category (should cascade)
  curl -X DELETE "http://localhost:8080/api/v1/categories/$CATEGORY_ID" -u admin:password -v
  # Expected: 200 OK with {"deletedBooks": N}
  ```

  **Commit**: YES
  - Message: `feat(api): implement cascade delete in CategoryApiController`
  - Files: `CategoryApiController.java`
  - Pre-commit: Maven compile

---

- [ ] 7. Update CategoryController to use cascade delete

  **What to do**:
  - Modify the `/delete/{id}` GET mapping
  - Call `categoryService.deleteCategoryWithBooks(id)` instead of `deleteCategoryById`
  - Optionally: Add flash message showing how many books were deleted

  **Must NOT do**:
  - Do not change from GET to POST (would break existing links)
  - Do not add complex redirect logic

  **Recommended Agent Profile**:
  - **Category**: `quick` - Simple method update
    - Reason: One-line service call change
  - **Skills**: None needed

  **Skills Evaluated but Omitted**:
  - All omitted - trivial change

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 6, 8)
  - **Blocks**: Task 9
  - **Blocked By**: Task 3

  **References**:
  
  **Pattern References**:
  - `CategoryController.java:73-78` - Current delete method to modify

  **API/Type References**:
  - `CategoryService.java` - New `deleteCategoryWithBooks` method from Task 3

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify method uses cascade delete
  grep -A 3 "deleteCategory" trantantai/src/main/java/trantantai/trantantai/controllers/CategoryController.java | grep "deleteCategoryWithBooks"
  # Expected: Line calling deleteCategoryWithBooks
  ```

  **Commit**: Groups with Task 8
  - Message: N/A (grouped)
  - Files: `CategoryController.java`

---

- [ ] 8. Update AdminController to use cascade delete

  **What to do**:
  - Modify the `/categories/delete/{id}` GET mapping
  - Call `categoryService.deleteCategoryWithBooks(id)` instead of `deleteCategoryById`
  - Optionally: Add redirect attribute showing deleted book count

  **Must NOT do**:
  - Do not change from GET to POST
  - Do not add admin-specific logic

  **Recommended Agent Profile**:
  - **Category**: `quick` - Simple method update
    - Reason: One-line service call change
  - **Skills**: None needed

  **Skills Evaluated but Omitted**:
  - All omitted - trivial change

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 6, 7)
  - **Blocks**: Task 9
  - **Blocked By**: Task 3

  **References**:
  
  **Pattern References**:
  - `AdminController.java:179-183` - Current delete method to modify

  **API/Type References**:
  - `CategoryService.java` - New `deleteCategoryWithBooks` method from Task 3

  **Acceptance Criteria**:

  **Automated Verification (Bash)**:
  ```bash
  # Verify method uses cascade delete
  grep -A 3 "deleteCategory" trantantai/src/main/java/trantantai/trantantai/controllers/AdminController.java | grep "deleteCategoryWithBooks"
  # Expected: Line calling deleteCategoryWithBooks
  ```

  **Commit**: YES (with Task 7)
  - Message: `feat(controller): implement cascade delete in CategoryController and AdminController`
  - Files: `CategoryController.java`, `AdminController.java`
  - Pre-commit: Maven compile

---

- [ ] 9. Verify admin cannot access cart (verification only)

  **What to do**:
  - Verify security config blocks admin from `/cart/**`
  - Test by attempting to access cart as admin user
  - Document verification result

  **Must NOT do**:
  - Do not modify security configuration
  - Do not modify admin layout (no cart links exist)

  **Recommended Agent Profile**:
  - **Category**: `quick` - Verification task only
    - Reason: No code changes, just verification
  - **Skills**: [`playwright`]
    - `playwright`: For browser automation to test access

  **Skills Evaluated but Omitted**:
  - `frontend-ui-ux`: Not modifying UI

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 4 (final)
  - **Blocks**: None
  - **Blocked By**: Tasks 6, 7, 8 (all implementation complete)

  **References**:
  
  **Pattern References**:
  - `SecurityConfig.java:line with /cart/**` - Security rule to verify

  **Acceptance Criteria**:

  **Automated Verification (Playwright via skill)**:
  ```
  # Agent executes via playwright browser automation:
  1. Navigate to: http://localhost:8080/login
  2. Login with admin credentials
  3. Try to navigate to: http://localhost:8080/cart
  4. Assert: Page shows 403 Forbidden error
  5. Screenshot: .sisyphus/evidence/task-9-admin-cart-blocked.png
  ```

  **Automated Verification (Bash curl)**:
  ```bash
  curl -X GET http://localhost:8080/cart -u admin:password -w "%{http_code}"
  # Expected: 403
  ```

  **Commit**: NO (verification only)

---

## Commit Strategy

| After Task(s) | Message | Files | Verification |
|--------------|---------|-------|--------------|
| 1 | `feat(repository): add deleteByCategoryId to IBookRepository` | IBookRepository.java | mvn compile |
| 2 | `feat(service): add deleteBooksByCategoryId to BookService` | BookService.java | mvn compile |
| 3 | `feat(service): add deleteCategoryWithBooks cascade method` | CategoryService.java | mvn compile |
| 4 | `feat(ui): add dynamic book count to admin category delete warning` | admin/categories/list.html | n/a |
| 5 | `feat(ui): add dynamic book count to category delete warning` | category/list.html | n/a |
| 6 | `feat(api): implement cascade delete in CategoryApiController` | CategoryApiController.java | mvn compile |
| 7, 8 | `feat(controller): implement cascade delete in web controllers` | CategoryController.java, AdminController.java | mvn compile |

---

## Success Criteria

### Verification Commands
```bash
# Start the application
cd trantantai && mvn spring-boot:run

# In another terminal, run verification:

# 1. Test REST API cascade delete
curl -X POST http://localhost:8080/api/v1/categories -H "Content-Type: application/json" -d '{"name":"Test Cat"}' -u admin:password
# Get the ID from response
curl -X DELETE http://localhost:8080/api/v1/categories/<ID> -u admin:password
# Expected: 200 OK with {"deletedBooks": N}

# 2. Test admin cart access blocked
curl -X GET http://localhost:8080/cart -u admin:password -w "%{http_code}"
# Expected: 403

# 3. Verify no orphan books after category deletion
# (Use MongoDB shell or admin panel to check)
```

### Final Checklist
- [ ] All cascade delete methods implemented with @Transactional
- [ ] REST API returns book count on delete
- [ ] Warning dialogs show dynamic book count
- [ ] Admin cannot access /cart (403)
- [ ] No orphan books remain after category deletion
- [ ] All controllers use new cascade delete method
