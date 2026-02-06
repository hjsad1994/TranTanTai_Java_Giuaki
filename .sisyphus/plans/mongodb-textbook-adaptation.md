# MongoDB Textbook Adaptation Plan

## TL;DR

> **Quick Summary**: Adapt existing Spring Boot 4.x + MongoDB book management app to match textbook requirements by adding Invoice persistence for checkout, enhanced multi-field search, and Jakarta validation with custom @ValidCategoryId validator.
> 
> **Deliverables**:
> - Invoice.java + ItemInvoice.java entities (checkout persistence)
> - IInvoiceRepository.java
> - Enhanced search (title OR author OR category.name)
> - Validation annotations on Book.java and Category.java
> - Custom @ValidCategoryId annotation + validator
> - Updated controllers with @Valid + BindingResult
> - Updated templates showing validation errors
> 
> **Estimated Effort**: Medium (4-6 hours)
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 (entities) -> Task 6 (search) -> Task 8 (checkout) -> Task 10 (templates)

---

## Context

### Original Request
Adapt Spring Boot 4.x + MongoDB book management app to match textbook requirements:
1. Add validation annotations to Book and Category
2. Create custom @ValidCategoryId validator
3. Enhanced search (title OR author OR category.name)
4. Checkout saves Invoice + ItemInvoice to MongoDB
5. Show validation errors in templates

### Interview Summary
**Key Discussions**:
- Search Strategy: Service-level filtering (normalized approach - search DB, then combine results)
- Invoice Structure: Simple - id, invoiceDate, price, embedded List<ItemInvoice>
- Category Validation: REQUIRED - validator fails if categoryId null or doesn't exist in DB
- Post-Checkout: Redirect to /cart (shows empty cart)

**Research Findings**:
- Jakarta validation uses jakarta.validation.* (Spring Boot 4.x)
- Custom validators can inject repositories via @Autowired
- Embedded documents (ItemInvoice) don't need @Document annotation
- spring-boot-starter-validation already in pom.xml

### Self-Review Gap Analysis
**Identified Gaps** (addressed):
- Empty cart checkout: Added guard to prevent saving empty invoice
- Search deduplication: Must use Set to avoid duplicate books
- Transient book population in ItemInvoice: Need to populate after retrieval
- Validation error display: Must include field-specific error messages

---

## Work Objectives

### Core Objective
Implement textbook-required features: Invoice persistence, enhanced search, and Jakarta validation with custom validator.

### Concrete Deliverables
- `trantantai/src/main/java/trantantai/trantantai/entities/Invoice.java`
- `trantantai/src/main/java/trantantai/trantantai/entities/ItemInvoice.java`
- `trantantai/src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java`
- `trantantai/src/main/java/trantantai/trantantai/validators/ValidCategoryId.java`
- `trantantai/src/main/java/trantantai/trantantai/validators/ValidCategoryIdValidator.java`
- Modified: Book.java, Category.java, BookService.java, CartService.java
- Modified: IBookRepository.java, ICategoryRepository.java
- Modified: BookController.java, CartController.java
- Modified: book/add.html, book/edit.html

### Definition of Done
- [ ] `mvn clean compile` succeeds without errors
- [ ] Application starts without exceptions
- [ ] Add book with empty title shows validation error
- [ ] Add book with invalid categoryId shows validation error
- [ ] Search "keyword" returns books matching title OR author OR category.name
- [ ] Checkout saves Invoice to MongoDB and clears cart
- [ ] Cart page shows empty after checkout

### Must Have
- Invoice entity with embedded List<ItemInvoice>
- Custom @ValidCategoryId validator that checks DB
- Multi-field search (title OR author OR category.name)
- Validation errors displayed in templates
- Checkout persists to MongoDB

### Must NOT Have (Guardrails)
- NO JPA annotations (@Entity, @Table, @ManyToOne, @OneToMany)
- NO separate ItemInvoice collection (must be embedded)
- NO customer info in Invoice (keep simple per textbook)
- NO @DBRef - use String categoryId with manual population
- NO aggregation pipeline for search (use service-level logic)
- NO checkout success page (just redirect to /cart)
- NO changes to existing Cart/Item DAOs
- NO modification to MongoConfig.java

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO (only basic Spring test)
- **User wants tests**: Manual verification (textbook lab assignment)
- **QA approach**: Manual verification with specific commands

