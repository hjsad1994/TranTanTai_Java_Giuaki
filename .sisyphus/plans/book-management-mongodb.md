# Book Management System with Spring Boot + MongoDB

## TL;DR

> **Quick Summary**: Implement a complete MVC Book Management System using Spring Boot 4.0.2, MongoDB Atlas, and Thymeleaf. Adapts JPA patterns to MongoDB with manual reference relationship pattern.
> 
> **Deliverables**:
> - MongoDB document classes (Book, Category)
> - Repository interfaces with pagination
> - Service layer with CRUD operations
> - Controllers (Home, Book)
> - Thymeleaf templates with fragment-based layout
> - Bootstrap 5.3.x styling
> 
> **Estimated Effort**: Medium (8-12 tasks)
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 (pom.xml) → Task 2 (config) → Tasks 3-4 (entities) → Tasks 5-6 (repos) → Tasks 7-8 (services) → Tasks 9-10 (controllers) → Tasks 11-13 (views)

---

## Context

### Original Request
Implement a Spring Boot 4.0.2 application with MongoDB for a Book Management System, adapting from MySQL/JPA patterns to MongoDB.

### Interview Summary
**Key Discussions**:
- **Relationship Strategy**: Manual Reference (categoryId as String, join in service layer)
- **Pagination**: 20 items per page default
- **Test Strategy**: Manual verification only (lab exercise)
- **Layout**: Fragment-based Thymeleaf (th:fragment/th:replace)
- **Styling**: Bootstrap 5.3.x via CDN

**Research Findings**:
- Spring Data MongoDB uses @Document instead of @Entity
- String id with @Id auto-maps to MongoDB ObjectId
- MongoRepository provides built-in pagination via Pageable
- Manual reference pattern is performant and explicit

