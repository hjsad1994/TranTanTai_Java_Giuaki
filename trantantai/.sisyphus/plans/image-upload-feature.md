# Image Upload Feature for Books and Reviews

## TL;DR

> **Quick Summary**: Implement Cloudinary-based image upload for Books (Admin) and Reviews (User) in a Spring Boot 4.0.2 + MongoDB application. Admin can upload multiple gallery images for books; users can upload up to 3 images when creating reviews.
> 
> **Deliverables**:
> - Cloudinary integration with configuration and service layer
> - Book entity extended with `imageUrls` field + updated ViewModels
> - Review entity extended with `imageUrls` field (max 3) + updated ViewModels
> - Image upload REST endpoints with proper security
> - Image validation (type, size) and error handling
> - Swagger documentation for all new endpoints
> 
> **Estimated Effort**: Medium (8-12 tasks)
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 → Task 2 → Task 3 → Tasks 4-7 (parallel) → Tasks 8-9 (parallel) → Task 10

---

## Context

### Original Request
Implement backend image upload functionality for:
1. **Book Images (Admin)**: Admin uploads multiple images when creating/editing books
2. **Review Images (User)**: Users upload up to 3 images when creating reviews

### Interview Summary
**Key Discussions**:
- Storage: Cloudinary (cloud-based image CDN) - simplifies architecture, no GridFS needed
- Review editing: Creation only (no image editing after review is created)
- Max review images: 3 per review
- Image compression: Cloudinary handles automatically with transformations
- Security: ADMIN for book images, USER for review images (must have purchased book)

**Research Findings**:
- Cloudinary Java SDK: `com.cloudinary:cloudinary-http44:1.37.0`
- Upload returns secure URL stored in MongoDB
- Cloudinary provides automatic optimization, CDN delivery
- Existing codebase uses Java records for ViewModels, @Document for entities

### Metis Review (Self-Analysis)
**Identified Gaps** (addressed):
- Cloud name needed for Cloudinary config → Will derive from API key format
- Delete orphan images consideration → Out of scope for MVP, can be added later
- CSRF handling for multipart → Already disabled for `/api/**` pattern
- Image serving endpoint → Not needed, Cloudinary URLs are public CDN

---

## Work Objectives

### Core Objective
Enable image uploads for Books (admin gallery) and Reviews (user attachments) using Cloudinary, storing URLs in MongoDB entities.

### Concrete Deliverables
- `CloudinaryConfig.java` - Configuration bean with credentials
- `ImageUploadService.java` - Cloudinary upload/delete operations
- `ImageValidator.java` - File type and size validation
- Modified `Book.java` - Added `List<String> imageUrls` field
- Modified `Review.java` - Added `List<String> imageUrls` field (max 3)
- `ImageUploadVm.java` - Request ViewModel
- `ImageGetVm.java` - Response ViewModel
- Updated `BookPostVm.java`, `BookGetVm.java` - With imageUrls field
- Updated `ReviewPostVm.java`, `ReviewGetVm.java` - With imageUrls field
- `ImageApiController.java` - Upload endpoints
- Updated `SecurityConfig.java` - Endpoint protection rules
- Updated `application.properties` - Cloudinary credentials and multipart config

### Definition of Done
- [ ] `POST /api/v1/images/books` uploads image and returns Cloudinary URL (ADMIN only)
- [ ] `POST /api/v1/images/reviews` uploads image and returns Cloudinary URL (USER only)
- [ ] `DELETE /api/v1/images/{publicId}` deletes image from Cloudinary (owner only)
- [ ] Book entity stores list of image URLs
- [ ] Review entity stores list of image URLs (max 3 enforced)
- [ ] Image validation rejects non-image files and files > 5MB
- [ ] Swagger UI documents all new endpoints
- [ ] All existing tests still pass

### Must Have
- Cloudinary SDK integration with proper configuration
- Image upload endpoints with role-based security
- File validation: JPEG, PNG, GIF, WebP only, max 5MB
- Image URLs stored in Book and Review entities
- Proper error handling and HTTP status codes