### Verification Approach
Each TODO includes executable verification that agents can run directly:
- `mvn clean compile` - Compilation check
- `mvn spring-boot:run` - Application startup
- Browser automation via playwright skill for UI verification

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: Create Invoice + ItemInvoice entities
├── Task 2: Create IInvoiceRepository
├── Task 3: Create @ValidCategoryId annotation
└── Task 4: Create ValidCategoryIdValidator

Wave 2 (After Wave 1):
├── Task 5: Add validation to Book.java and Category.java
├── Task 6: Add repository methods for search
└── Task 7: Update BookService.searchBooks()

Wave 3 (After Wave 2):
├── Task 8: Update CartService with saveCart()
├── Task 9: Update controllers with @Valid + BindingResult
└── Task 10: Update templates with validation errors

Critical Path: Task 1 → Task 8 → Task 9 → Task 10
Parallel Speedup: ~40% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 8 | 2, 3, 4 |
| 2 | None | 8 | 1, 3, 4 |
| 3 | None | 4, 5 | 1, 2 |
| 4 | 3 | 5 | None |
| 5 | 3, 4 | 9 | 6, 7 |
| 6 | None | 7 | 5 |
| 7 | 6 | None | 5 |
| 8 | 1, 2 | 9 | 5, 6, 7 |
| 9 | 5, 8 | 10 | None |
| 10 | 9 | None | None |

---

## TODOs

- [ ] 1. Create Invoice and ItemInvoice entities

  **What to do**:
  - Create `ItemInvoice.java` as a plain Java class (NOT @Document - it will be embedded)
    - Fields: `String id`, `int quantity`, `String bookId`, `@Transient Book book`
    - Add getters, setters, constructors
  - Create `Invoice.java` with @Document annotation
    - Fields: `@Id String id`, `Date invoiceDate = new Date()`, `Double price`, `List<ItemInvoice> itemInvoices = new ArrayList<>()`
    - Add getters, setters, constructors

  **Must NOT do**:
  - Do NOT add @Document to ItemInvoice
  - Do NOT use @DBRef - store bookId as String
  - Do NOT add customer information fields

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple entity creation with known patterns
  - **Skills**: None needed
  - **Skills Evaluated but Omitted**:
    - `git-master`: No commit needed during implementation

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4)
  - **Blocks**: Task 8
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/Book.java:1-42` - @Document annotation pattern, @Id usage, @Transient for non-persisted fields
  - `trantantai/src/main/java/trantantai/trantantai/daos/Item.java:5-21` - Similar structure to ItemInvoice (bookId, quantity, price)

  **WHY Each Reference Matters**:
  - Book.java shows the exact @Document, @Id, @Transient annotation style used in this project
  - Item.java shows the cart item structure that ItemInvoice will mirror

  **Acceptance Criteria**:

  ```bash
  # Verify files created with correct structure
  mvn clean compile -f trantantai/pom.xml
  # Assert: BUILD SUCCESS
  
  # Verify Invoice has @Document annotation
  grep -q "@Document" trantantai/src/main/java/trantantai/trantantai/entities/Invoice.java
  # Assert: Exit code 0
  
  # Verify ItemInvoice does NOT have @Document
  grep -q "@Document" trantantai/src/main/java/trantantai/trantantai/entities/ItemInvoice.java
  # Assert: Exit code 1 (not found = correct)
  ```

  **Commit**: NO (groups with Task 2)

---

- [ ] 2. Create IInvoiceRepository

  **What to do**:
  - Create `IInvoiceRepository.java` interface
    - Extends `MongoRepository<Invoice, String>`
    - Add `@Repository` annotation
    - No custom methods needed initially

  **Must NOT do**:
  - Do NOT add JpaRepository or any JPA imports

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single file, follows existing pattern exactly
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3, 4)
  - **Blocks**: Task 8
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/repositories/ICategoryRepository.java:1-10` - Exact pattern to follow (simple MongoRepository extension)

  **WHY Each Reference Matters**:
  - ICategoryRepository shows the minimal repository pattern used in this project

  **Acceptance Criteria**:

  ```bash
  # Verify file exists and compiles
  mvn clean compile -f trantantai/pom.xml
  # Assert: BUILD SUCCESS
  
  # Verify extends MongoRepository
  grep -q "MongoRepository<Invoice, String>" trantantai/src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(entities): add Invoice, ItemInvoice entities and IInvoiceRepository`
  - Files: `entities/Invoice.java`, `entities/ItemInvoice.java`, `repositories/IInvoiceRepository.java`
  - Pre-commit: `mvn clean compile -f trantantai/pom.xml`

---

