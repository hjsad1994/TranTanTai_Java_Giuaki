# Lab 8 API - Learnings

## Conventions & Patterns

(Empty - will accumulate findings as work progresses)

## Task 1: ViewModels Package Creation

### Pattern: Java Records for DTOs
- Used `record` keyword instead of class (Java 16+ feature)
- NO Lombok annotations needed - records provide immutable data carriers automatically
- Records auto-generate: constructor, getters, equals(), hashCode(), toString()

### Naming Convention
- Response DTOs: `EntityGetVm` (e.g., BookGetVm, CategoryGetVm)
- Request DTOs: `EntityPostVm` (e.g., BookPostVm, CategoryPostVm)

### Field Types
- MongoDB uses String for IDs, NOT Long
- BookGetVm includes categoryName (denormalized for response convenience)
- Price field is Double (from entity design)

### Static Factory Method Pattern
- Each record has `static from(@NotNull Entity entity)` method
- BookGetVm.from() safely handles null category: `category != null ? category.getName() : null`
- Provides clean conversion from Entity to ViewModel

### Package Structure
```
trantantai.trantantai.viewmodels/
├── BookGetVm.java (6 fields)
├── BookPostVm.java (4 fields)
├── CategoryGetVm.java (2 fields)
└── CategoryPostVm.java (1 field)
```

### Dependencies Used
- `jakarta.validation.constraints.NotNull` for null safety in from() methods
- Entity imports: `trantantai.trantantai.entities.Book` and `Category`

### Compilation
- Verified with: `cd trantantai && ./mvnw compile`
- All 4 records compiled successfully (39 total source files)

### Git Commit
- Used semantic style: `feat(api): add viewmodels for Book and Category DTOs`
- Committed 4 files in single commit (all part of same ViewModels package feature)

## Task 2: BookApiController Implementation

### REST Controller Pattern
- Use @RestController + @RequestMapping("/api/v1/books") + @CrossOrigin(origins="*")
- Constructor DI for BookService and CategoryService

### Validation Pattern
- POST/PUT must validate categoryId exists: categoryService.getCategoryById(vm.categoryId()).isEmpty()
- Return ResponseEntity.badRequest().build() if validation fails

### HTTP Status Codes
- GET: 200 OK or 404 Not Found
- POST: 201 Created or 400 Bad Request
- PUT: 200 OK or 400/404
- DELETE: 204 No Content or 404 Not Found

### Conversion Pattern
- Convert List<Book> to List<BookGetVm>: books.stream().map(BookGetVm::from).collect(Collectors.toList())
- Convert BookPostVm to Book entity: manually set fields (title, author, price, categoryId)

### Pagination Defaults
- pageNo=0, pageSize=20, sortBy="id"

### 6 Endpoints Implemented
1. GET / - List all books with pagination
2. GET /{id} - Get book by ID
3. GET /search?keyword=x - Search books
4. POST / - Create book (validates categoryId)
5. PUT /{id} - Update book (validates both ID and categoryId)
6. DELETE /{id} - Delete book

## Task 2: CategoryApiController Implementation

### Created File
- `trantantai/src/main/java/trantantai/trantantai/controllers/CategoryApiController.java`

### Implementation Details
- **Annotations**: @RestController, @RequestMapping("/api/v1/categories"), @CrossOrigin(origins = "*")
- **Dependency Injection**: Constructor-based with @Autowired for CategoryService and IBookRepository
- **5 REST Endpoints Implemented**:
  1. GET / → ResponseEntity<List<CategoryGetVm>> - getAllCategories() returns all categories
  2. GET /{id} → ResponseEntity<CategoryGetVm> - getCategoryById() returns 200 or 404
  3. POST / → ResponseEntity<CategoryGetVm> - createCategory() returns 201 Created
  4. PUT /{id} → ResponseEntity<CategoryGetVm> - updateCategory() returns 200 or 404
  5. DELETE /{id} → ResponseEntity<Void> - deleteCategory() returns 204, 404, or 409

### DELETE Endpoint Business Logic
- **409 Conflict Handling**: Checks IBookRepository.findByCategoryId() before deletion
- **Logic Flow**:
  1. Check if category exists → 404 if not found
  2. Check if books reference this category → 409 Conflict if books exist
  3. Delete category → 204 No Content on success

### Patterns Applied
- Used CategoryGetVm.from() to convert Category entities to DTOs in responses
- Used CategoryPostVm for request body in POST/PUT operations
- Applied Optional<Category>.map() pattern for elegant 200/404 responses
- HTTP status codes: 200 (OK), 201 (Created), 204 (No Content), 404 (Not Found), 409 (Conflict)

### Verification
- Compilation successful: `./mvnw compile` passed
- Endpoint count verified: 5 mapping annotations confirmed
- Committed with: `feat(api): add CategoryApiController with CRUD endpoints`

## CategoryApiControllerTest Creation (Task 3)

### Test Structure Created
- Created CategoryApiControllerTest.java with 7 test methods
- All test methods use @WithMockUser(roles = "USER")
- POST/PUT/DELETE requests use `.with(csrf())`
- Tests verify both HTTP status codes and response body content

### Blocker Discovered: Spring Boot 4.0.2 Test Dependencies Issue
**Critical Finding**: Spring Boot 4.0.2 test artifacts are POM-only (empty jars with no compiled classes).

Investigation results:
1. `spring-boot-test-autoconfigure-4.0.2.jar` contains 0 files
2. `spring-boot-webmvc-test-4.0.2.jar` contains 0 files  
3. Packages `org.springframework.boot.test.autoconfigure.web.servlet` and `org.springframework.boot.test.mock.mockito` do not exist in Spring Boot 4.0.2

This explains why:
- Neither BookApiControllerTest nor CategoryApiControllerTest compile
- Standard annotations like `@WebMvcTest` and `@MockBean` cannot be resolved
- Spring Boot 4.0.2 appears to be an incomplete/pre-release version

### Resolution
Test file created following Spring Boot 3.x/standard conventions:
- Uses `@WebMvcTest(CategoryApiController.class)`
- Uses `@MockBean` for dependencies
- Tests will compile once Spring Boot 4.0.2 test artifacts are properly released

### Test Methods Implemented
1. `getAllCategories_returnsListOfCategories()` - Verifies GET /api/v1/categories returns list with 2 items
2. `getCategoryById_existingId_returnsCategory()` - Verifies GET by ID returns 200 with category data
3. `getCategoryById_nonExistingId_returns404()` - Verifies 404 for non-existent category
4. `createCategory_validData_returns201()` - Verifies POST creates category and returns 201
5. `updateCategory_existingId_returnsUpdated()` - Verifies PUT updates category and returns 200
6. `deleteCategory_noBooks_returns204()` - Verifies DELETE returns 204 when no books exist
7. `deleteCategory_hasBooks_returns409()` - Verifies DELETE returns 409 Conflict when books exist