### Must NOT Have (Guardrails)
- ❌ Do NOT implement image editing for reviews (creation only)
- ❌ Do NOT store image bytes in MongoDB (URLs only)
- ❌ Do NOT implement thumbnail generation server-side (Cloudinary handles this)
- ❌ Do NOT add image processing dependencies (Thumbnailator, etc.)
- ❌ Do NOT create separate Image entity - embed URLs directly in Book/Review
- ❌ Do NOT modify existing CRUD logic beyond adding imageUrls field
- ❌ Do NOT implement orphan image cleanup (out of scope for MVP)

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: YES (spring-boot-starter-test in pom.xml)
- **User wants tests**: Manual verification (Swagger + curl)
- **Framework**: Spring Boot Test

### Automated Verification (API Testing via curl/Swagger)

Each TODO includes verification procedures that can be executed via curl commands or Swagger UI.

**Evidence Requirements:**
- HTTP response codes and bodies captured
- Cloudinary URLs verified accessible
- MongoDB documents verified with proper fields

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Foundation - Sequential):
└── Task 1: Add Cloudinary dependency + config properties
    └── Task 2: Create CloudinaryConfig bean
        └── Task 3: Create ImageUploadService + ImageValidator

Wave 2 (Entity & ViewModel Updates - Parallel):
├── Task 4: Update Book entity + ViewModels
├── Task 5: Update Review entity + ViewModels  
├── Task 6: Create ImageUploadVm and ImageGetVm
└── Task 7: Update BookService + ReviewService (add imageUrls handling)

Wave 3 (API Layer - Parallel after Wave 2):
├── Task 8: Create ImageApiController
└── Task 9: Update SecurityConfig

Wave 4 (Final):
└── Task 10: Integration testing and Swagger verification
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2 | None |
| 2 | 1 | 3 | None |
| 3 | 2 | 4, 5, 6, 7, 8 | None |
| 4 | 3 | 7, 10 | 5, 6 |
| 5 | 3 | 7, 10 | 4, 6 |
| 6 | 3 | 8 | 4, 5 |
| 7 | 4, 5 | 10 | 6 |
| 8 | 3, 6 | 9, 10 | 7 |
| 9 | 8 | 10 | 7 |
| 10 | 7, 8, 9 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Dispatch |
|------|-------|---------------------|
| 1 | 1, 2, 3 | Sequential - foundational setup |
| 2 | 4, 5, 6, 7 | Parallel after Task 3 completes |
| 3 | 8, 9 | Parallel after Task 6 completes |
| 4 | 10 | Final integration verification |

---

## TODOs

- [ ] 1. Add Cloudinary Dependency and Configuration Properties

  **What to do**:
  - Add Cloudinary Java SDK dependency to `pom.xml`:
    ```xml
    <dependency>
        <groupId>com.cloudinary</groupId>
        <artifactId>cloudinary-http44</artifactId>
        <version>1.37.0</version>
    </dependency>
    ```
  - Add configuration properties to `application.properties`:
    ```properties
    # Cloudinary Configuration
    cloudinary.cloud-name=YOUR_CLOUD_NAME
    cloudinary.api-key=175823613897388
    cloudinary.api-secret=02Fh3kbm0xrcejIOzNP0210ZlRE
    
    # Multipart File Upload Configuration
    spring.servlet.multipart.enabled=true
    spring.servlet.multipart.max-file-size=5MB
    spring.servlet.multipart.max-request-size=10MB
    ```
  - Note: Cloud name can be found in Cloudinary dashboard (usually derived from account)

  **Must NOT do**:
  - Do NOT commit API secrets to version control (for production, use environment variables)
  - Do NOT add unnecessary dependencies

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple configuration file edits, no complex logic
  - **Skills**: None needed
  - **Skills Evaluated but Omitted**:
    - `git-master`: Not needed for this task

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (sequential)
  - **Blocks**: Task 2
  - **Blocked By**: None (can start immediately)

  **References**:
  - `pom.xml:32-98` - Existing dependencies section, add new dependency here
  - `src/main/resources/application.properties:1-38` - Existing config, add Cloudinary props at end
  - Cloudinary SDK docs: https://cloudinary.com/documentation/java_integration

  **Acceptance Criteria**:
  ```bash
  # Verify dependency added to pom.xml
  grep -q "cloudinary-http44" pom.xml && echo "PASS: Cloudinary dependency found" || echo "FAIL"
  
  # Verify properties added
  grep -q "cloudinary.api-key" src/main/resources/application.properties && echo "PASS: Cloudinary config found" || echo "FAIL"
  
  # Verify multipart config added  
  grep -q "spring.servlet.multipart.max-file-size" src/main/resources/application.properties && echo "PASS: Multipart config found" || echo "FAIL"
  ```

  **Commit**: YES
  - Message: `feat(config): add Cloudinary SDK dependency and configuration`
  - Files: `pom.xml`, `src/main/resources/application.properties`