### Project Location
`E:\TranTanTai_22806028_JEE\TranTanTai_Lab03\trantantai\`

---

## Work Objectives

### Core Objective
Build a complete MVC Book Management System with MongoDB backend, featuring book listing with pagination and category management.

### Concrete Deliverables
1. `pom.xml` - Updated with MongoDB, Thymeleaf, Lombok, Validation dependencies
2. `application.properties` - MongoDB Atlas connection configuration
3. `Category.java` - MongoDB document entity
4. `Book.java` - MongoDB document entity with categoryId reference
5. `ICategoryRepository.java` - MongoRepository interface
6. `IBookRepository.java` - MongoRepository with pagination methods
7. `CategoryService.java` - CRUD operations
8. `BookService.java` - CRUD + pagination with category resolution
9. `HomeController.java` - Home page controller
10. `BookController.java` - Book listing with pagination
11. `layout.html` - Base template with Bootstrap
12. `home/index.html` - Home page view
13. `book/list.html` - Paginated book list view

### Definition of Done
- [ ] Application starts without errors: `mvn spring-boot:run`
- [ ] Home page displays at http://localhost:8080/
- [ ] Book list displays with pagination at http://localhost:8080/books
- [ ] MongoDB Atlas connection successful (check logs for "Connected to MongoDB")

### Must Have
- @Document annotations on entity classes
- String type for MongoDB ObjectId fields
- @Id annotation on id fields
- Manual categoryId reference (not @DBRef)
- Validation annotations (@Size) for field constraints
- Pagination with PageRequest.of(page, size)
- Fragment-based layout (th:fragment, th:replace)
- Bootstrap 5.3.x via CDN

### Must NOT Have (Guardrails)
- NO @Entity, @Table, @Column, @JoinColumn annotations (JPA-specific)
- NO @GeneratedValue(strategy = GenerationType.IDENTITY) - MongoDB auto-generates ObjectId
- NO @DBRef annotation - using manual reference instead
- NO Hibernate-specific code
- NO local Bootstrap/jQuery files - use CDN only
- NO automated tests - manual verification only
- NO complex aggregation queries - keep it simple
- NO embedded documents - use reference pattern

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO
- **User wants tests**: NO - Manual verification only
- **Framework**: None

### Manual Verification Procedures

Each task includes verification steps that can be executed via browser or terminal to confirm success.

**Verification Tools by Task Type:**
| Type | Tool | Method |
|------|------|--------|
| Configuration | Terminal | `mvn spring-boot:run` - check startup logs |
| Backend | Terminal | Application logs, no runtime errors |
| Frontend | Browser | Navigate to URL, verify visual output |
| Full Stack | Browser | Navigate, interact, verify functionality |

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
└── Task 1: Update pom.xml with dependencies

Wave 2 (After Wave 1):
├── Task 2: Configure application.properties
└── Task 3: Create layout.html (can start early - no backend dependency)

Wave 3 (After Wave 2):
├── Task 4: Create Category.java entity
└── Task 5: Create Book.java entity

Wave 4 (After Wave 3):
├── Task 6: Create ICategoryRepository.java
└── Task 7: Create IBookRepository.java

Wave 5 (After Wave 4):
├── Task 8: Create CategoryService.java
└── Task 9: Create BookService.java

Wave 6 (After Wave 5):
├── Task 10: Create HomeController.java
├── Task 11: Create home/index.html
└── Task 12: Create BookController.java

Wave 7 (After Wave 6):
└── Task 13: Create book/list.html

Wave 8 (Final):
└── Task 14: Full integration verification

Critical Path: 1 → 2 → 4/5 → 6/7 → 8/9 → 12 → 13 → 14
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2, 4, 5, 6, 7, 8, 9, 10, 12 | None (must be first) |
| 2 | 1 | 4, 5, 6, 7, 8, 9, 10, 12 | 3 |
| 3 | 1 | 11, 13 | 2 |
| 4 | 2 | 6, 8 | 5 |
| 5 | 2 | 7, 9 | 4 |
| 6 | 4 | 8 | 7 |
| 7 | 5 | 9 | 6 |
| 8 | 6 | 9, 10 | None |
| 9 | 7, 8 | 12 | None |
| 10 | 8 | 11 | None |
| 11 | 3, 10 | None | 12 |
| 12 | 9 | 13 | 10, 11 |
| 13 | 3, 12 | 14 | None |
| 14 | All | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Approach |
|------|-------|---------------------|
| 1 | 1 | Single task - pom.xml update |
| 2 | 2, 3 | Parallel: config + layout template |
| 3 | 4, 5 | Parallel: both entities |
| 4 | 6, 7 | Parallel: both repositories |
| 5 | 8, 9 | Sequential: CategoryService → BookService |
| 6 | 10, 11, 12 | Parallel: controllers + home view |
| 7 | 13 | Single task - book list view |
| 8 | 14 | Verification task |

---

## TODOs

### Task 1: Update pom.xml with Dependencies

- [ ] 1. Update pom.xml with MongoDB, Thymeleaf, Lombok, Validation dependencies

  **What to do**:
  - Add `spring-boot-starter-data-mongodb` dependency
  - Add `spring-boot-starter-thymeleaf` dependency
  - Add `lombok` dependency with `<optional>true</optional>`
  - Add `spring-boot-starter-validation` dependency
  - Configure Lombok in spring-boot-maven-plugin (exclude from repackage)

  **Must NOT do**:
  - Do NOT add spring-boot-starter-data-jpa
  - Do NOT add any SQL database drivers
  - Do NOT change Spring Boot version (keep 4.0.2)
  - Do NOT change Java version (keep 25)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple XML editing task, well-defined changes
  - **Skills**: None needed
    - This is a straightforward file edit
  
  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (alone)
  - **Blocks**: Tasks 2-13
  - **Blocked By**: None (start immediately)

  **References**:
  
  **Pattern References**:
  - `E:\TranTanTai_22806028_JEE\TranTanTai_Lab03\trantantai\pom.xml` - Current pom.xml to modify (has spring-boot-starter-webmvc)

  **External References**:
  - Spring Boot Starters: The dependencies follow Spring Boot starter convention (spring-boot-starter-*)
  - Lombok configuration: Must exclude from spring-boot-maven-plugin to avoid build issues

  **WHY Each Reference Matters**:
  - pom.xml: Need to add dependencies to existing structure, preserve existing webmvc dependency

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  mvn dependency:resolve -q
  # Assert: Exit code 0 (all dependencies resolve)
  
  mvn compile -q
  # Assert: Exit code 0 (compilation succeeds)
  ```
  
  **Evidence to Capture**:
  - [ ] mvn dependency:resolve output (should show MongoDB, Thymeleaf, Lombok, Validation)

  **Commit**: YES
  - Message: `build: add MongoDB, Thymeleaf, Lombok, Validation dependencies`
  - Files: `pom.xml`

