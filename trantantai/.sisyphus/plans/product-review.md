# Product Review Feature (Binh luan san pham)

## TL;DR

> **Quick Summary**: Xay dung chuc nang binh luan san pham voi rating star (1-5). Chi user da mua va nhan hang (DELIVERED) moi co the danh gia. Tich hop vao trang chi tiet san pham, thay the localStorage bang backend API.
> 
> **Deliverables**:
> - Review entity + repository + service + REST controller
> - ViewModels (DTOs) cho request/response
> - Sua detail.html: thay localStorage JS bang AJAX calls
> - Purchase verification logic
> 
> **Estimated Effort**: Medium (4-6 hours)
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: Task 1 -> Task 2 -> Task 3 -> Task 5 -> Task 6

---

## Context

### Original Request
Xay dung chuc nang binh luan san pham (review) voi rating star, dam bao user khi mua hang thi moi co the binh luan duoc san pham. Tich hop vao FE detail san pham de hoat dong.

### Interview Summary
**Key Discussions**:
- Edit/Delete reviews: KHONG - MVP don gian
- Pagination: 10 reviews/trang
- Admin moderation: KHONG CAN
- Display name: Username
- Test strategy: Manual verification

**Research Findings**:
- Entity pattern: `@Document`, `@Id` (String), `@Indexed`, validation annotations
- Repository: `I{Entity}Repository extends MongoRepository<Entity, String>`
- REST API: `@RestController`, `ResponseEntity<T>`, Java records for DTOs
- detail.html: Review UI exists (lines 256-348), localStorage JS (lines 445-760)
- CSRF: `X-CSRF-TOKEN` header required for AJAX POST

### Metis Review
**Identified Gaps** (addressed):
- Missing custom query for purchase verification -> Add to IInvoiceRepository
- Username storage -> DENORMALIZED into Review entity
- Comment validation -> Required, max 500 chars
- XSS protection -> Sanitize HTML in comments
- Concurrent submissions -> Unique compound index on (bookId, userId)
- Average rating calculation -> On-demand (not stored on Book)

---

## Work Objectives

### Core Objective
Cho phep user da mua san pham (order DELIVERED) danh gia voi star rating va comment. Hien thi reviews tren trang chi tiet san pham.

### Concrete Deliverables
- `src/main/java/trantantai/trantantai/entities/Review.java`
- `src/main/java/trantantai/trantantai/repositories/IReviewRepository.java`
- `src/main/java/trantantai/trantantai/services/ReviewService.java`
- `src/main/java/trantantai/trantantai/controllers/ReviewApiController.java`
- `src/main/java/trantantai/trantantai/viewmodels/ReviewGetVm.java`
- `src/main/java/trantantai/trantantai/viewmodels/ReviewPostVm.java`
- `src/main/java/trantantai/trantantai/viewmodels/ReviewStatisticsVm.java`
- Modified `IInvoiceRepository.java` (add purchase check query)
- Modified `src/main/resources/templates/book/detail.html` (AJAX integration)

### Definition of Done
- [ ] User chua mua hang KHONG THAY review form, thay message "Ban can mua san pham nay de danh gia"
- [ ] User da mua va nhan hang (DELIVERED) THAY review form
- [ ] User da review roi KHONG THAY form, thay message "Ban da danh gia san pham nay"
- [ ] Submit review thanh cong -> Review hien thi ngay khong can refresh
- [ ] GET /api/reviews/{bookId} tra ve danh sach reviews paginated
- [ ] POST /api/reviews tra ve 201 Created khi thanh cong
- [ ] POST /api/reviews tra ve 403 khi chua mua hang
- [ ] POST /api/reviews tra ve 409 khi da review roi

### Must Have
- Rating 1-5 (integer, required)
- Comment (required, 1-500 chars)
- Purchase verification (DELIVERED order containing bookId)
- One review per user per book (unique constraint)
- Newest-first sort order
- 10 reviews per page
- CSRF token handling

### Must NOT Have (Guardrails)
- Edit/delete reviews (MVP)
- Admin moderation
- Review images
- Review replies
- "Helpful" votes
- Filter by star rating
- Sort options (hardcode newest-first)
- Store averageRating on Book entity

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO (no test framework configured)
- **User wants tests**: NO - Manual verification
- **Framework**: None

### Automated Verification (Manual + Playwright)