- [ ] 3. Create @ValidCategoryId annotation

  **What to do**:
  - Create package `trantantai.trantantai.validators`
  - Create `ValidCategoryId.java` annotation:
    ```java
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = ValidCategoryIdValidator.class)
    @Documented
    public @interface ValidCategoryId {
        String message() default "Invalid category";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
    ```

  **Must NOT do**:
  - Do NOT use javax.validation - use jakarta.validation

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard Jakarta validation annotation boilerplate
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Task 4, Task 5
  - **Blocked By**: None

  **References**:

  **External References**:
  - Jakarta Validation: Custom constraint annotation pattern with @Constraint, @Target, @Retention

  **WHY Each Reference Matters**:
  - Standard Jakarta validation annotation structure required for custom validators

  **Acceptance Criteria**:

  ```bash
  # Verify annotation compiles (validator not ready yet, will fail - that's OK)
  # Just verify syntax is correct
  grep -q "@Constraint" trantantai/src/main/java/trantantai/trantantai/validators/ValidCategoryId.java
  # Assert: Exit code 0
  
  grep -q "jakarta.validation" trantantai/src/main/java/trantantai/trantantai/validators/ValidCategoryId.java
  # Assert: Exit code 0
  ```

  **Commit**: NO (groups with Task 4)

---

- [ ] 4. Create ValidCategoryIdValidator

  **What to do**:
  - Create `ValidCategoryIdValidator.java` implementing `ConstraintValidator<ValidCategoryId, String>`
  - Inject `ICategoryRepository` via `@Autowired`
  - In `isValid()` method:
    - If categoryId is null or empty: return false (category is REQUIRED)
    - If categoryId exists in database: return true
    - Otherwise: return false

  **Must NOT do**:
  - Do NOT return true for null/empty (category is required per user)
  - Do NOT use constructor injection (use @Autowired field injection for validators)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard validator implementation
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential after Task 3
  - **Blocks**: Task 5
  - **Blocked By**: Task 3

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/repositories/ICategoryRepository.java` - Repository to inject for existence check

  **External References**:
  - Research finding: Custom validators can inject repositories via @Autowired

  **WHY Each Reference Matters**:
  - ICategoryRepository.existsById() will be used to check if category exists

  **Acceptance Criteria**:

  ```bash
  # Verify compiles with annotation
  mvn clean compile -f trantantai/pom.xml
  # Assert: BUILD SUCCESS
  
  # Verify implements ConstraintValidator
  grep -q "ConstraintValidator<ValidCategoryId, String>" trantantai/src/main/java/trantantai/trantantai/validators/ValidCategoryIdValidator.java
  # Assert: Exit code 0
  
  # Verify repository injection
  grep -q "ICategoryRepository" trantantai/src/main/java/trantantai/trantantai/validators/ValidCategoryIdValidator.java
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(validation): add custom @ValidCategoryId annotation and validator`
  - Files: `validators/ValidCategoryId.java`, `validators/ValidCategoryIdValidator.java`
  - Pre-commit: `mvn clean compile -f trantantai/pom.xml`

---

- [ ] 5. Add validation annotations to Book.java and Category.java

  **What to do**:
  - In `Book.java`, add to fields:
    - `@NotBlank(message = "Title is required")` and `@Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters")` on `title`
    - `@NotBlank(message = "Author is required")` and `@Size(min = 1, max = 50, message = "Author must be between 1 and 50 characters")` on `author`
    - `@Positive(message = "Price must be positive")` on `price`
    - `@ValidCategoryId` on `categoryId`
  - In `Category.java`, add to fields:
    - `@NotBlank(message = "Name is required")` and `@Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")` on `name`
  - Add necessary imports: `jakarta.validation.constraints.*`

  **Must NOT do**:
  - Do NOT use javax.validation - use jakarta.validation
  - Do NOT remove existing @Indexed annotations
  - Do NOT modify constructors or getters/setters

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Adding annotations to existing fields
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 6, 7)
  - **Blocks**: Task 9
  - **Blocked By**: Task 3, Task 4

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/Book.java:13-28` - Fields to annotate
  - `trantantai/src/main/java/trantantai/trantantai/entities/Category.java:12-16` - Fields to annotate

  **WHY Each Reference Matters**:
  - Need exact field names and types to add correct validation annotations

  **Acceptance Criteria**:

  ```bash
  # Verify compiles
  mvn clean compile -f trantantai/pom.xml
  # Assert: BUILD SUCCESS
  
  # Verify Book has @NotBlank on title
  grep -q "@NotBlank" trantantai/src/main/java/trantantai/trantantai/entities/Book.java
  # Assert: Exit code 0
  
  # Verify Book has @ValidCategoryId
  grep -q "@ValidCategoryId" trantantai/src/main/java/trantantai/trantantai/entities/Book.java
  # Assert: Exit code 0
  
  # Verify Category has @NotBlank
  grep -q "@NotBlank" trantantai/src/main/java/trantantai/trantantai/entities/Category.java
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(validation): add Jakarta validation annotations to Book and Category entities`
  - Files: `entities/Book.java`, `entities/Category.java`
  - Pre-commit: `mvn clean compile -f trantantai/pom.xml`

---

- [ ] 6. Add repository methods for enhanced search

  **What to do**:
  - In `IBookRepository.java`, add:
    - `List<Book> findByAuthorContainingIgnoreCase(String keyword);`
    - `List<Book> findByCategoryIdIn(List<String> categoryIds);`
  - In `ICategoryRepository.java`, add:
    - `List<Category> findByNameContainingIgnoreCase(String keyword);`

  **Must NOT do**:
  - Do NOT remove existing methods
  - Do NOT use @Query annotation (Spring Data method naming is sufficient)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Adding standard Spring Data method signatures
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 7)
  - **Blocks**: Task 7
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/repositories/IBookRepository.java:20-21` - Existing method pattern
  - `trantantai/src/main/java/trantantai/trantantai/repositories/ICategoryRepository.java:7-9` - Repository to modify

  **WHY Each Reference Matters**:
  - Follow existing naming conventions for repository methods
  - IBookRepository already has findByTitleContainingIgnoreCase to follow

  **Acceptance Criteria**:

  ```bash
  # Verify compiles
  mvn clean compile -f trantantai/pom.xml
  # Assert: BUILD SUCCESS
  
  # Verify new methods in IBookRepository
  grep -q "findByAuthorContainingIgnoreCase" trantantai/src/main/java/trantantai/trantantai/repositories/IBookRepository.java
  # Assert: Exit code 0
  
  grep -q "findByCategoryIdIn" trantantai/src/main/java/trantantai/trantantai/repositories/IBookRepository.java
  # Assert: Exit code 0
  
  # Verify new method in ICategoryRepository
  grep -q "findByNameContainingIgnoreCase" trantantai/src/main/java/trantantai/trantantai/repositories/ICategoryRepository.java
  # Assert: Exit code 0
  ```

  **Commit**: NO (groups with Task 7)