---

### Task 2: Configure MongoDB Atlas Connection

- [ ] 2. Configure application.properties for MongoDB Atlas

  **What to do**:
  - Set `spring.data.mongodb.uri` with Atlas connection string
  - Set `spring.data.mongodb.database=hutech_db`
  - Configure Thymeleaf settings (cache=false for development)
  - Set server port if needed

  **Must NOT do**:
  - Do NOT include JPA/Hibernate properties
  - Do NOT include SQL datasource properties
  - Do NOT hardcode sensitive credentials in multiple places

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple properties file configuration
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 3)
  - **Blocks**: Tasks 4-13
  - **Blocked By**: Task 1

  **References**:
  
  **Pattern References**:
  - `E:\TranTanTai_22806028_JEE\TranTanTai_Lab03\trantantai\src\main\resources\application.properties` - Current file to modify

  **External References**:
  - MongoDB Atlas URI format: `mongodb+srv://user:pass@cluster.mongodb.net/database?options`

  **WHY Each Reference Matters**:
  - application.properties: Need to add MongoDB URI while preserving existing spring.application.name

  **Configuration Values**:
  ```properties
  spring.application.name=trantantai
  
  # MongoDB Atlas Configuration
  spring.data.mongodb.uri=mongodb+srv://hjsad1994:Aa0908700714@java.xy7xyh8.mongodb.net/hutech_db?retryWrites=true&w=majority&appName=Java
  spring.data.mongodb.database=hutech_db
  
  # Thymeleaf Configuration
  spring.thymeleaf.cache=false
  spring.thymeleaf.prefix=classpath:/templates/
  spring.thymeleaf.suffix=.html
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  # Verify properties file contains MongoDB URI
  type src\main\resources\application.properties | findstr "mongodb"
  # Assert: Output contains "spring.data.mongodb.uri"
  ```
  
  **Evidence to Capture**:
  - [ ] Properties file content showing MongoDB configuration

  **Commit**: YES
  - Message: `config: add MongoDB Atlas and Thymeleaf configuration`
  - Files: `src/main/resources/application.properties`

---

### Task 3: Create Base Layout Template

- [ ] 3. Create layout.html with Bootstrap 5.3.x

  **What to do**:
  - Create `src/main/resources/templates/layout.html`
  - Include Bootstrap 5.3.x CSS via CDN
  - Include Bootstrap 5.3.x JS Bundle via CDN
  - Include jQuery 3.7.x via CDN
  - Create header fragment with navigation
  - Create footer fragment
  - Create main content area with th:replace

  **Must NOT do**:
  - Do NOT download Bootstrap/jQuery locally
  - Do NOT use Thymeleaf Layout Dialect
  - Do NOT create complex multi-level layouts

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard HTML template creation
  - **Skills**: None needed (standard Thymeleaf patterns)
  
  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 2)
  - **Blocks**: Tasks 11, 13
  - **Blocked By**: Task 1

  **References**:
  
  **External References**:
  - Bootstrap 5.3.x CDN: `https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css`
  - Bootstrap JS Bundle: `https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js`
  - jQuery CDN: `https://code.jquery.com/jquery-3.7.1.min.js`

  **WHY Each Reference Matters**:
  - CDN links provide Bootstrap styling and JS functionality without local files

  **Template Structure**:
  ```html
  <!DOCTYPE html>
  <html xmlns:th="http://www.thymeleaf.org">
  <head>
      <!-- Bootstrap CSS CDN -->
      <!-- Page title fragment -->
  </head>
  <body>
      <!-- Header fragment with navbar -->
      <div th:fragment="header">...</div>
      
      <!-- Main content replaced by child templates -->
      <main class="container mt-4">
          <div th:replace="~{::content}"></div>
      </main>
      
      <!-- Footer fragment -->
      <div th:fragment="footer">...</div>
      
      <!-- Bootstrap JS + jQuery CDN -->
  </body>
  </html>
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent verifies file exists and contains key elements:
  type src\main\resources\templates\layout.html | findstr "bootstrap"
  # Assert: Output contains "bootstrap" (CDN reference)
  
  type src\main\resources\templates\layout.html | findstr "th:fragment"
  # Assert: Output contains "th:fragment" (Thymeleaf fragments)
  ```
  
  **Evidence to Capture**:
  - [ ] layout.html file content showing Bootstrap CDN and fragments

  **Commit**: YES (groups with Task 11)
  - Message: `feat(ui): add base layout template with Bootstrap 5.3`
  - Files: `src/main/resources/templates/layout.html`