Each TODO includes verification procedures that agents can execute:

**For REST API changes** (using Bash curl):
```bash
# Agent runs curl commands to verify API responses
curl -X GET http://localhost:8080/api/reviews/{bookId}
curl -X POST http://localhost:8080/api/reviews -H "Content-Type: application/json" -d '...'
```

**For Frontend/UI changes** (using playwright skill):
```
1. Navigate to: http://localhost:8080/book/{id}
2. Verify review form visibility based on user state
3. Fill form and submit
4. Assert new review appears
```

**Evidence to Capture:**
- Terminal output from curl commands
- Screenshots in .sisyphus/evidence/

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
+-- Task 1: Review Entity + Repository
+-- Task 4: Add purchase check query to IInvoiceRepository

Wave 2 (After Wave 1):
+-- Task 2: ReviewService (depends: 1, 4)
+-- Task 3: ViewModels/DTOs (depends: 1)

Wave 3 (After Wave 2):
+-- Task 5: ReviewApiController (depends: 2, 3)

Wave 4 (After Wave 3):
+-- Task 6: Frontend Integration (depends: 5)

Critical Path: Task 1 -> Task 2 -> Task 5 -> Task 6
Parallel Speedup: ~30% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2, 3 | 4 |
| 4 | None | 2 | 1 |
| 2 | 1, 4 | 5 | 3 |
| 3 | 1 | 5 | 2 |
| 5 | 2, 3 | 6 | None |
| 6 | 5 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Approach |
|------|-------|---------------------|
| 1 | 1, 4 | Parallel - independent entity/repo work |
| 2 | 2, 3 | Parallel - service and DTOs |
| 3 | 5 | Sequential - needs service + DTOs |
| 4 | 6 | Sequential - needs API ready |

---

## TODOs

### Task 1: Create Review Entity and Repository

**What to do**:
1. Create `Review.java` entity in `entities/` package:
   - Fields: id (String, @Id), bookId (String, @Indexed), userId (String, @Indexed), rating (int, @Min(1) @Max(5)), comment (String, @Size(min=1, max=500)), username (String), createdAt (Date)
   - Add `@Document(collection = "reviews")`
   - Add `@CompoundIndex(def = "{'bookId': 1, 'userId': 1}", unique = true)` for unique constraint
   - No Lombok - explicit getters/setters
   - Default constructor + all-args constructor

2. Create `IReviewRepository.java` in `repositories/` package:
   - Extend `MongoRepository<Review, String>`
   - Methods:
     - `Page<Review> findByBookIdOrderByCreatedAtDesc(String bookId, Pageable pageable)`
     - `List<Review> findByBookIdOrderByCreatedAtDesc(String bookId)`
     - `Optional<Review> findByBookIdAndUserId(String bookId, String userId)`
     - `boolean existsByBookIdAndUserId(String bookId, String userId)`
     - `long countByBookId(String bookId)`
     - `long countByBookIdAndRating(String bookId, int rating)`

**Must NOT do**:
- Do NOT add @DBRef - use manual reference with bookId/userId strings
- Do NOT use Lombok
- Do NOT add edit/delete functionality

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Straightforward entity and repository creation following established patterns
- **Skills**: [`git-master`]
  - `git-master`: For atomic commit after task completion

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 1 (with Task 4)
- **Blocks**: Task 2, Task 3
- **Blocked By**: None (can start immediately)

**References** (CRITICAL):

**Pattern References**:
- `src/main/java/trantantai/trantantai/entities/Book.java` - Entity pattern with @Document, @Id, @Indexed, validation
- `src/main/java/trantantai/trantantai/entities/Invoice.java` - Entity with @Indexed fields
- `src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java` - Repository pattern with custom query methods
- `src/main/java/trantantai/trantantai/repositories/IBookRepository.java` - Repository with findBy, countBy methods

**Type References**:
- `org.springframework.data.mongodb.core.mapping.Document`
- `org.springframework.data.annotation.Id`
- `org.springframework.data.mongodb.core.index.Indexed`
- `org.springframework.data.mongodb.core.index.CompoundIndex`
- `jakarta.validation.constraints.Min`, `Max`, `Size`
- `org.springframework.data.mongodb.repository.MongoRepository`

**Acceptance Criteria**:

**Automated Verification** (using Bash):
```bash
# Verify files created
ls -la src/main/java/trantantai/trantantai/entities/Review.java
ls -la src/main/java/trantantai/trantantai/repositories/IReviewRepository.java

# Verify compilation
cd E:\TranTanTai_22806028_JEE\TrantanTai_Lab03\trantantai && mvn compile -q
# Assert: Exit code 0, no compilation errors
```

**Evidence to Capture:**
- [ ] File exists: `entities/Review.java`
- [ ] File exists: `repositories/IReviewRepository.java`
- [ ] `mvn compile` passes without errors

**Commit**: YES
- Message: `feat(review): add Review entity and IReviewRepository`
- Files: `entities/Review.java`, `repositories/IReviewRepository.java`
- Pre-commit: `mvn compile -q`

---

### Task 2: Create ReviewService with Purchase Verification

**What to do**:
1. Create `ReviewService.java` in `services/` package:
   - Constructor injection with `@Autowired`: IReviewRepository, IInvoiceRepository, IUserRepository
   - Methods:
     - `Page<Review> getReviewsByBookId(String bookId, int page, int size)` - paginated reviews
     - `Review addReview(String bookId, String userId, int rating, String comment)` - create review
     - `boolean canUserReview(String userId, String bookId)` - check purchase verification
     - `boolean hasUserReviewed(String userId, String bookId)` - check existing review
     - `ReviewStatistics getReviewStatistics(String bookId)` - average rating, count per star

2. Purchase verification logic in `canUserReview()`:
   ```java
   // Check if user has a DELIVERED order containing bookId
   // NOTE: Use the @Query method from IInvoiceRepository (Task 4)
   return invoiceRepository.existsByUserIdAndDeliveredOrderContainingBook(userId, bookId);
   ```

3. Create inner class or separate class `ReviewStatistics`:
   - Fields: averageRating (double), totalCount (long), countByStar (Map<Integer, Long>)