---

- [ ] 2. Create CloudinaryConfig Bean

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/config/CloudinaryConfig.java`
  - Configure Cloudinary bean with credentials from properties:
    ```java
    @Configuration
    public class CloudinaryConfig {
        @Value("${cloudinary.cloud-name}")
        private String cloudName;
        
        @Value("${cloudinary.api-key}")
        private String apiKey;
        
        @Value("${cloudinary.api-secret}")
        private String apiSecret;
        
        @Bean
        public Cloudinary cloudinary() {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            config.put("secure", "true");
            return new Cloudinary(config);
        }
    }
    ```

  **Must NOT do**:
  - Do NOT hardcode credentials in the class
  - Do NOT create multiple Cloudinary instances

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple Spring configuration class creation
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (sequential)
  - **Blocks**: Task 3
  - **Blocked By**: Task 1

  **References**:
  - `src/main/java/trantantai/trantantai/config/MoMoConfig.java` - Similar config pattern with @Value injection
  - `src/main/java/trantantai/trantantai/config/SecurityConfig.java` - @Configuration class pattern

  **Acceptance Criteria**:
  ```bash
  # Verify file exists
  test -f src/main/java/trantantai/trantantai/config/CloudinaryConfig.java && echo "PASS: CloudinaryConfig exists" || echo "FAIL"
  
  # Verify @Configuration annotation
  grep -q "@Configuration" src/main/java/trantantai/trantantai/config/CloudinaryConfig.java && echo "PASS: Has @Configuration" || echo "FAIL"
  
  # Verify @Bean method
  grep -q "@Bean" src/main/java/trantantai/trantantai/config/CloudinaryConfig.java && echo "PASS: Has @Bean" || echo "FAIL"
  
  # Verify Cloudinary return type
  grep -q "public Cloudinary cloudinary()" src/main/java/trantantai/trantantai/config/CloudinaryConfig.java && echo "PASS: Returns Cloudinary" || echo "FAIL"
  ```

  **Commit**: YES
  - Message: `feat(config): create CloudinaryConfig bean for image upload`
  - Files: `src/main/java/trantantai/trantantai/config/CloudinaryConfig.java`

---

- [ ] 3. Create ImageUploadService and ImageValidator

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/services/ImageUploadService.java`:
    - Inject Cloudinary bean
    - `uploadImage(MultipartFile file, String folder)` - uploads to Cloudinary, returns URL
    - `deleteImage(String publicId)` - deletes from Cloudinary
    - Handle Cloudinary API responses and errors
  - Create `src/main/java/trantantai/trantantai/validators/ImageValidator.java`:
    - `validateImage(MultipartFile file)` - validates file type and size
    - Allowed types: image/jpeg, image/png, image/gif, image/webp
    - Max size: 5MB
    - Throw descriptive exceptions on validation failure

  **Must NOT do**:
  - Do NOT store file bytes locally
  - Do NOT implement compression (Cloudinary handles it)
  - Do NOT create thumbnails server-side

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Service class with clear input/output, follows existing patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (sequential)
  - **Blocks**: Tasks 4, 5, 6, 7, 8
  - **Blocked By**: Task 2

  **References**:
  - `src/main/java/trantantai/trantantai/services/ReviewService.java:24-40` - Service pattern with constructor injection
  - `src/main/java/trantantai/trantantai/validators/ValidCategoryIdValidator.java` - Validator pattern
  - Cloudinary upload docs: https://cloudinary.com/documentation/java_image_and_video_upload

  **Acceptance Criteria**:
  ```bash
  # Verify ImageUploadService exists
  test -f src/main/java/trantantai/trantantai/services/ImageUploadService.java && echo "PASS" || echo "FAIL"
  
  # Verify ImageValidator exists  
  test -f src/main/java/trantantai/trantantai/validators/ImageValidator.java && echo "PASS" || echo "FAIL"
  
  # Verify uploadImage method
  grep -q "uploadImage" src/main/java/trantantai/trantantai/services/ImageUploadService.java && echo "PASS" || echo "FAIL"
  
  # Verify deleteImage method
  grep -q "deleteImage" src/main/java/trantantai/trantantai/services/ImageUploadService.java && echo "PASS" || echo "FAIL"
  
  # Verify validateImage method
  grep -q "validateImage" src/main/java/trantantai/trantantai/validators/ImageValidator.java && echo "PASS" || echo "FAIL"
  ```

  **Commit**: YES
  - Message: `feat(service): add ImageUploadService and ImageValidator for Cloudinary uploads`
  - Files: `src/main/java/trantantai/trantantai/services/ImageUploadService.java`, `src/main/java/trantantai/trantantai/validators/ImageValidator.java`