---

### Task 4: Create Category Document Entity

- [ ] 4. Create Category.java MongoDB document

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/entities/Category.java`
  - Add @Document("categories") annotation
  - Add @Id annotation on String id field
  - Add @Size(max = 50) on name field
  - Add Lombok annotations: @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor

  **Must NOT do**:
  - Do NOT use @Entity annotation
  - Do NOT use @Table, @Column annotations
  - Do NOT use @GeneratedValue
  - Do NOT add @DBRef reference to books (avoid circular reference)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple Java class creation with annotations
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 5)
  - **Blocks**: Tasks 6, 8
  - **Blocked By**: Task 2

  **References**:
  
  **External References**:
  - Spring Data MongoDB @Document: Marks class as MongoDB document
  - @Id with String: Auto-maps to MongoDB ObjectId

  **WHY Each Reference Matters**:
  - @Document replaces JPA's @Entity for MongoDB
  - String id allows MongoDB to auto-generate ObjectId

  **Entity Structure**:
  ```java
  package trantantai.trantantai.entities;
  
  @Document("categories")
  @Getter @Setter
  @NoArgsConstructor @AllArgsConstructor
  public class Category {
      @Id
      private String id;
      
      @Size(max = 50, message = "Category name must be at most 50 characters")
      private String name;
  }
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0 (Category.java compiles)
  
  type src\main\java\trantantai\trantantai\entities\Category.java | findstr "@Document"
  # Assert: Output contains "@Document"
  ```
  
  **Evidence to Capture**:
  - [ ] Category.java compiled successfully (no errors)

  **Commit**: YES (groups with Task 5)
  - Message: `feat(entity): add Category MongoDB document`
  - Files: `src/main/java/trantantai/trantantai/entities/Category.java`

---

### Task 5: Create Book Document Entity

- [ ] 5. Create Book.java MongoDB document with categoryId reference

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/entities/Book.java`
  - Add @Document("books") annotation
  - Add @Id annotation on String id field
  - Add @Size(max = 50) on title and author fields
  - Add price field (Double)
  - Add categoryId field (String) for manual reference
  - Add Lombok annotations: @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor

  **Must NOT do**:
  - Do NOT use @Entity annotation
  - Do NOT use @DBRef annotation
  - Do NOT use @JoinColumn
  - Do NOT embed Category object directly

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple Java class creation with annotations
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 4)
  - **Blocks**: Tasks 7, 9
  - **Blocked By**: Task 2

  **References**:
  
  **Pattern References**:
  - Task 4 Category.java for consistent annotation style

  **WHY Each Reference Matters**:
  - Maintain consistent entity patterns across documents

  **Entity Structure**:
  ```java
  package trantantai.trantantai.entities;
  
  @Document("books")
  @Getter @Setter
  @NoArgsConstructor @AllArgsConstructor
  public class Book {
      @Id
      private String id;
      
      @Size(max = 50, message = "Title must be at most 50 characters")
      private String title;
      
      @Size(max = 50, message = "Author must be at most 50 characters")
      private String author;
      
      private Double price;
      
      private String categoryId;  // Manual reference to Category
  }
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0 (Book.java compiles)
  
  type src\main\java\trantantai\trantantai\entities\Book.java | findstr "categoryId"
  # Assert: Output contains "categoryId"
  ```
  
  **Evidence to Capture**:
  - [ ] Book.java compiled successfully with categoryId field

  **Commit**: YES (groups with Task 4)
  - Message: `feat(entity): add Book MongoDB document with category reference`
  - Files: `src/main/java/trantantai/trantantai/entities/Book.java`

---

### Task 6: Create Category Repository Interface

