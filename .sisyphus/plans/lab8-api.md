# Lab 8 - REST API CRUD cho Books và Categories (MongoDB)

## TL;DR

> **Quick Summary**: Xây dựng REST API CRUD cho Book và Category entities, tạo ViewModels với Java records, thêm Unit Tests với MockMvc, và demo JavaScript trên list.html.
> 
> **Deliverables**:
> - ViewModels package với BookGetVm, BookPostVm, CategoryGetVm, CategoryPostVm
> - BookApiController với full CRUD endpoints
> - CategoryApiController với full CRUD endpoints  
> - Unit tests cho cả 2 API controllers
> - JavaScript demo buttons trên book/list.html
> 
> **Estimated Effort**: Medium (4-6 hours)
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: ViewModels → Controllers → Tests → Frontend

---

## Context

### Original Request
Adapt Lab 8 (API và Socket) cho MongoDB - tập trung vào REST API CRUD cho Books và Categories. Skip phần WebSocket/RabbitMQ chat.

### Interview Summary
**Key Discussions**:
- **Chat feature**: Skip - không cần WebSocket/RabbitMQ
- **Frontend approach**: Giữ Thymeleaf + thêm JavaScript demo buttons
- **Category API**: Có - thêm CRUD API cho Category
- **Testing**: Unit tests với MockMvc

**Research Findings**:
- Project dùng Spring Boot 4.0.2 + Spring Data MongoDB
- Không có Lombok - dùng Java records cho ViewModels
- Test dependency đã có: `spring-boot-starter-webmvc-test`
- SecurityConfig đã có `/api/**` requires authenticated()

### Metis Review
**Identified Gaps** (addressed):
- CSRF handling cho API tests → Dùng `.with(csrf())` trong MockMvc
- Category delete khi có books → Return 409 Conflict
- Invalid categoryId khi tạo/update Book → Return 400 Bad Request
- JavaScript cần CSRF token → Lấy từ Thymeleaf

---

## Work Objectives

### Core Objective
Implement REST API CRUD cho Book và Category entities theo mô hình Lab 8, adapted cho MongoDB với String IDs.

### Concrete Deliverables
- `trantantai.trantantai.viewmodels.BookGetVm` - Response DTO
- `trantantai.trantantai.viewmodels.BookPostVm` - Request DTO
- `trantantai.trantantai.viewmodels.CategoryGetVm` - Response DTO
- `trantantai.trantantai.viewmodels.CategoryPostVm` - Request DTO
- `trantantai.trantantai.controllers.BookApiController` - REST API
- `trantantai.trantantai.controllers.CategoryApiController` - REST API
- `trantantai.trantantai.BookApiControllerTest` - Unit tests
- `trantantai.trantantai.CategoryApiControllerTest` - Unit tests
- Updated `templates/book/list.html` với JavaScript demo

### Definition of Done
- [x] All 4 ViewModels compile và có static `from()` method
- [x] BookApiController handles GET/POST/PUT/DELETE
- [x] CategoryApiController handles GET/POST/PUT/DELETE
- [~] All unit tests pass: `./mvnw test` (BLOCKED: Spring Boot 4.x compatibility)
- [x] JavaScript demo buttons work trên list.html

### Must Have
- Java records cho ViewModels (không có Lombok)
- Validation cho categoryId exists trước khi save Book
- Return 409 Conflict khi delete Category có books
- CSRF handling trong tests và JavaScript
- MockMvc tests với @WebMvcTest và @MockBean

### Must NOT Have (Guardrails)
- KHÔNG thêm dependency mới vào pom.xml
- KHÔNG modify SecurityConfig
- KHÔNG modify existing entities (Book, Category)
- KHÔNG tạo @ControllerAdvice cho API errors
- KHÔNG thêm Swagger/OpenAPI
- KHÔNG thêm CORS configuration
- KHÔNG tạo wrapper response objects (ApiResponse<T>)
- KHÔNG thêm external JavaScript libraries

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES (JUnit 5 + spring-boot-starter-webmvc-test)
- **User wants tests**: YES
- **Framework**: Spring MockMvc