---

- [ ] 4. Update Book Entity and ViewModels with Image Fields

  **What to do**:
  - Modify `src/main/java/trantantai/trantantai/entities/Book.java`:
    - Add `private List<String> imageUrls = new ArrayList<>();` field
    - Add getter/setter for imageUrls
    - Update constructors and toString()
  - Modify `src/main/java/trantantai/trantantai/viewmodels/BookPostVm.java`:
    - Add `List<String> imageUrls` field to record
    - Update `from()` method if exists
  - Modify `src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java`:
    - Add `List<String> imageUrls` field to record
    - Update `from()` method to include imageUrls

  **Must NOT do**:
  - Do NOT change existing field names or types
  - Do NOT modify validation annotations on other fields
  - Do NOT create separate Image entity

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Entity field addition following existing patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6)
  - **Blocks**: Task 7, Task 10
  - **Blocked By**: Task 3

  **References**:
  - `src/main/java/trantantai/trantantai/entities/Book.java:16-140` - Current Book entity structure
  - `src/main/java/trantantai/trantantai/viewmodels/BookPostVm.java:1-30` - Current BookPostVm record
  - `src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java` - Current BookGetVm record with from() method

  **Acceptance Criteria**:
  ```bash
  # Verify imageUrls field in Book entity
  grep -q "imageUrls" src/main/java/trantantai/trantantai/entities/Book.java && echo "PASS" || echo "FAIL"
  
  # Verify List<String> type
  grep -q "List<String> imageUrls" src/main/java/trantantai/trantantai/entities/Book.java && echo "PASS" || echo "FAIL"
  
  # Verify imageUrls in BookPostVm
  grep -q "imageUrls" src/main/java/trantantai/trantantai/viewmodels/BookPostVm.java && echo "PASS" || echo "FAIL"
  
  # Verify imageUrls in BookGetVm
  grep -q "imageUrls" src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java && echo "PASS" || echo "FAIL"
  ```

  **Commit**: YES
  - Message: `feat(entity): add imageUrls field to Book entity and ViewModels`
  - Files: `src/main/java/trantantai/trantantai/entities/Book.java`, `src/main/java/trantantai/trantantai/viewmodels/BookPostVm.java`, `src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java`

---