- [ ] 6. Create ICategoryRepository.java extending MongoRepository

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/repositories/ICategoryRepository.java`
  - Extend MongoRepository<Category, String>
  - Add custom query methods if needed (findByName, etc.)

  **Must NOT do**:
  - Do NOT extend JpaRepository
  - Do NOT add @Repository annotation (optional for Spring Data)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple interface creation
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Task 7)
  - **Blocks**: Task 8
  - **Blocked By**: Task 4

  **References**:
  
  **Pattern References**:
  - `src/main/java/trantantai/trantantai/entities/Category.java` - Entity this repo manages

  **WHY Each Reference Matters**:
  - MongoRepository generic types must match Category entity

  **Repository Structure**:
  ```java
  package trantantai.trantantai.repositories;
  
  public interface ICategoryRepository extends MongoRepository<Category, String> {
      Optional<Category> findByName(String name);
  }
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0 (ICategoryRepository.java compiles)
  ```
  
  **Evidence to Capture**:
  - [ ] ICategoryRepository.java compiled successfully

  **Commit**: YES (groups with Task 7)
  - Message: `feat(repo): add Category and Book MongoDB repositories`
  - Files: `src/main/java/trantantai/trantantai/repositories/ICategoryRepository.java`

---

### Task 7: Create Book Repository Interface with Pagination

- [ ] 7. Create IBookRepository.java extending MongoRepository with pagination

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/repositories/IBookRepository.java`
  - Extend MongoRepository<Book, String>
  - Add `Page<Book> findByCategoryId(String categoryId, Pageable pageable)`
  - Add `Page<Book> findByTitleContaining(String keyword, Pageable pageable)` for search

  **Must NOT do**:
  - Do NOT extend JpaRepository
  - Do NOT use @Query with SQL syntax

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple interface creation with Spring Data method naming
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Task 6)
  - **Blocks**: Task 9
  - **Blocked By**: Task 5

  **References**:
  
  **Pattern References**:
  - `src/main/java/trantantai/trantantai/entities/Book.java` - Entity this repo manages

  **External References**:
  - Spring Data derived query methods: Method naming conventions generate queries automatically

  **WHY Each Reference Matters**:
  - Book entity fields determine what query methods can be created

  **Repository Structure**:
  ```java
  package trantantai.trantantai.repositories;
  
  public interface IBookRepository extends MongoRepository<Book, String> {
      Page<Book> findByCategoryId(String categoryId, Pageable pageable);
      Page<Book> findByTitleContaining(String keyword, Pageable pageable);
  }
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0 (IBookRepository.java compiles)
  ```
  
  **Evidence to Capture**:
  - [ ] IBookRepository.java compiled successfully with pagination methods

  **Commit**: YES (groups with Task 6)
  - Message: `feat(repo): add Category and Book MongoDB repositories`
  - Files: `src/main/java/trantantai/trantantai/repositories/IBookRepository.java`

---

### Task 8: Create Category Service

- [ ] 8. Create CategoryService.java with CRUD operations

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/services/CategoryService.java`
  - Add @Service annotation
  - Inject ICategoryRepository
  - Implement: findAll(), findById(), save(), deleteById()

  **Must NOT do**:
  - Do NOT add @Transactional (not needed for MongoDB basic ops)
  - Do NOT add complex business logic

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard service class with repository delegation
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 5 (before Task 9)
  - **Blocks**: Task 9
  - **Blocked By**: Task 6

  **References**:
  
  **Pattern References**:
  - `src/main/java/trantantai/trantantai/repositories/ICategoryRepository.java` - Repository to inject

  **WHY Each Reference Matters**:
  - Service wraps repository methods, uses repository interface

  **Service Structure**:
  ```java
  package trantantai.trantantai.services;
  
  @Service
  @RequiredArgsConstructor
  public class CategoryService {
      private final ICategoryRepository categoryRepository;
      
      public List<Category> findAll() { ... }
      public Optional<Category> findById(String id) { ... }
      public Category save(Category category) { ... }
      public void deleteById(String id) { ... }
  }
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0 (CategoryService.java compiles)
  ```
  
  **Evidence to Capture**:
  - [ ] CategoryService.java compiled successfully

  **Commit**: YES (groups with Task 9)
  - Message: `feat(service): add Category and Book services with CRUD operations`
  - Files: `src/main/java/trantantai/trantantai/services/CategoryService.java`

---

### Task 9: Create Book Service with Pagination

- [ ] 9. Create BookService.java with CRUD and pagination

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/services/BookService.java`
  - Add @Service annotation
  - Inject IBookRepository and CategoryService
  - Implement: findAll(Pageable), findById(), save(), deleteById()
  - Implement: findByCategoryId(String categoryId, Pageable)
  - Add method to resolve category name when needed

  **Must NOT do**:
  - Do NOT auto-fetch category for every book (N+1 problem)
  - Do NOT use complex joins

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard service class with pagination
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 5 (after Task 8)
  - **Blocks**: Task 12
  - **Blocked By**: Tasks 7, 8

  **References**:
  
  **Pattern References**:
  - `src/main/java/trantantai/trantantai/repositories/IBookRepository.java` - Repository to inject
  - `src/main/java/trantantai/trantantai/services/CategoryService.java` - For category resolution

  **WHY Each Reference Matters**:
  - BookService needs IBookRepository for data access
  - CategoryService needed to resolve categoryId to Category name

  **Service Structure**:
  ```java
  package trantantai.trantantai.services;
  
  @Service
  @RequiredArgsConstructor
  public class BookService {
      private final IBookRepository bookRepository;
      private final CategoryService categoryService;
      
      public Page<Book> findAll(Pageable pageable) { ... }
      public Page<Book> findByCategoryId(String categoryId, Pageable pageable) { ... }
      public Optional<Book> findById(String id) { ... }
      public Book save(Book book) { ... }
      public void deleteById(String id) { ... }
  }
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0 (BookService.java compiles)
  ```
  
  **Evidence to Capture**:
  - [ ] BookService.java compiled successfully

  **Commit**: YES (groups with Task 8)
  - Message: `feat(service): add Category and Book services with CRUD operations`
  - Files: `src/main/java/trantantai/trantantai/services/BookService.java`