### Automated Verification

**For API endpoints** (using curl via Bash after server running):
```bash
# Test GET all books
curl -s http://localhost:8080/api/v1/books -u admin:admin | jq 'type'
# Assert: "array"

# Test GET category by ID (needs valid ID)
curl -s http://localhost:8080/api/v1/categories -u admin:admin | jq '.[0].id'
# Assert: Returns ID string
```

**For Unit Tests**:
```bash
./mvnw test -Dtest="BookApiControllerTest,CategoryApiControllerTest"
# Assert: BUILD SUCCESS
```

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: Create ViewModels (BookGetVm, BookPostVm, CategoryGetVm, CategoryPostVm)
└── (Independent - foundation for all other tasks)

Wave 2 (After Wave 1):
├── Task 2: Create CategoryApiController
├── Task 3: Create BookApiController  
└── (Can run in parallel - both depend on ViewModels)

Wave 3 (After Wave 2):
├── Task 4: Create CategoryApiControllerTest
├── Task 5: Create BookApiControllerTest
└── (Can run in parallel - both depend on controllers)

Wave 4 (After Wave 3):
└── Task 6: Update list.html với JavaScript demo

Critical Path: Task 1 → Task 3 → Task 5 → Task 6
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2, 3 | None (foundation) |
| 2 | 1 | 4 | 3 |
| 3 | 1 | 5 | 2 |
| 4 | 2 | 6 | 5 |
| 5 | 3 | 6 | 4 |
| 6 | 4, 5 | None | None (final) |

---

## TODOs