- [ ] 5. Update Review Entity and ViewModels with Image Fields

  **What to do**:
  - Modify `src/main/java/trantantai/trantantai/entities/Review.java`:
    - Add `private List<String> imageUrls = new ArrayList<>();` field
    - Add getter/setter for imageUrls
    - Update constructors and toString()
  - Modify `src/main/java/trantantai/trantantai/viewmodels/ReviewPostVm.java`:
    - Add `@Size(max = 3, message = "Maximum 3 images allowed") List<String> imageUrls` field
    - Add validation annotation for max 3 images
  - Modify `src/main/java/trantantai/trantantai/viewmodels/ReviewGetVm.java`:
    - Add `List<String> imageUrls` field to record
    - Update `from()` method to include imageUrls

  **Must NOT do**:
  - Do NOT allow more than 3 images per review
  - Do NOT change existing Review validation logic
  - Do NOT add image editing capability

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Entity field addition following existing patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 6)
  - **Blocks**: Task 7, Task 10
  - **Blocked By**: Task 3

  **References**:
  - `src/main/java/trantantai/trantantai/entities/Review.java:1-156` - Current Review entity structure
  - `src/main/java/trantantai/trantantai/viewmodels/ReviewPostVm.java:1-28` - Current ReviewPostVm record with validation
  - `src/main/java/trantantai/trantantai/viewmodels/ReviewGetVm.java:1-57` - Current ReviewGetVm record with from() method

  **Acceptance Criteria**:
  ```bash
  # Verify imageUrls field in Review entity
  grep -q "imageUrls" src/main/java/trantantai/trantantai/entities/Review.java && echo "PASS" || echo "FAIL"
  
  # Verify List<String> type
  grep -q "List<String> imageUrls" src/main/java/trantantai/trantantai/entities/Review.java && echo "PASS" || echo "FAIL"
  
  # Verify max 3 validation in ReviewPostVm
  grep -q "@Size" src/main/java/trantantai/trantantai/viewmodels/ReviewPostVm.java && echo "PASS" || echo "FAIL"
  
  # Verify imageUrls in ReviewGetVm
  grep -q "imageUrls" src/main/java/trantantai/trantantai/viewmodels/ReviewGetVm.java && echo "PASS" || echo "FAIL"
  ```

  **Commit**: YES
  - Message: `feat(entity): add imageUrls field to Review entity and ViewModels (max 3)`
  - Files: `src/main/java/trantantai/trantantai/entities/Review.java`, `src/main/java/trantantai/trantantai/viewmodels/ReviewPostVm.java`, `src/main/java/trantantai/trantantai/viewmodels/ReviewGetVm.java`

---