---

### Task 10: Create Home Controller

- [ ] 10. Create HomeController.java for home page

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/controllers/HomeController.java`
  - Add @Controller annotation
  - Add @GetMapping("/") that returns "home/index"

  **Must NOT do**:
  - Do NOT use @RestController (we're returning views)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple controller with single endpoint
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 6 (with Tasks 11, 12)
  - **Blocks**: Task 11
  - **Blocked By**: Task 8

  **References**:
  
  **External References**:
  - Spring MVC @Controller: Returns view names resolved by Thymeleaf

  **Controller Structure**:
  ```java
  package trantantai.trantantai.controllers;
  
  @Controller
  public class HomeController {
      @GetMapping("/")
      public String home(Model model) {
          return "home/index";
      }
  }
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0 (HomeController.java compiles)
  ```
  
  **Evidence to Capture**:
  - [ ] HomeController.java compiled successfully

  **Commit**: YES (groups with Task 12)
  - Message: `feat(controller): add Home and Book controllers`
  - Files: `src/main/java/trantantai/trantantai/controllers/HomeController.java`

---

### Task 11: Create Home Page View

- [ ] 11. Create home/index.html using layout fragments

  **What to do**:
  - Create `src/main/resources/templates/home/index.html`
  - Include layout fragments (th:replace for header, footer)
  - Add welcome content
  - Add navigation links to book list

  **Must NOT do**:
  - Do NOT duplicate Bootstrap CDN links (already in layout)
  - Do NOT use layout:decorate

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple HTML template
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 6 (with Tasks 10, 12)
  - **Blocks**: None
  - **Blocked By**: Tasks 3, 10

  **References**:
  
  **Pattern References**:
  - `src/main/resources/templates/layout.html` - Layout fragments to include

  **WHY Each Reference Matters**:
  - Must use th:replace to include header/footer fragments from layout.html

  **Template Structure**:
  ```html
  <!DOCTYPE html>
  <html xmlns:th="http://www.thymeleaf.org">
  <head>
      <title>Home - Book Management</title>
      <link rel="stylesheet" th:href="@{...bootstrap...}">
  </head>
  <body>
      <div th:replace="~{layout :: header}"></div>
      
      <main class="container mt-4">
          <h1>Welcome to Book Management System</h1>
          <a th:href="@{/books}" class="btn btn-primary">View Books</a>
      </main>
      
      <div th:replace="~{layout :: footer}"></div>
      <script th:src="@{...bootstrap...}"></script>
  </body>
  </html>
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent verifies file exists:
  type src\main\resources\templates\home\index.html | findstr "th:replace"
  # Assert: Output contains "th:replace" (using layout fragments)
  ```
  
  **Evidence to Capture**:
  - [ ] home/index.html content with layout fragment includes

  **Commit**: YES (groups with Task 13)
  - Message: `feat(ui): add home and book list views with pagination`
  - Files: `src/main/resources/templates/home/index.html`

---

### Task 12: Create Book Controller with Pagination

- [ ] 12. Create BookController.java with paginated listing

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/controllers/BookController.java`
  - Add @Controller annotation
  - Inject BookService
  - Add @GetMapping("/books") with pagination parameters
  - Use @RequestParam for page (default 0), size (default 20)
  - Add Page<Book> to model for view

  **Must NOT do**:
  - Do NOT use @RestController
  - Do NOT hardcode page size (use parameter with default)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard controller with pagination pattern
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 6 (with Tasks 10, 11)
  - **Blocks**: Task 13
  - **Blocked By**: Task 9

  **References**:
  
  **Pattern References**:
  - `src/main/java/trantantai/trantantai/services/BookService.java` - Service to inject

  **External References**:
  - Spring Data Pageable: PageRequest.of(page, size) creates pagination

  **WHY Each Reference Matters**:
  - BookService.findAll(Pageable) returns Page<Book> for pagination

  **Controller Structure**:
  ```java
  package trantantai.trantantai.controllers;
  
  @Controller
  @RequiredArgsConstructor
  public class BookController {
      private final BookService bookService;
      
      @GetMapping("/books")
      public String listBooks(
              @RequestParam(defaultValue = "0") int page,
              @RequestParam(defaultValue = "20") int size,
              Model model) {
          Pageable pageable = PageRequest.of(page, size);
          Page<Book> books = bookService.findAll(pageable);
          model.addAttribute("books", books);
          return "book/list";
      }
  }
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0 (BookController.java compiles)
  
  type src\main\java\trantantai\trantantai\controllers\BookController.java | findstr "defaultValue"
  # Assert: Output contains "defaultValue" (pagination params)
  ```
  
  **Evidence to Capture**:
  - [ ] BookController.java compiled with pagination parameters

  **Commit**: YES (groups with Task 10)
  - Message: `feat(controller): add Home and Book controllers`
  - Files: `src/main/java/trantantai/trantantai/controllers/BookController.java`