**Must NOT do**:
- Do NOT add edit/delete methods
- Do NOT modify Book entity for averageRating storage
- Do NOT use @Transactional (MongoDB doesn't support it like SQL)

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Service layer following established patterns with clear business logic
- **Skills**: [`git-master`]
  - `git-master`: For atomic commit after task completion

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 2 (with Task 3)
- **Blocks**: Task 5
- **Blocked By**: Task 1, Task 4

**References** (CRITICAL):

**Pattern References**:
- `src/main/java/trantantai/trantantai/services/OrderService.java` - Service pattern with constructor DI
- `src/main/java/trantantai/trantantai/services/BookService.java` - Service with repository injection

**API/Type References**:
- `src/main/java/trantantai/trantantai/entities/Review.java` - Review entity (from Task 1)
- `src/main/java/trantantai/trantantai/repositories/IReviewRepository.java` - Review repo (from Task 1)
- `src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java` - Invoice repo with new method (from Task 4)
- `src/main/java/trantantai/trantantai/constants/OrderStatus.java` - DELIVERED enum value

**Acceptance Criteria**:

**Automated Verification** (using Bash):
```bash
# Verify file created
ls -la src/main/java/trantantai/trantantai/services/ReviewService.java

# Verify compilation
mvn compile -q
# Assert: Exit code 0
```

**Evidence to Capture:**
- [ ] File exists: `services/ReviewService.java`
- [ ] Contains method: `canUserReview(String userId, String bookId)`
- [ ] Contains method: `getReviewStatistics(String bookId)`
- [ ] `mvn compile` passes

**Commit**: YES
- Message: `feat(review): add ReviewService with purchase verification logic`
- Files: `services/ReviewService.java`
- Pre-commit: `mvn compile -q`

---

### Task 3: Create ViewModels (DTOs) for Review API

**What to do**:
1. Create `ReviewGetVm.java` in `viewmodels/` package (Java record with @Schema annotations):
   ```java
   import io.swagger.v3.oas.annotations.media.Schema;
   import jakarta.validation.constraints.NotNull;
   
   @Schema(description = "Review response model")
   public record ReviewGetVm(
       @Schema(description = "Review unique identifier") String id,
       @Schema(description = "Book ID") String bookId,
       @Schema(description = "User ID") String userId,
       @Schema(description = "Rating 1-5") int rating,
       @Schema(description = "Review comment") String comment,
       @Schema(description = "Username of reviewer") String username,
       @Schema(description = "First character of username") String userInitial,
       @Schema(description = "Review creation date") Date createdAt
   ) {
       public static ReviewGetVm from(@NotNull Review review) {
           String initial = review.getUsername() != null && !review.getUsername().isEmpty() 
               ? review.getUsername().substring(0, 1).toUpperCase() 
               : "?";
           return new ReviewGetVm(
               review.getId(),
               review.getBookId(),
               review.getUserId(),
               review.getRating(),
               review.getComment(),
               review.getUsername(),
               initial,
               review.getCreatedAt()
           );
       }
   }
   ```

2. Create `ReviewPostVm.java` in `viewmodels/` package (Java record with validation):
   ```java
   import io.swagger.v3.oas.annotations.media.Schema;
   import jakarta.validation.constraints.*;
   
   @Schema(description = "Review creation request model")
   public record ReviewPostVm(
       @Schema(description = "Book ID to review", requiredMode = Schema.RequiredMode.REQUIRED)
       @NotBlank(message = "Book ID is required")
       String bookId,
       
       @Schema(description = "Rating 1-5 stars", minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
       @Min(value = 1, message = "Rating must be at least 1")
       @Max(value = 5, message = "Rating must be at most 5")
       int rating,
       
       @Schema(description = "Review comment", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
       @NotBlank(message = "Comment is required")
       @Size(max = 500, message = "Comment must not exceed 500 characters")
       String comment
   ) {}
   ```

3. Create `ReviewStatisticsVm.java` in `viewmodels/` package (Java record):
   ```java
   import io.swagger.v3.oas.annotations.media.Schema;
   import java.util.Map;
   
   @Schema(description = "Review statistics for a book")
   public record ReviewStatisticsVm(
       @Schema(description = "Average rating (0-5)", example = "4.2")
       double averageRating,
       @Schema(description = "Total number of reviews", example = "42")
       long totalCount,
       @Schema(description = "Count of reviews per star rating {5: 10, 4: 5, ...}")
       Map<Integer, Long> countByStar
   ) {}
   ```

4. Create `CanReviewVm.java` in `viewmodels/` package:
   ```java
   import io.swagger.v3.oas.annotations.media.Schema;
   
   @Schema(description = "User's ability to review a book")
   public record CanReviewVm(
       @Schema(description = "Whether user can submit a review")
       boolean canReview,
       @Schema(description = "Whether user has already reviewed this book")
       boolean hasReviewed,
       @Schema(description = "Reason if cannot review: 'Chua mua san pham' or 'Da danh gia'")
       String reason
   ) {}
   ```

**Must NOT do**:
- Do NOT use Lombok
- Do NOT create class instead of record

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Simple DTO creation following Java record pattern
- **Skills**: [`git-master`]
  - `git-master`: For atomic commit

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 2 (with Task 2)
- **Blocks**: Task 5
- **Blocked By**: Task 1

**References** (CRITICAL):

**Pattern References**:
- `src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java` - Response DTO pattern (Java record with static from() method)
- `src/main/java/trantantai/trantantai/viewmodels/BookPostVm.java` - Request DTO pattern with validation
- `src/main/java/trantantai/trantantai/viewmodels/CategoryGetVm.java` - Simple record DTO

**Type References**:
- `src/main/java/trantantai/trantantai/entities/Review.java` - Review entity to map from

**Acceptance Criteria**:

**Automated Verification** (using Bash):
```bash
# Verify files created
ls -la src/main/java/trantantai/trantantai/viewmodels/ReviewGetVm.java
ls -la src/main/java/trantantai/trantantai/viewmodels/ReviewPostVm.java
ls -la src/main/java/trantantai/trantantai/viewmodels/ReviewStatisticsVm.java
ls -la src/main/java/trantantai/trantantai/viewmodels/CanReviewVm.java

# Verify compilation
mvn compile -q
# Assert: Exit code 0
```

**Evidence to Capture:**
- [ ] 4 ViewModel files created
- [ ] All use `record` keyword (not class)
- [ ] `mvn compile` passes

**Commit**: YES
- Message: `feat(review): add Review DTOs (ViewModels)`
- Files: `viewmodels/ReviewGetVm.java`, `viewmodels/ReviewPostVm.java`, `viewmodels/ReviewStatisticsVm.java`, `viewmodels/CanReviewVm.java`
- Pre-commit: `mvn compile -q`

---

### Task 4: Add Purchase Verification Query to IInvoiceRepository

**What to do**:
1. Open `IInvoiceRepository.java`
2. Add import for `@Query` annotation:
   ```java
   import org.springframework.data.mongodb.repository.Query;
   ```
3. Add custom query method for purchase verification (MUST use @Query - Spring Data does NOT support derived query for embedded list fields):
   ```java
   /**
    * Check if user has a DELIVERED order containing the specified bookId.
    * Uses @Query because Spring Data MongoDB doesn't support derived queries
    * for fields inside embedded document lists (itemInvoices.bookId).
    */
   @Query(value = "{ 'userId': ?0, 'orderStatus': 'DELIVERED', 'itemInvoices.bookId': ?1 }", exists = true)
   boolean existsByUserIdAndDeliveredOrderContainingBook(String userId, String bookId);
   ```

**IMPORTANT**: Do NOT use derived query method like `existsByUserIdAndOrderStatusAndItemInvoices_BookId` - it will NOT work with embedded document list. MUST use `@Query` annotation.

**Must NOT do**:
- Do NOT modify existing methods
- Do NOT change Invoice or ItemInvoice entities

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Single method addition to existing repository
- **Skills**: [`git-master`]
  - `git-master`: For atomic commit

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 1 (with Task 1)
- **Blocks**: Task 2
- **Blocked By**: None (can start immediately)

**References** (CRITICAL):

**Pattern References**:
- `src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java:10-20` - Existing repository methods
- `src/main/java/trantantai/trantantai/repositories/IBookRepository.java` - Similar repository patterns

**Type References**:
- `src/main/java/trantantai/trantantai/entities/Invoice.java` - Invoice entity with itemInvoices field
- `src/main/java/trantantai/trantantai/entities/ItemInvoice.java` - Embedded document with bookId field
- `src/main/java/trantantai/trantantai/entities/OrderStatus.java` - DELIVERED enum

**Acceptance Criteria**:

**Automated Verification** (using Bash):
```bash
# Verify method added (search for method signature)
grep -n "existsByUserIdAndDeliveredOrderContainingBook\|existsByUserIdAndOrderStatusAndItemInvoices_BookId" src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java
# Assert: Returns line number (method exists)

# Verify compilation
mvn compile -q
# Assert: Exit code 0
```

**Evidence to Capture:**
- [ ] Method added to IInvoiceRepository
- [ ] `mvn compile` passes

**Commit**: YES
- Message: `feat(invoice): add purchase verification query method`
- Files: `repositories/IInvoiceRepository.java`
- Pre-commit: `mvn compile -q`

---

### Task 5: Create ReviewApiController (REST API)

**What to do**:
1. Create `ReviewApiController.java` in `controllers/` package:
   ```java
   @Tag(name = "Reviews", description = "Product review APIs")
   @RestController
   @RequestMapping("/api/reviews")
   public class ReviewApiController {
       // Constructor injection with ReviewService
   }
   ```

2. Implement endpoints:
   
   **GET /api/reviews/{bookId}** - Get reviews for book (paginated)
   ```java
   @GetMapping("/{bookId}")
   @Operation(summary = "Get reviews for a book")
   public ResponseEntity<Page<ReviewGetVm>> getReviews(
       @PathVariable String bookId,
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "10") int size
   ) { ... }
   ```

   **POST /api/reviews** - Submit review
   ```java
   @PostMapping
   @Operation(summary = "Submit a review")
   public ResponseEntity<?> createReview(
       @Valid @RequestBody ReviewPostVm reviewPostVm,
       @AuthenticationPrincipal User user
   ) {
       // Check canUserReview -> 403 if false
       // Check hasUserReviewed -> 409 if true
       // Create review -> 201 Created
   }
   ```

   **GET /api/reviews/{bookId}/can-review** - Check if user can review
   ```java
   @GetMapping("/{bookId}/can-review")
   public ResponseEntity<CanReviewVm> canReview(
       @PathVariable String bookId,
       @AuthenticationPrincipal User user
   ) { ... }
   ```

   **GET /api/reviews/{bookId}/statistics** - Get rating statistics
   ```java
   @GetMapping("/{bookId}/statistics")
   public ResponseEntity<ReviewStatisticsVm> getStatistics(
       @PathVariable String bookId
   ) { ... }
   ```

3. Response status codes:
   - 200 OK: GET requests
   - 201 Created: POST success
   - 400 Bad Request: Validation errors
   - 401 Unauthorized: Not logged in
   - 403 Forbidden: Hasn't purchased
   - 409 Conflict: Already reviewed

**Must NOT do**:
- Do NOT add PUT/DELETE endpoints (MVP)
- Do NOT bypass CSRF (it's handled by Spring Security for authenticated routes)
- Do NOT add admin-only endpoints

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: REST controller following established patterns
- **Skills**: [`git-master`]
  - `git-master`: For atomic commit

**Parallelization**:
- **Can Run In Parallel**: NO
- **Parallel Group**: Wave 3 (sequential)
- **Blocks**: Task 6
- **Blocked By**: Task 2, Task 3

**References** (CRITICAL):

**Pattern References**:
- `src/main/java/trantantai/trantantai/controllers/BookApiController.java` - REST controller with @Tag, @Operation, ResponseEntity
- `src/main/java/trantantai/trantantai/controllers/CategoryApiController.java` - Similar CRUD controller

**API/Type References**:
- `src/main/java/trantantai/trantantai/services/ReviewService.java` - Service to inject (from Task 2)
- `src/main/java/trantantai/trantantai/viewmodels/ReviewGetVm.java` - Response DTO (from Task 3)
- `src/main/java/trantantai/trantantai/viewmodels/ReviewPostVm.java` - Request DTO (from Task 3)
- `src/main/java/trantantai/trantantai/entities/User.java` - For @AuthenticationPrincipal

**Acceptance Criteria**:

**Automated Verification** (using Bash after starting server):
```bash
# Start server in background (if not running)
# mvn spring-boot:run &

# Test GET reviews (should return empty or existing reviews)
curl -s http://localhost:8080/api/reviews/test-book-id | jq '.content'
# Assert: Returns JSON array (even if empty)

# Test GET statistics
curl -s http://localhost:8080/api/reviews/test-book-id/statistics | jq '.averageRating'
# Assert: Returns number

# Test GET can-review (without auth - should fail)
curl -s -w "%{http_code}" http://localhost:8080/api/reviews/test-book-id/can-review
# Assert: Returns 401 or 403

# Verify compilation
mvn compile -q
# Assert: Exit code 0
```

**Evidence to Capture:**
- [ ] File exists: `controllers/ReviewApiController.java`
- [ ] Has 4 endpoints: GET /{bookId}, POST, GET /{bookId}/can-review, GET /{bookId}/statistics
- [ ] `mvn compile` passes
- [ ] curl tests return expected status codes

**Commit**: YES
- Message: `feat(review): add ReviewApiController REST endpoints`
- Files: `controllers/ReviewApiController.java`
- Pre-commit: `mvn compile -q`

---

### Task 6: Integrate Review API into detail.html Frontend

**What to do**:
1. Open `src/main/resources/templates/book/detail.html`

2. **Replace localStorage JavaScript with AJAX calls**:
   
   Location: Lines 445-760 (review JavaScript section)
   
   a. Add CSRF token variables at start of script:
   ```javascript
   const csrfToken = /*[[${_csrf.token}]]*/ '';
   const csrfHeader = /*[[${_csrf.headerName}]]*/ 'X-CSRF-TOKEN';
   const bookId = /*[[${book.id}]]*/ '';
   const isAuthenticated = /*[[${#authorization.expression('isAuthenticated()')}]]*/ false;
   ```

   b. Replace `saveReview()` function:
   ```javascript
   async function saveReview(reviewData) {
       const response = await fetch('/api/reviews', {
           method: 'POST',
           headers: {
               'Content-Type': 'application/json',
               [csrfHeader]: csrfToken
           },
           body: JSON.stringify({
               bookId: bookId,
               rating: reviewData.rating,
               comment: reviewData.comment
           })
       });
       
       if (response.status === 201) {
           const review = await response.json();
           prependReviewCard(review);
           resetForm();
           showToast('Danh gia cua ban da duoc gui!', 'success');
           loadStatistics();
           checkCanReview(); // Re-check to hide form
       } else if (response.status === 403) {
           showToast('Ban can mua san pham nay de danh gia', 'warning');
       } else if (response.status === 409) {
           showToast('Ban da danh gia san pham nay roi', 'warning');
       }
   }
   ```

   c. Replace `getReviews()` and `loadReviews()`:
   ```javascript
   let currentPage = 0;
   const pageSize = 10;
   
   async function loadReviews(page = 0) {
       const response = await fetch(`/api/reviews/${bookId}?page=${page}&size=${pageSize}`);
       const data = await response.json();
       
       if (page === 0) {
           clearReviewsList();
       }
       
       data.content.forEach(review => {
           appendReviewCard(review);
       });
       
       // Show/hide "Load more" button
       const loadMoreBtn = document.getElementById('loadMoreBtn');
       if (data.last) {
           loadMoreBtn.style.display = 'none';
       } else {
           loadMoreBtn.style.display = 'block';
       }
       
       currentPage = page;
   }
   ```

   d. Add `checkCanReview()` function:
   ```javascript
   async function checkCanReview() {
       if (!isAuthenticated) {
           showLoginPrompt();
           return;
       }
       
       const response = await fetch(`/api/reviews/${bookId}/can-review`);
       const data = await response.json();
       
       const reviewForm = document.getElementById('writeReviewSection');
       const alreadyReviewedMsg = document.getElementById('alreadyReviewedMsg');
       const purchaseRequiredMsg = document.getElementById('purchaseRequiredMsg');
       
       if (data.hasReviewed) {
           reviewForm.style.display = 'none';
           alreadyReviewedMsg.style.display = 'block';
           purchaseRequiredMsg.style.display = 'none';
       } else if (!data.canReview) {
           reviewForm.style.display = 'none';
           alreadyReviewedMsg.style.display = 'none';
           purchaseRequiredMsg.style.display = 'block';
       } else {
           reviewForm.style.display = 'block';
           alreadyReviewedMsg.style.display = 'none';
           purchaseRequiredMsg.style.display = 'none';
       }
   }
   ```

   e. Add `loadStatistics()` function:
   ```javascript
   async function loadStatistics() {
       const response = await fetch(`/api/reviews/${bookId}/statistics`);
       const stats = await response.json();
       
       document.getElementById('averageRating').textContent = stats.averageRating.toFixed(1);
       document.getElementById('totalReviews').textContent = stats.totalCount;
       
       // Update breakdown bars
       for (let star = 5; star >= 1; star--) {
           const count = stats.countByStar[star] || 0;
           const percent = stats.totalCount > 0 ? (count / stats.totalCount * 100) : 0;
           
           document.querySelector(`.breakdown-fill[data-stars="${star}"]`).style.width = `${percent}%`;
           document.querySelector(`.breakdown-count[data-stars="${star}"]`).textContent = count;
       }
       
       updateSummaryStars(stats.averageRating);
   }
   ```

   f. Update `DOMContentLoaded` to call new functions:
   ```javascript
   document.addEventListener('DOMContentLoaded', function() {
       loadReviews();
       loadStatistics();
       checkCanReview();
       setupStarRating();
       setupReviewForm();
   });
   ```

3. **Add HTML elements for conditional messages**:
   
   Location: Near line 245 (inside authenticated section)
   ```html
   <!-- Already reviewed message -->
   <div id="alreadyReviewedMsg" class="review-message" style="display: none;">
       <p>Ban da danh gia san pham nay roi. Cam on ban!</p>
   </div>
   
   <!-- Purchase required message -->
   <div id="purchaseRequiredMsg" class="review-message" style="display: none;">
       <p>Ban can mua san pham nay de co the danh gia.</p>
   </div>
   ```

4. **Add "Load more" button** (if not exists):
   Location: After `#reviewsList` div
   ```html
   <button id="loadMoreBtn" class="load-more-btn" onclick="loadReviews(currentPage + 1)" style="display: none;">
       Xem them danh gia
   </button>
   ```

5. **Remove localStorage code**:
   - Delete `REVIEWS_KEY` constant
   - Delete old `localStorage.setItem/getItem` calls

**Must NOT do**:
- Do NOT change CSS styles (reuse existing classes)
- Do NOT add new UI components beyond messages
- Do NOT modify star rating UI structure
- Do NOT touch non-review parts of the page

**Recommended Agent Profile**:
- **Category**: `visual-engineering`
  - Reason: Frontend modification with Thymeleaf/JavaScript integration
- **Skills**: [`playwright`, `git-master`]
  - `playwright`: For UI verification via browser automation
  - `git-master`: For atomic commit

**Parallelization**:
- **Can Run In Parallel**: NO
- **Parallel Group**: Wave 4 (final)
- **Blocks**: None (final task)
- **Blocked By**: Task 5

**References** (CRITICAL):

**Pattern References**:
- `src/main/resources/templates/book/detail.html:387-443` - Existing AJAX pattern (add-to-cart fetch)
- `src/main/resources/templates/admin/inventory/list.html` - CSRF token handling pattern

**Code References**:
- `src/main/resources/templates/book/detail.html:445-760` - Current localStorage JS to REPLACE
- `src/main/resources/templates/book/detail.html:256-348` - Review UI HTML (KEEP structure)
- `src/main/resources/templates/book/detail.html:217-242` - Rating display elements to UPDATE

**Type References**:
- API: `GET /api/reviews/{bookId}` returns `Page<ReviewGetVm>`
- API: `POST /api/reviews` accepts `ReviewPostVm`
- API: `GET /api/reviews/{bookId}/can-review` returns `CanReviewVm`
- API: `GET /api/reviews/{bookId}/statistics` returns `ReviewStatisticsVm`

**Acceptance Criteria**:

**Automated Verification** (using playwright skill):
```
1. Start application: mvn spring-boot:run

2. Test as anonymous user:
   - Navigate to: http://localhost:8080/book/{existing-book-id}
   - Assert: Review form is HIDDEN
   - Assert: "Login to review" prompt is visible
   - Assert: Existing reviews load from API (check network tab)

3. Test as authenticated user WITHOUT purchase:
   - Login as test user
   - Navigate to: http://localhost:8080/book/{book-not-purchased}
   - Assert: Review form is HIDDEN
   - Assert: "Ban can mua san pham nay de danh gia" message visible

4. Test as authenticated user WITH DELIVERED purchase:
   - Login as user with delivered order
   - Navigate to: http://localhost:8080/book/{purchased-book-id}
   - Assert: Review form is VISIBLE
   - Fill: 5 stars, "San pham tuyet voi!"
   - Click: Submit
   - Assert: New review appears at top of list
   - Assert: Form is now hidden (already reviewed)
   - Assert: Average rating updated

5. Screenshot: .sisyphus/evidence/task-6-review-flow.png
```

**Evidence to Capture:**
- [ ] detail.html modified
- [ ] localStorage code removed
- [ ] AJAX calls to /api/reviews implemented
- [ ] Conditional message elements added
- [ ] Manual/Playwright verification passes

**Commit**: YES
- Message: `feat(review): integrate review API into product detail page`
- Files: `templates/book/detail.html`
- Pre-commit: Manual verification

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(review): add Review entity and IReviewRepository` | entities/Review.java, repositories/IReviewRepository.java | mvn compile |
| 4 | `feat(invoice): add purchase verification query method` | repositories/IInvoiceRepository.java | mvn compile |
| 2 | `feat(review): add ReviewService with purchase verification logic` | services/ReviewService.java | mvn compile |
| 3 | `feat(review): add Review DTOs (ViewModels)` | viewmodels/Review*Vm.java, viewmodels/CanReviewVm.java | mvn compile |
| 5 | `feat(review): add ReviewApiController REST endpoints` | controllers/ReviewApiController.java | mvn compile + curl |
| 6 | `feat(review): integrate review API into product detail page` | templates/book/detail.html | Browser test |

---

## Success Criteria

### Verification Commands
```bash
# Compile entire project
mvn compile
# Expected: BUILD SUCCESS

# Run application
mvn spring-boot:run
# Expected: Started application

# Test API endpoints
curl http://localhost:8080/api/reviews/{bookId}
# Expected: {"content":[],"pageable":...}
```

### Final Checklist
- [ ] User without login cannot see review form
- [ ] User without DELIVERED order sees "Ban can mua san pham" message
- [ ] User with DELIVERED order can submit review
- [ ] User cannot submit duplicate review (409 Conflict)
- [ ] Reviews display with star rating, username, comment, date
- [ ] Average rating and breakdown bars update after new review
- [ ] Pagination works (10 reviews per page, load more button)
- [ ] All commits follow conventional commit format