- [x] 1. Create ViewModels Package

  **What to do**:
  - Tạo package `trantantai.trantantai.viewmodels`
  - Tạo `BookGetVm.java` - Java record với fields: String id, String title, String author, Double price, String categoryId, String categoryName
  - Tạo `BookPostVm.java` - Java record với fields: String title, String author, Double price, String categoryId
  - Tạo `CategoryGetVm.java` - Java record với fields: String id, String name
  - Tạo `CategoryPostVm.java` - Java record với fields: String name
  - Mỗi ViewModel có static `from()` method để convert từ Entity

  **Must NOT do**:
  - KHÔNG dùng Lombok (@Builder, @Data)
  - KHÔNG dùng class - phải dùng record
  - KHÔNG thêm Long id - MongoDB dùng String

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Straightforward record creation, single package, copy-paste pattern
  - **Skills**: [`git-master`]
    - `git-master`: Commit atomic changes
  - **Skills Evaluated but Omitted**:
    - `frontend-ui-ux`: Not frontend task

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (foundation)
  - **Blocks**: Tasks 2, 3
  - **Blocked By**: None (start immediately)

  **References**:
  
  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/Book.java:14-52` - Book entity structure (fields to map)
  - `trantantai/src/main/java/trantantai/trantantai/entities/Category.java:11-30` - Category entity structure

  **API/Type References**:
  - Lab document shows BookGetVm pattern với `@Builder` - ADAPT to Java record WITHOUT @Builder
  - Lab: `BookGetVm(Long id, String title, String author, Double price, String category)`
  - Adapt: `BookGetVm(String id, String title, String author, Double price, String categoryId, String categoryName)`

  **Documentation References**:
  - Java Records: https://docs.oracle.com/en/java/javase/17/language/records.html

  **WHY Each Reference Matters**:
  - Book.java shows all fields và types cần map
  - Category.java shows category fields
  - Lab document provides the ViewModel pattern (adapt for record)

  **Acceptance Criteria**:

  ```bash
  # Verify files exist
  test -f "trantantai/src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java" && echo "EXISTS" || echo "MISSING"
  test -f "trantantai/src/main/java/trantantai/trantantai/viewmodels/BookPostVm.java" && echo "EXISTS" || echo "MISSING"
  test -f "trantantai/src/main/java/trantantai/trantantai/viewmodels/CategoryGetVm.java" && echo "EXISTS" || echo "MISSING"
  test -f "trantantai/src/main/java/trantantai/trantantai/viewmodels/CategoryPostVm.java" && echo "EXISTS" || echo "MISSING"
  # Assert: All say "EXISTS"
  
  # Verify compilation
  cd trantantai && ./mvnw compile -q && echo "COMPILE SUCCESS" || echo "COMPILE FAILED"
  # Assert: "COMPILE SUCCESS"
  ```

  **Commit**: YES
  - Message: `feat(api): add viewmodels for Book and Category DTOs`
  - Files: `viewmodels/*.java`
  - Pre-commit: `./mvnw compile`

---

- [x] 2. Create CategoryApiController

  **What to do**:
  - Tạo `CategoryApiController.java` trong package `controllers`
  - Annotations: `@RestController`, `@RequestMapping("/api/v1/categories")`, `@CrossOrigin(origins = "*")`
  - Inject `CategoryService` và `IBookRepository` (để check books trước khi delete)
  - Implement endpoints:
    - `GET /` → `ResponseEntity<List<CategoryGetVm>>` - getAllCategories
    - `GET /{id}` → `ResponseEntity<CategoryGetVm>` hoặc 404
    - `POST /` → `ResponseEntity<CategoryGetVm>` với status 201 Created
    - `PUT /{id}` → `ResponseEntity<CategoryGetVm>` hoặc 404
    - `DELETE /{id}` → 204 No Content, 404 Not Found, hoặc 409 Conflict (nếu có books)

  **Must NOT do**:
  - KHÔNG tạo @ControllerAdvice
  - KHÔNG modify CategoryService
  - KHÔNG thêm dependency

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard REST controller pattern, clear requirements
  - **Skills**: [`git-master`]
    - `git-master`: Atomic commit
  - **Skills Evaluated but Omitted**:
    - `frontend-ui-ux`: Not frontend

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 3)
  - **Blocks**: Task 4
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookController.java:27-32` - Constructor injection pattern
  - Lab document `ApiController` example - adapt endpoint patterns

  **API/Type References**:
  - `trantantai/src/main/java/trantantai/trantantai/services/CategoryService.java` - All service methods available
  - `trantantai/src/main/java/trantantai/trantantai/repositories/IBookRepository.java:15` - `findByCategoryId()` to check books exist

  **WHY Each Reference Matters**:
  - BookController shows DI pattern used in project
  - CategoryService shows available methods
  - IBookRepository.findByCategoryId() cần để check trước khi delete

  **Acceptance Criteria**:

  ```bash
  # Verify file exists
  test -f "trantantai/src/main/java/trantantai/trantantai/controllers/CategoryApiController.java" && echo "EXISTS"
  # Assert: "EXISTS"
  
  # Verify compilation
  cd trantantai && ./mvnw compile -q && echo "COMPILE SUCCESS"
  # Assert: "COMPILE SUCCESS"
  
  # Verify endpoints defined (grep for annotations)
  grep -c "@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" trantantai/src/main/java/trantantai/trantantai/controllers/CategoryApiController.java
  # Assert: >= 5 (GET all, GET by id, POST, PUT, DELETE)
  ```

  **Commit**: YES
  - Message: `feat(api): add CategoryApiController with CRUD endpoints`
  - Files: `controllers/CategoryApiController.java`
  - Pre-commit: `./mvnw compile`

---

- [x] 3. Create BookApiController

  **What to do**:
  - Tạo `BookApiController.java` trong package `controllers`
  - Annotations: `@RestController`, `@RequestMapping("/api/v1/books")`, `@CrossOrigin(origins = "*")`
  - Inject `BookService` và `CategoryService` (để validate categoryId)
  - Implement endpoints:
    - `GET /` → `ResponseEntity<List<BookGetVm>>` với params pageNo, pageSize, sortBy
    - `GET /{id}` → `ResponseEntity<BookGetVm>` hoặc 404
    - `GET /search?keyword=x` → `ResponseEntity<List<BookGetVm>>`
    - `POST /` → 201 + BookGetVm HOẶC 400 (nếu categoryId invalid)
    - `PUT /{id}` → BookGetVm HOẶC 404 HOẶC 400 (validation)
    - `DELETE /{id}` → 204 HOẶC 404

  **Must NOT do**:
  - KHÔNG modify BookService
  - KHÔNG tạo endpoint mới ngoài spec
  - KHÔNG thêm pagination metadata response

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard REST controller, similar to Task 2
  - **Skills**: [`git-master`]
    - `git-master`: Atomic commit
  - **Skills Evaluated but Omitted**:
    - `frontend-ui-ux`: Not frontend

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 2)
  - **Blocks**: Task 5
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookController.java:34-51` - Pagination params handling
  - Lab document `ApiController` - endpoint structure

  **API/Type References**:
  - `trantantai/src/main/java/trantantai/trantantai/services/BookService.java` - All service methods
  - `trantantai/src/main/java/trantantai/trantantai/services/CategoryService.java:25-27` - getCategoryById() for validation

  **WHY Each Reference Matters**:
  - BookController shows how pagination params are handled
  - BookService has all CRUD methods ready
  - CategoryService.getCategoryById() để validate categoryId exists

  **Acceptance Criteria**:

  ```bash
  # Verify file exists
  test -f "trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java" && echo "EXISTS"
  # Assert: "EXISTS"
  
  # Verify compilation
  cd trantantai && ./mvnw compile -q && echo "COMPILE SUCCESS"
  # Assert: "COMPILE SUCCESS"
  
  # Verify endpoints count
  grep -c "@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java
  # Assert: >= 6 (GET all, GET by id, GET search, POST, PUT, DELETE)
  ```

  **Commit**: YES
  - Message: `feat(api): add BookApiController with CRUD endpoints`
  - Files: `controllers/BookApiController.java`
  - Pre-commit: `./mvnw compile`

---

- [~] 4. Create CategoryApiControllerTest (BLOCKED: Spring Boot 4.x @WebMvcTest incompatibility)

  **What to do**:
  - Tạo `CategoryApiControllerTest.java` trong `src/test/java/trantantai/trantantai/`
  - Dùng `@WebMvcTest(CategoryApiController.class)` annotation
  - Mock dependencies với `@MockBean CategoryService` và `@MockBean IBookRepository`
  - Thêm `@MockBean` cho các beans cần thiết khác (UserService, OAuthService nếu cần)
  - Test cases:
    - `getAllCategories_returnsListOfCategories()`
    - `getCategoryById_existingId_returnsCategory()`
    - `getCategoryById_nonExistingId_returns404()`
    - `createCategory_validData_returns201()`
    - `updateCategory_existingId_returnsUpdated()`
    - `deleteCategory_noBooks_returns204()`
    - `deleteCategory_hasBooks_returns409()`
  - Dùng `@WithMockUser(roles = "USER")` cho mỗi test method
  - Dùng `.with(csrf())` cho POST/PUT/DELETE requests

  **Must NOT do**:
  - KHÔNG require real MongoDB
  - KHÔNG test với real database
  - KHÔNG skip CSRF trong tests

  **Recommended Agent Profile**:
  - **Category**: `unspecified-low`
    - Reason: Standard test class, follows MockMvc patterns
  - **Skills**: [`git-master`]
    - `git-master`: Atomic commit
  - **Skills Evaluated but Omitted**:
    - `frontend-ui-ux`: Not frontend

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 5)
  - **Blocks**: Task 6
  - **Blocked By**: Task 2

  **References**:

  **Pattern References**:
  - `trantantai/src/test/java/trantantai/trantantai/TrantantaiApplicationTests.java` - Test class structure
  - Spring MockMvc docs: Testing pattern with @WebMvcTest

  **API/Type References**:
  - `CategoryApiController` - endpoints to test
  - `CategoryService` - methods to mock
  - `IBookRepository.findByCategoryId()` - mock for delete conflict test

  **External References**:
  - MockMvc with CSRF: https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/csrf.html
  - @WebMvcTest: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing.spring-boot-applications.spring-mvc-tests

  **WHY Each Reference Matters**:
  - Existing test shows project test structure
  - MockMvc docs show CSRF and auth handling
  - @WebMvcTest docs explain how to mock beans

  **Acceptance Criteria**:

  ```bash
  # Verify file exists
  test -f "trantantai/src/test/java/trantantai/trantantai/CategoryApiControllerTest.java" && echo "EXISTS"
  # Assert: "EXISTS"
  
  # Run specific test class
  cd trantantai && ./mvnw test -Dtest=CategoryApiControllerTest -q && echo "TESTS PASSED" || echo "TESTS FAILED"
  # Assert: "TESTS PASSED"
  ```

  **Commit**: YES
  - Message: `test(api): add unit tests for CategoryApiController`
  - Files: `src/test/java/.../CategoryApiControllerTest.java`
  - Pre-commit: `./mvnw test -Dtest=CategoryApiControllerTest`

---

- [~] 5. Create BookApiControllerTest (BLOCKED: Spring Boot 4.x @WebMvcTest incompatibility)

  **What to do**:
  - Tạo `BookApiControllerTest.java` trong `src/test/java/trantantai/trantantai/`
  - Dùng `@WebMvcTest(BookApiController.class)` annotation
  - Mock dependencies với `@MockBean BookService` và `@MockBean CategoryService`
  - Thêm `@MockBean` cho các beans cần thiết khác
  - Test cases:
    - `getAllBooks_returnsListOfBooks()`
    - `getBookById_existingId_returnsBook()`
    - `getBookById_nonExistingId_returns404()`
    - `searchBooks_returnsMatchingBooks()`
    - `createBook_validData_returns201()`
    - `createBook_invalidCategoryId_returns400()`
    - `updateBook_existingId_returnsUpdated()`
    - `updateBook_nonExistingId_returns404()`
    - `deleteBook_existingId_returns204()`
  - Dùng `@WithMockUser(roles = "USER")`
  - Dùng `.with(csrf())` cho POST/PUT/DELETE

  **Must NOT do**:
  - KHÔNG require real MongoDB
  - KHÔNG skip CSRF

  **Recommended Agent Profile**:
  - **Category**: `unspecified-low`
    - Reason: Standard test class pattern
  - **Skills**: [`git-master`]
    - `git-master`: Atomic commit
  - **Skills Evaluated but Omitted**:
    - `frontend-ui-ux`: Not frontend

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 4)
  - **Blocks**: Task 6
  - **Blocked By**: Task 3

  **References**:

  **Pattern References**:
  - Task 4's CategoryApiControllerTest - same pattern
  - `trantantai/src/test/java/trantantai/trantantai/TrantantaiApplicationTests.java` - Base test structure

  **API/Type References**:
  - `BookApiController` - endpoints to test
  - `BookService` - methods to mock
  - `CategoryService.getCategoryById()` - mock for validation test

  **WHY Each Reference Matters**:
  - CategoryApiControllerTest (Task 4) provides reusable pattern
  - BookService methods define what to mock

  **Acceptance Criteria**:

  ```bash
  # Verify file exists
  test -f "trantantai/src/test/java/trantantai/trantantai/BookApiControllerTest.java" && echo "EXISTS"
  # Assert: "EXISTS"
  
  # Run specific test class
  cd trantantai && ./mvnw test -Dtest=BookApiControllerTest -q && echo "TESTS PASSED" || echo "TESTS FAILED"
  # Assert: "TESTS PASSED"
  ```

  **Commit**: YES
  - Message: `test(api): add unit tests for BookApiController`
  - Files: `src/test/java/.../BookApiControllerTest.java`
  - Pre-commit: `./mvnw test -Dtest=BookApiControllerTest`

---

- [x] 6. Update list.html with JavaScript API Demo

  **What to do**:
  - Mở `trantantai/src/main/resources/templates/book/list.html`
  - Thêm section "API Demo" với 3 buttons:
    - "Load Books from API" - GET /api/v1/books → hiển thị trong div
    - "Get First Book" - GET /api/v1/books/{id} của item đầu tiên
    - "Create Test Book" - POST /api/v1/books với test data
  - Thêm `<div id="api-result">` để hiển thị kết quả JSON
  - Lấy CSRF token từ Thymeleaf: `th:attr="data-csrf=${_csrf.token}"`
  - Dùng native `fetch()` API
  - Style buttons và result div cho đẹp

  **Must NOT do**:
  - KHÔNG thay thế existing Thymeleaf table
  - KHÔNG thêm external JS libraries (jQuery, Axios)
  - KHÔNG remove existing functionality

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Frontend work with HTML/JavaScript, needs UI consideration
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: HTML/CSS/JS integration, UI styling
  - **Skills Evaluated but Omitted**:
    - `playwright`: Not needed - manual verification
    - `git-master`: Simple single file change

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 4 (final)
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 4, 5 (need working API)

  **References**:

  **Pattern References**:
  - `trantantai/src/main/resources/templates/book/list.html` - Current structure
  - `trantantai/src/main/resources/templates/layout.html` - Check for existing JS patterns

  **External References**:
  - Thymeleaf CSRF: https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html#csrf-meta-tags
  - Fetch API: https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch

  **WHY Each Reference Matters**:
  - list.html shows where to add new section
  - layout.html may have JS patterns to follow
  - Thymeleaf CSRF docs show how to expose token to JS

  **Acceptance Criteria**:

  **Automated verification via Playwright browser** (using playwright skill):
  ```
  1. Start server: ./mvnw spring-boot:run (background)
  2. Navigate to: http://localhost:8080/login
  3. Fill: input[name="username"] with "admin"
  4. Fill: input[name="password"] with "admin" 
  5. Click: button[type="submit"]
  6. Wait for: URL contains "/books"
  7. Assert: element "#api-demo-section" exists
  8. Assert: button "Load Books from API" visible
  9. Click: button "Load Books from API"
  10. Wait 2s for API response
  11. Assert: element "#api-result" contains text (not empty)
  12. Screenshot: .sisyphus/evidence/task-6-api-demo.png
  ```

  **Commit**: YES
  - Message: `feat(ui): add JavaScript API demo section to book list`
  - Files: `templates/book/list.html`
  - Pre-commit: N/A (manual browser test)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(api): add viewmodels for Book and Category DTOs` | viewmodels/*.java | `./mvnw compile` |
| 2 | `feat(api): add CategoryApiController with CRUD endpoints` | controllers/CategoryApiController.java | `./mvnw compile` |
| 3 | `feat(api): add BookApiController with CRUD endpoints` | controllers/BookApiController.java | `./mvnw compile` |
| 4 | `test(api): add unit tests for CategoryApiController` | CategoryApiControllerTest.java | `./mvnw test -Dtest=CategoryApiControllerTest` |
| 5 | `test(api): add unit tests for BookApiController` | BookApiControllerTest.java | `./mvnw test -Dtest=BookApiControllerTest` |
| 6 | `feat(ui): add JavaScript API demo section to book list` | templates/book/list.html | Browser test |

---

## Success Criteria

### Verification Commands
```bash
# Compile project
cd trantantai && ./mvnw compile
# Expected: BUILD SUCCESS

# Run all tests
cd trantantai && ./mvnw test
# Expected: BUILD SUCCESS, Tests run: X, Failures: 0

# Run specific API tests
cd trantantai && ./mvnw test -Dtest="BookApiControllerTest,CategoryApiControllerTest"
# Expected: BUILD SUCCESS
```

### Final Checklist
- [x] All ViewModels compile (4 files)
- [x] BookApiController has 6 endpoints
- [x] CategoryApiController has 5 endpoints
- [~] All unit tests pass (BLOCKED: Spring Boot 4.x compatibility)
- [x] JavaScript demo works in browser
- [x] No new dependencies added
- [x] SecurityConfig unchanged
- [x] Entities unchanged