---

### Task 13: Create Book List View with Pagination

- [ ] 13. Create book/list.html with paginated table

  **What to do**:
  - Create `src/main/resources/templates/book/list.html`
  - Include layout fragments
  - Create Bootstrap table with Book columns (title, author, price)
  - Add pagination controls using Page metadata (first, last, number, totalPages)
  - Use th:each for iterating books
  - Use th:href for pagination links with page parameter

  **Must NOT do**:
  - Do NOT use JavaScript-only pagination
  - Do NOT hardcode page numbers

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Thymeleaf template with standard pagination pattern
  - **Skills**: None needed
  
  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 7 (alone)
  - **Blocks**: Task 14
  - **Blocked By**: Tasks 3, 12

  **References**:
  
  **Pattern References**:
  - `src/main/resources/templates/layout.html` - Layout fragments
  - `src/main/java/trantantai/trantantai/controllers/BookController.java` - Model attributes

  **External References**:
  - Spring Data Page interface: provides totalPages, number, first, last, content

  **WHY Each Reference Matters**:
  - Controller puts Page<Book> in model as "books"
  - Page object has metadata for pagination UI (totalPages, isFirst, isLast)

  **Template Structure**:
  ```html
  <!DOCTYPE html>
  <html xmlns:th="http://www.thymeleaf.org">
  <head>
      <title>Books - Book Management</title>
      <link rel="stylesheet" th:href="@{...bootstrap...}">
  </head>
  <body>
      <div th:replace="~{layout :: header}"></div>
      
      <main class="container mt-4">
          <h1>Book List</h1>
          
          <table class="table table-striped">
              <thead>
                  <tr>
                      <th>Title</th>
                      <th>Author</th>
                      <th>Price</th>
                  </tr>
              </thead>
              <tbody>
                  <tr th:each="book : ${books.content}">
                      <td th:text="${book.title}"></td>
                      <td th:text="${book.author}"></td>
                      <td th:text="${book.price}"></td>
                  </tr>
              </tbody>
          </table>
          
          <!-- Pagination -->
          <nav>
              <ul class="pagination">
                  <li class="page-item" th:classappend="${books.first} ? 'disabled'">
                      <a class="page-link" th:href="@{/books(page=${books.number - 1})}">Previous</a>
                  </li>
                  <li class="page-item" th:each="i : ${#numbers.sequence(0, books.totalPages - 1)}"
                      th:classappend="${i == books.number} ? 'active'">
                      <a class="page-link" th:href="@{/books(page=${i})}" th:text="${i + 1}"></a>
                  </li>
                  <li class="page-item" th:classappend="${books.last} ? 'disabled'">
                      <a class="page-link" th:href="@{/books(page=${books.number + 1})}">Next</a>
                  </li>
              </ul>
          </nav>
      </main>
      
      <div th:replace="~{layout :: footer}"></div>
      <script th:src="@{...bootstrap...}"></script>
  </body>
  </html>
  ```

  **Acceptance Criteria**:
  
  ```bash
  # Agent verifies file exists with pagination:
  type src\main\resources\templates\book\list.html | findstr "pagination"
  # Assert: Output contains "pagination" (Bootstrap pagination)
  
  type src\main\resources\templates\book\list.html | findstr "th:each"
  # Assert: Output contains "th:each" (iterating books)
  ```
  
  **Evidence to Capture**:
  - [ ] book/list.html content with table and pagination controls

  **Commit**: YES (groups with Task 11)
  - Message: `feat(ui): add home and book list views with pagination`
  - Files: `src/main/resources/templates/book/list.html`