---

- [ ] 7. Update BookService.searchBooks() method

  **What to do**:
  - Modify `searchBooks(String keyword)` method to:
    1. Search books by title: `bookRepository.findByTitleContainingIgnoreCase(keyword)`
    2. Search books by author: `bookRepository.findByAuthorContainingIgnoreCase(keyword)`
    3. Find categories matching keyword: `categoryRepository.findByNameContainingIgnoreCase(keyword)`
    4. Extract category IDs from matching categories
    5. If category IDs exist: Search books by categoryIds: `bookRepository.findByCategoryIdIn(categoryIds)`
    6. Combine all results using `Set<Book>` to avoid duplicates (based on book.id)
    7. Convert back to `List<Book>`
    8. Populate category for each book using existing `populateCategory()` helper
    9. Return the combined list

  **Must NOT do**:
  - Do NOT use MongoDB aggregation
  - Do NOT modify populateCategory() helper
  - Do NOT change method signature

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Service method logic modification
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential after Task 6
  - **Blocks**: None
  - **Blocked By**: Task 6

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/services/BookService.java:67-71` - Current searchBooks implementation to modify
  - `trantantai/src/main/java/trantantai/trantantai/services/BookService.java:74-79` - populateCategory helper to reuse

  **WHY Each Reference Matters**:
  - Current searchBooks shows the existing pattern (search + populate)
  - populateCategory shows how to populate @Transient category field

  **Acceptance Criteria**:

  ```bash
  # Verify compiles
  mvn clean compile -f trantantai/pom.xml
  # Assert: BUILD SUCCESS
  
  # Verify search uses multiple repositories
  grep -q "findByAuthorContainingIgnoreCase" trantantai/src/main/java/trantantai/trantantai/services/BookService.java
  # Assert: Exit code 0
  
  grep -q "findByNameContainingIgnoreCase" trantantai/src/main/java/trantantai/trantantai/services/BookService.java
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(search): enhance search to support title, author, and category name`
  - Files: `repositories/IBookRepository.java`, `repositories/ICategoryRepository.java`, `services/BookService.java`
  - Pre-commit: `mvn clean compile -f trantantai/pom.xml`

---

- [ ] 8. Update CartService with saveCart() method

  **What to do**:
  - Inject `IInvoiceRepository` into CartService
  - Add `saveCart(HttpSession session)` method:
    1. Get cart from session
    2. If cart is empty (no items): return without saving
    3. Create new Invoice with:
       - `invoiceDate = new Date()`
       - `price = getSumPrice(session)` (total price)
    4. Convert each cart Item to ItemInvoice:
       - `bookId = item.getBookId()`
       - `quantity = item.getQuantity()`
    5. Add all ItemInvoices to Invoice
    6. Save Invoice via repository
    7. Clear cart from session using `removeCart(session)`

  **Must NOT do**:
  - Do NOT save empty invoices (guard against empty cart)
  - Do NOT modify existing Cart or Item DAOs
  - Do NOT add customer information

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Service method addition following existing patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (after Wave 2)
  - **Blocks**: Task 9
  - **Blocked By**: Task 1, Task 2

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/services/CartService.java:16-23` - getCart pattern to follow
  - `trantantai/src/main/java/trantantai/trantantai/services/CartService.java:39-43` - getSumPrice for total calculation
  - `trantantai/src/main/java/trantantai/trantantai/daos/Item.java:23-54` - Item getters for conversion

  **API/Type References**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/Invoice.java` - Invoice entity to create (Task 1)
  - `trantantai/src/main/java/trantantai/trantantai/entities/ItemInvoice.java` - ItemInvoice entity (Task 1)

  **WHY Each Reference Matters**:
  - CartService patterns show session handling style
  - Item getters needed to map to ItemInvoice
  - getSumPrice provides total for Invoice

  **Acceptance Criteria**:

  ```bash
  # Verify compiles
  mvn clean compile -f trantantai/pom.xml
  # Assert: BUILD SUCCESS
  
  # Verify saveCart method exists
  grep -q "saveCart" trantantai/src/main/java/trantantai/trantantai/services/CartService.java
  # Assert: Exit code 0
  
  # Verify IInvoiceRepository is injected
  grep -q "IInvoiceRepository" trantantai/src/main/java/trantantai/trantantai/services/CartService.java
  # Assert: Exit code 0
  ```

  **Commit**: NO (groups with Task 9)

---

- [ ] 9. Update controllers with @Valid and BindingResult

  **What to do**:
  - In `BookController.java`:
    - Modify `addBook()` POST handler:
      - Add `@Valid` before `@ModelAttribute Book book`
      - Add `BindingResult bindingResult` parameter after book
      - Add `Model model` parameter
      - If `bindingResult.hasErrors()`: add categories to model and return "book/add"
    - Modify `updateBook()` POST handler:
      - Add `@Valid` before `@ModelAttribute Book book`
      - Add `BindingResult bindingResult` parameter after book
      - Add `Model model` parameter
      - If `bindingResult.hasErrors()`: add categories to model and return "book/edit"
  - In `CartController.java`:
    - Modify `checkout()` method:
      - Call `cartService.saveCart(session)` before redirect
      - Change return to `return "redirect:/cart";` (currently returns `/books`)

  **Must NOT do**:
  - Do NOT change GET handlers
  - Do NOT remove existing functionality
  - Do NOT add global exception handler (keep simple for textbook)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Controller method signature and logic modifications
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (after Task 8)
  - **Blocks**: Task 10
  - **Blocked By**: Task 5, Task 8

  **References**:

  **Pattern References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookController.java:72-76` - addBook POST handler to modify
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookController.java:89-94` - updateBook POST handler to modify
  - `trantantai/src/main/java/trantantai/trantantai/controllers/CartController.java:54-60` - checkout handler to modify (currently returns /books, needs /cart)

  **WHY Each Reference Matters**:
  - Need exact method signatures to modify for validation
  - Checkout currently just clears cart - need to add saveCart call

  **Acceptance Criteria**:

  ```bash
  # Verify compiles
  mvn clean compile -f trantantai/pom.xml
  # Assert: BUILD SUCCESS
  
  # Verify @Valid in BookController
  grep -q "@Valid" trantantai/src/main/java/trantantai/trantantai/controllers/BookController.java
  # Assert: Exit code 0
  
  # Verify BindingResult in BookController
  grep -q "BindingResult" trantantai/src/main/java/trantantai/trantantai/controllers/BookController.java
  # Assert: Exit code 0
  
  # Verify saveCart in CartController
  grep -q "saveCart" trantantai/src/main/java/trantantai/trantantai/controllers/CartController.java
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(controllers): add validation handling and checkout persistence`
  - Files: `controllers/BookController.java`, `controllers/CartController.java`, `services/CartService.java`
  - Pre-commit: `mvn clean compile -f trantantai/pom.xml`

---

- [ ] 10. Update templates to display validation errors

  **What to do**:
  - In `book/add.html`:
    - After each form field, add Thymeleaf error display:
      ```html
      <span th:if="${#fields.hasErrors('title')}" th:errors="*{title}" class="text-danger"></span>
      ```
    - Add for: title, author, price, categoryId
    - Add Bootstrap `is-invalid` class conditionally: `th:classappend="${#fields.hasErrors('title')} ? 'is-invalid'"`
  - In `book/edit.html`:
    - Same error display pattern for: title, author, price, categoryId

  **Must NOT do**:
  - Do NOT change form structure or layout
  - Do NOT remove existing th:field bindings
  - Do NOT add JavaScript validation (server-side only)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Thymeleaf template modifications
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Helpful for proper Bootstrap error styling

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (final task)
  - **Blocks**: None (final task)
  - **Blocked By**: Task 9

  **References**:

  **Pattern References**:
  - `trantantai/src/main/resources/templates/book/add.html:17-44` - Form structure to add error displays
  - `trantantai/src/main/resources/templates/book/edit.html:17-45` - Form structure to add error displays

  **WHY Each Reference Matters**:
  - Need exact form field structure to add error spans in correct locations

  **Acceptance Criteria**:

  **For UI changes (using playwright skill):**
  ```
  # Agent executes via playwright browser automation:
  1. Start application: mvn spring-boot:run (background)
  2. Navigate to: http://localhost:8080/books/add
  3. Click: button[type="submit"] (submit empty form)
  4. Wait for: page reload
  5. Assert: text "Title is required" appears on page
  6. Assert: text "Author is required" appears on page
  7. Assert: text "Invalid category" appears on page
  8. Screenshot: .sisyphus/evidence/task-10-validation-errors.png
  ```

  ```bash
  # Verify error display in templates
  grep -q "th:errors" trantantai/src/main/resources/templates/book/add.html
  # Assert: Exit code 0
  
  grep -q "th:errors" trantantai/src/main/resources/templates/book/edit.html
  # Assert: Exit code 0
  
  grep -q "is-invalid" trantantai/src/main/resources/templates/book/add.html
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(templates): add validation error display to add and edit book forms`
  - Files: `templates/book/add.html`, `templates/book/edit.html`
  - Pre-commit: `mvn clean compile -f trantantai/pom.xml`

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 2 | `feat(entities): add Invoice, ItemInvoice entities and IInvoiceRepository` | Invoice.java, ItemInvoice.java, IInvoiceRepository.java | mvn compile |
| 4 | `feat(validation): add custom @ValidCategoryId annotation and validator` | ValidCategoryId.java, ValidCategoryIdValidator.java | mvn compile |
| 5 | `feat(validation): add Jakarta validation annotations to Book and Category entities` | Book.java, Category.java | mvn compile |
| 7 | `feat(search): enhance search to support title, author, and category name` | IBookRepository.java, ICategoryRepository.java, BookService.java | mvn compile |
| 9 | `feat(controllers): add validation handling and checkout persistence` | BookController.java, CartController.java, CartService.java | mvn compile |
| 10 | `feat(templates): add validation error display to add and edit book forms` | add.html, edit.html | mvn compile |

---

## Success Criteria

### Verification Commands
```bash
# Full build
mvn clean compile -f trantantai/pom.xml
# Expected: BUILD SUCCESS

# Start application
mvn spring-boot:run -f trantantai/pom.xml
# Expected: Started TrantantaiApplication in X seconds
```

### Final Checklist
- [ ] All 10 tasks completed
- [ ] All 6 commits made
- [ ] Application compiles without errors
- [ ] Application starts without exceptions
- [ ] Validation errors display on add/edit forms
- [ ] Search returns results for title, author, AND category name matches
- [ ] Checkout saves Invoice to MongoDB
- [ ] Cart clears after checkout
- [ ] No JPA annotations used (MongoDB only)
- [ ] ItemInvoice embedded in Invoice (not separate collection)