- [ ] 6. Create Image Upload/Response ViewModels

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/viewmodels/ImageUploadVm.java`:
    ```java
    @Schema(description = "Image upload response model")
    public record ImageUploadVm(
        @Schema(description = "Cloudinary public ID for deletion")
        String publicId,
        @Schema(description = "Full URL to the uploaded image")
        String url,
        @Schema(description = "Original filename")
        String originalFilename,
        @Schema(description = "File size in bytes")
        long size
    ) {}
    ```
  - Create `src/main/java/trantantai/trantantai/viewmodels/ImageDeleteVm.java`:
    ```java
    @Schema(description = "Image deletion request model")
    public record ImageDeleteVm(
        @Schema(description = "Cloudinary public ID to delete")
        @NotBlank(message = "Public ID is required")
        String publicId
    ) {}
    ```

  **Must NOT do**:
  - Do NOT include sensitive Cloudinary metadata in response
  - Do NOT expose internal implementation details

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple record class creation following existing ViewModel patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5)
  - **Blocks**: Task 8
  - **Blocked By**: Task 3

  **References**:
  - `src/main/java/trantantai/trantantai/viewmodels/ReviewGetVm.java:1-57` - Record ViewModel pattern with @Schema
  - `src/main/java/trantantai/trantantai/viewmodels/CanReviewVm.java` - Simple response record pattern

  **Acceptance Criteria**:
  ```bash
  # Verify ImageUploadVm exists
  test -f src/main/java/trantantai/trantantai/viewmodels/ImageUploadVm.java && echo "PASS" || echo "FAIL"
  
  # Verify ImageDeleteVm exists
  test -f src/main/java/trantantai/trantantai/viewmodels/ImageDeleteVm.java && echo "PASS" || echo "FAIL"
  
  # Verify @Schema annotation
  grep -q "@Schema" src/main/java/trantantai/trantantai/viewmodels/ImageUploadVm.java && echo "PASS" || echo "FAIL"
  
  # Verify record type
  grep -q "public record ImageUploadVm" src/main/java/trantantai/trantantai/viewmodels/ImageUploadVm.java && echo "PASS" || echo "FAIL"
  ```

  **Commit**: YES
  - Message: `feat(viewmodel): add ImageUploadVm and ImageDeleteVm for image API responses`
  - Files: `src/main/java/trantantai/trantantai/viewmodels/ImageUploadVm.java`, `src/main/java/trantantai/trantantai/viewmodels/ImageDeleteVm.java`

---

- [ ] 7. Update BookService and ReviewService for Image URL Handling

  **What to do**:
  - Modify `src/main/java/trantantai/trantantai/services/BookService.java`:
    - Ensure `addBook()` and `updateBook()` methods handle imageUrls field
    - No special logic needed - just ensure field is persisted
  - Modify `src/main/java/trantantai/trantantai/services/ReviewService.java`:
    - Update `addReview()` method signature to accept `List<String> imageUrls`
    - Validate max 3 images in service layer (defense in depth)
    - Set imageUrls on Review entity before save

  **Must NOT do**:
  - Do NOT modify existing business logic (purchase validation, etc.)
  - Do NOT add image upload logic to these services (that's in ImageUploadService)
  - Do NOT change method return types

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Minor service method updates
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES (after Tasks 4, 5)
  - **Parallel Group**: Wave 2 (can run with Task 6 after 4, 5 complete)
  - **Blocks**: Task 10
  - **Blocked By**: Tasks 4, 5

  **References**:
  - `src/main/java/trantantai/trantantai/services/BookService.java` - Current book service
  - `src/main/java/trantantai/trantantai/services/ReviewService.java:67-92` - addReview() method to modify
  - `src/main/java/trantantai/trantantai/controllers/ReviewApiController.java:126-163` - How addReview is called

  **Acceptance Criteria**:
  ```bash
  # Verify ReviewService addReview has imageUrls parameter
  grep -q "imageUrls" src/main/java/trantantai/trantantai/services/ReviewService.java && echo "PASS" || echo "FAIL"
  
  # Verify max 3 validation exists
  grep -q "3" src/main/java/trantantai/trantantai/services/ReviewService.java && echo "PASS (check for max 3 logic)" || echo "NEEDS REVIEW"
  ```

  **Commit**: YES
  - Message: `feat(service): update BookService and ReviewService to handle imageUrls`
  - Files: `src/main/java/trantantai/trantantai/services/BookService.java`, `src/main/java/trantantai/trantantai/services/ReviewService.java`

---

- [ ] 8. Create ImageApiController with Upload Endpoints

  **What to do**:
  - Create `src/main/java/trantantai/trantantai/controllers/ImageApiController.java`:
    - `@RestController` with `@RequestMapping("/api/v1/images")`
    - `@Tag(name = "Images", description = "Image upload APIs")`
    - `POST /books` - Upload book image (ADMIN only via @PreAuthorize)
    - `POST /reviews` - Upload review image (USER only via @PreAuthorize)
    - `DELETE /{publicId}` - Delete image (authenticated, ownership check)
    - Use `@RequestPart("file") MultipartFile file` for uploads
    - Inject ImageUploadService and ImageValidator
    - Return `ResponseEntity<ImageUploadVm>` on success
    - Handle and return proper error responses

  **Must NOT do**:
  - Do NOT allow anonymous uploads
  - Do NOT skip image validation
  - Do NOT expose Cloudinary API credentials in responses

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: REST controller following existing patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES (after Task 6)
  - **Parallel Group**: Wave 3 (with Task 9)
  - **Blocks**: Task 9, Task 10
  - **Blocked By**: Tasks 3, 6

  **References**:
  - `src/main/java/trantantai/trantantai/controllers/BookApiController.java:1-176` - REST controller pattern with OpenAPI annotations
  - `src/main/java/trantantai/trantantai/controllers/ReviewApiController.java:126-163` - @AuthenticationPrincipal usage pattern
  - `src/main/java/trantantai/trantantai/config/SecurityConfig.java:43-75` - Security rules pattern

  **Acceptance Criteria**:
  ```bash
  # Verify ImageApiController exists
  test -f src/main/java/trantantai/trantantai/controllers/ImageApiController.java && echo "PASS" || echo "FAIL"
  
  # Verify @RestController annotation
  grep -q "@RestController" src/main/java/trantantai/trantantai/controllers/ImageApiController.java && echo "PASS" || echo "FAIL"
  
  # Verify @Tag annotation for Swagger
  grep -q "@Tag" src/main/java/trantantai/trantantai/controllers/ImageApiController.java && echo "PASS" || echo "FAIL"
  
  # Verify POST mapping for books
  grep -q "PostMapping.*books" src/main/java/trantantai/trantantai/controllers/ImageApiController.java && echo "PASS" || echo "FAIL"
  
  # Verify POST mapping for reviews
  grep -q "PostMapping.*reviews" src/main/java/trantantai/trantantai/controllers/ImageApiController.java && echo "PASS" || echo "FAIL"
  
  # Verify DELETE mapping
  grep -q "DeleteMapping" src/main/java/trantantai/trantantai/controllers/ImageApiController.java && echo "PASS" || echo "FAIL"
  ```

  **Commit**: YES
  - Message: `feat(api): add ImageApiController with book and review image upload endpoints`
  - Files: `src/main/java/trantantai/trantantai/controllers/ImageApiController.java`

---

- [ ] 9. Update SecurityConfig for Image Endpoints

  **What to do**:
  - Modify `src/main/java/trantantai/trantantai/config/SecurityConfig.java`:
    - Add rule: `.requestMatchers(HttpMethod.POST, "/api/v1/images/books/**").hasRole("ADMIN")`
    - Add rule: `.requestMatchers(HttpMethod.POST, "/api/v1/images/reviews/**").hasRole("USER")`
    - Add rule: `.requestMatchers(HttpMethod.DELETE, "/api/v1/images/**").authenticated()`
    - Place these rules BEFORE the generic `/api/**` rule
  - Ensure CSRF is properly handled (already disabled for `/api/**` via existing config)

  **Must NOT do**:
  - Do NOT remove existing security rules
  - Do NOT make image endpoints public
  - Do NOT change CSRF configuration

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Security configuration update following existing patterns
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES (after Task 8)
  - **Parallel Group**: Wave 3 (after Task 8)
  - **Blocks**: Task 10
  - **Blocked By**: Task 8

  **References**:
  - `src/main/java/trantantai/trantantai/config/SecurityConfig.java:43-75` - Existing security rules
  - `src/main/java/trantantai/trantantai/config/SecurityConfig.java:49-50` - ADMIN role pattern
  - `src/main/java/trantantai/trantantai/config/SecurityConfig.java:56-57` - USER role pattern

  **Acceptance Criteria**:
  ```bash
  # Verify book image endpoint rule
  grep -q "images/books" src/main/java/trantantai/trantantai/config/SecurityConfig.java && echo "PASS" || echo "FAIL"
  
  # Verify review image endpoint rule
  grep -q "images/reviews" src/main/java/trantantai/trantantai/config/SecurityConfig.java && echo "PASS" || echo "FAIL"
  
  # Verify ADMIN role for book images
  grep -q "images/books.*ADMIN" src/main/java/trantantai/trantantai/config/SecurityConfig.java && echo "PASS" || echo "FAIL"
  
  # Verify USER role for review images  
  grep -q "images/reviews.*USER" src/main/java/trantantai/trantantai/config/SecurityConfig.java && echo "PASS" || echo "FAIL"
  ```

  **Commit**: YES
  - Message: `feat(security): add security rules for image upload endpoints`
  - Files: `src/main/java/trantantai/trantantai/config/SecurityConfig.java`

---

- [ ] 10. Integration Testing and Swagger Verification

  **What to do**:
  - Start the application: `mvn spring-boot:run`
  - Verify Swagger UI shows new Image endpoints at `/swagger-ui.html`
  - Test book image upload (as ADMIN):
    ```bash
    curl -X POST http://localhost:8080/api/v1/images/books \
      -H "Cookie: JSESSIONID=<admin-session>" \
      -F "file=@test-image.jpg"
    ```
  - Test review image upload (as USER):
    ```bash
    curl -X POST http://localhost:8080/api/v1/images/reviews \
      -H "Cookie: JSESSIONID=<user-session>" \
      -F "file=@test-image.jpg"
    ```
  - Verify invalid file type rejection (PDF, etc.)
  - Verify file size limit (>5MB rejected)
  - Verify security (USER cannot upload book images, anonymous rejected)

  **Must NOT do**:
  - Do NOT skip security verification tests
  - Do NOT leave test images uploaded to Cloudinary

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Manual verification and testing
  - **Skills**: [`playwright`] for browser-based Swagger testing if needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 4 (final)
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 7, 8, 9

  **References**:
  - `src/main/resources/application.properties:22-28` - Swagger UI configuration
  - `src/main/java/trantantai/trantantai/controllers/ImageApiController.java` - Endpoints to test

  **Acceptance Criteria**:

  **Automated Verification via Bash:**
  ```bash
  # Build and start the application
  mvn clean compile -q && echo "PASS: Build successful" || echo "FAIL: Build failed"
  
  # Check for compilation errors in new files
  mvn compile -q 2>&1 | grep -i error && echo "FAIL: Compilation errors" || echo "PASS: No compilation errors"
  ```

  **Manual Verification via Swagger UI (using playwright skill):**
  ```
  1. Navigate to: http://localhost:8080/swagger-ui.html
  2. Login as ADMIN user
  3. Expand "Images" section
  4. Verify endpoints visible:
     - POST /api/v1/images/books
     - POST /api/v1/images/reviews  
     - DELETE /api/v1/images/{publicId}
  5. Test POST /api/v1/images/books with a test image
  6. Assert: Response contains "url" field with Cloudinary URL
  7. Screenshot: .sisyphus/evidence/task-10-swagger-images.png
  ```

  **Evidence to Capture:**
  - [ ] Screenshot of Swagger UI showing Image endpoints
  - [ ] curl response showing successful upload with Cloudinary URL
  - [ ] curl response showing validation error for invalid file type

  **Commit**: NO (verification only)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(config): add Cloudinary SDK dependency and configuration` | pom.xml, application.properties | grep commands |
| 2 | `feat(config): create CloudinaryConfig bean for image upload` | CloudinaryConfig.java | file exists |
| 3 | `feat(service): add ImageUploadService and ImageValidator` | ImageUploadService.java, ImageValidator.java | grep methods |
| 4 | `feat(entity): add imageUrls field to Book entity and ViewModels` | Book.java, BookPostVm.java, BookGetVm.java | grep imageUrls |
| 5 | `feat(entity): add imageUrls field to Review entity and ViewModels (max 3)` | Review.java, ReviewPostVm.java, ReviewGetVm.java | grep imageUrls |
| 6 | `feat(viewmodel): add ImageUploadVm and ImageDeleteVm` | ImageUploadVm.java, ImageDeleteVm.java | file exists |
| 7 | `feat(service): update BookService and ReviewService to handle imageUrls` | BookService.java, ReviewService.java | grep imageUrls |
| 8 | `feat(api): add ImageApiController with image upload endpoints` | ImageApiController.java | grep PostMapping |
| 9 | `feat(security): add security rules for image upload endpoints` | SecurityConfig.java | grep images |
| 10 | (no commit - verification only) | - | Manual testing |

---

## Success Criteria

### Verification Commands
```bash
# Build project successfully
mvn clean compile

# Run existing tests (should still pass)
mvn test

# Start application
mvn spring-boot:run

# Test endpoints via curl (after login)
curl -X POST http://localhost:8080/api/v1/images/books -F "file=@image.jpg" -H "Cookie: JSESSIONID=xxx"
```

### Final Checklist
- [ ] All "Must Have" present:
  - [ ] Cloudinary SDK integrated
  - [ ] Image upload endpoints working
  - [ ] File validation enforced
  - [ ] Image URLs stored in entities
  - [ ] Proper error handling
- [ ] All "Must NOT Have" absent:
  - [ ] No image bytes in MongoDB
  - [ ] No review image editing
  - [ ] No server-side thumbnails
  - [ ] No separate Image entity
- [ ] All existing tests still pass
- [ ] Swagger documentation shows new endpoints