---

### Task 14: Full Integration Verification

- [ ] 14. Verify complete application functionality

  **What to do**:
  - Run application with `mvn spring-boot:run`
  - Verify MongoDB connection in logs
  - Access home page at http://localhost:8080/
  - Access book list at http://localhost:8080/books
  - Verify pagination controls appear (even with empty data)
  - Optional: Insert test data via MongoDB shell or Compass

  **Must NOT do**:
  - Do NOT add test data programmatically (not in scope)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Requires browser verification of UI
  - **Skills**: [`playwright`]
    - playwright: For browser automation to verify pages render correctly
  
  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 8 (final, alone)
  - **Blocks**: None
  - **Blocked By**: All previous tasks

  **References**:
  
  **Pattern References**:
  - All created files

  **WHY Each Reference Matters**:
  - This task verifies all components work together

  **Acceptance Criteria**:
  
  ```bash
  # Agent starts application in background, then verifies:
  # 1. Start app
  cd trantantai && start /B mvn spring-boot:run
  # Wait for startup (check for "Started TrantantaiApplication")
  
  # 2. Via playwright skill - navigate and verify:
  # - http://localhost:8080/ shows "Welcome" or home content
  # - http://localhost:8080/books shows book list table
  # - Pagination controls are visible
  # - No error pages (500, 404)
  
  # 3. Screenshot evidence saved to .sisyphus/evidence/
  ```
  
  **Evidence to Capture**:
  - [ ] Application startup log showing "Started TrantantaiApplication"
  - [ ] Screenshot of home page
  - [ ] Screenshot of book list page with pagination

  **Commit**: NO (verification only)

---

## Commit Strategy

| After Tasks | Message | Files |
|-------------|---------|-------|
| 1 | `build: add MongoDB, Thymeleaf, Lombok, Validation dependencies` | pom.xml |
| 2 | `config: add MongoDB Atlas and Thymeleaf configuration` | application.properties |
| 3, 11, 13 | `feat(ui): add layout, home page, and book list templates` | templates/*.html |
| 4, 5 | `feat(entity): add Book and Category MongoDB documents` | entities/*.java |
| 6, 7 | `feat(repo): add Category and Book MongoDB repositories` | repositories/*.java |
| 8, 9 | `feat(service): add Category and Book services` | services/*.java |
| 10, 12 | `feat(controller): add Home and Book controllers` | controllers/*.java |

---

## Success Criteria

### Verification Commands
```bash
# In trantantai directory:
mvn spring-boot:run

# Expected: Application starts, logs show:
# - "Started TrantantaiApplication in X seconds"
# - No MongoDB connection errors
```

### Final Checklist
- [ ] All Java files compile without errors
- [ ] MongoDB Atlas connection successful
- [ ] Home page renders at http://localhost:8080/
- [ ] Book list renders at http://localhost:8080/books
- [ ] Pagination controls visible on book list
- [ ] Bootstrap styling applied correctly
- [ ] No @Entity, @Table, @Column JPA annotations present
- [ ] No @DBRef annotations (using manual reference)
