# OpenAI Content Moderation Integration

## TL;DR

> **Quick Summary**: Integrate OpenAI's Moderation API to automatically check review text and images for harmful content before allowing submission. Text is checked during review creation, images are checked before Cloudinary upload.
> 
> **Deliverables**:
> - `OpenAIConfig.java` - Configuration class for API credentials
> - `OpenAIModerationService.java` - Service for text and image moderation
> - `ModerationResultVm.java` - DTO for moderation response
> - Modified `ReviewService.addReview()` - Text moderation integration
> - Modified `ImageApiController.uploadReviewImage()` - Image moderation integration
> - Updated `application.properties` - OpenAI configuration properties
> 
> **Estimated Effort**: Medium (4-6 hours)
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: Task 1 → Task 2 → Task 3 → (Task 4 & Task 5 parallel) → Task 6

---

## Context

### Original Request
Implement OpenAI Moderation API to:
1. Moderate text comments - Check review comments for harmful content before saving
2. Moderate images - Check uploaded review images for violations before Cloudinary upload

### Interview Summary
**Key Discussions**:
- HTTP status code: **400 Bad Request** for moderation failures (consistent with existing patterns)
- Image moderation timing: **Before Cloudinary upload** (no harmful content stored)
- Fallback behavior: **Fail closed** - reject content if OpenAI API unavailable
- Logging: **Log all moderation calls** (flagged status + categories, NOT content)
- Test strategy: **Manual verification only** (no test infrastructure setup)

**Research Findings**:
- Config pattern: `@ConfigurationProperties(prefix = "openai")` like `MoMoConfig.java`
- HTTP client: `RestTemplate` with inline `new RestTemplate()` like `MoMoService.java`
- DTO pattern: Java records with `*Vm` suffix in `viewmodels` package
- Logging: `java.util.logging.Logger` pattern
- Error response: `Map.of("error", "message")` format
- Exceptions: Use `IllegalArgumentException` for validation failures
- OpenAI API: `POST /v1/moderations`, model `omni-moderation-latest`, returns `{ results: [{ flagged, categories }] }`

### Metis Review
**Identified Gaps** (addressed):
- Image upload and review creation are SEPARATE operations (images already Cloudinary URLs when review created)
  → **Solution**: Moderate images at upload time in `ImageApiController`, moderate text at review creation in `ReviewService`
- No timeout on RestTemplate 
  → **Solution**: Configure 10-second timeout for OpenAI calls
- Base64 image format requires MIME type prefix 
  → **Solution**: Build `data:{mimeType};base64,{base64data}` format from MultipartFile
- API key storage security
  → **Solution**: Use environment variable `OPENAI_API_KEY`, reference via `${OPENAI_API_KEY}` in properties

---

## Work Objectives

### Core Objective
Prevent harmful content (violence, hate speech, explicit material) from being submitted in product reviews by integrating OpenAI's content moderation API at two integration points: text during review creation, images during upload.

### Concrete Deliverables
1. `src/main/java/trantantai/trantantai/config/OpenAIConfig.java`
2. `src/main/java/trantantai/trantantai/services/OpenAIModerationService.java`
3. `src/main/java/trantantai/trantantai/viewmodels/ModerationResultVm.java`
4. Modified `ReviewService.addReview()` method
5. Modified `ImageApiController.uploadReviewImage()` method
6. Updated `application.properties` with OpenAI settings

### Definition of Done
- [ ] Review with clean text → saves successfully
- [ ] Review with harmful text → rejected with 400 and error message
- [ ] Image upload with clean image → uploads to Cloudinary successfully
- [ ] Image upload with harmful image → rejected with 400 before reaching Cloudinary
- [ ] OpenAI API timeout → rejected with 503 and "Service temporarily unavailable"
- [ ] All moderation calls logged with flagged status and categories

### Must Have
- Environment variable `OPENAI_API_KEY` for API authentication (NOT hardcoded)
- 10-second timeout on OpenAI API calls
- Fail-closed behavior (reject on API error)
- Logging of all moderation decisions (flagged/not flagged, categories)
- Clear error messages for rejected content

### Must NOT Have (Guardrails)
- ❌ NO hardcoded API keys in source code or properties
- ❌ NO moderation of admin book images (out of scope)
- ❌ NO moderation of existing reviews (only new reviews)
- ❌ NO custom category score thresholds (use OpenAI's default `flagged` boolean)
- ❌ NO moderation result persistence/storage (log only)
- ❌ NO retry logic on API failure (fail closed immediately)
- ❌ NO human review queue or appeal workflow
- ❌ NO logging of actual review content (privacy)

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO
- **User wants tests**: Manual verification only
- **Framework**: N/A

### Manual Verification Procedures

Each TODO includes automated verification commands that agents can execute directly using Bash (curl) and application logs.

**Evidence Requirements:**
- curl command outputs with HTTP status codes
- Application log snippets showing moderation results
- Cloudinary dashboard verification for image tests

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: OpenAIConfig.java (no dependencies)
└── Task 2: ModerationResultVm.java (no dependencies)

Wave 2 (After Wave 1):
└── Task 3: OpenAIModerationService.java (depends: 1, 2)

Wave 3 (After Wave 2):
├── Task 4: Integrate text moderation in ReviewService (depends: 3)
└── Task 5: Integrate image moderation in ImageApiController (depends: 3)

Wave 4 (After Wave 3):
└── Task 6: End-to-end verification (depends: 4, 5)

Critical Path: Task 1 → Task 3 → Task 4 → Task 6
Parallel Speedup: ~30% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 3 | 2 |
| 2 | None | 3 | 1 |
| 3 | 1, 2 | 4, 5 | None |
| 4 | 3 | 6 | 5 |
| 5 | 3 | 6 | 4 |
| 6 | 4, 5 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Approach |
|------|-------|---------------------|
| 1 | 1, 2 | Parallel - independent config and DTO creation |
| 2 | 3 | Sequential - core service depends on Wave 1 |
| 3 | 4, 5 | Parallel - independent integrations |
| 4 | 6 | Sequential - verification after all implementation |

---

## TODOs

- [ ] 1. Create OpenAI Configuration Class

  **What to do**:
  - Create `OpenAIConfig.java` in `config/` package
  - Use `@ConfigurationProperties(prefix = "openai")` annotation (like `MoMoConfig.java`)
  - Add fields: `apiKey`, `moderationEndpoint`, `moderationModel`
  - Add getters and setters for all fields
  - Add `application.properties` entries with environment variable reference

  **Must NOT do**:
  - ❌ Do NOT hardcode API key - use `${OPENAI_API_KEY}` placeholder
  - ❌ Do NOT create a RestTemplate bean (service will create inline like MoMoService)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple configuration class creation, straightforward pattern copy
  - **Skills**: None needed
    - This is basic Spring configuration, no specialized skills required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Task 3
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References** (existing code to follow):
  - `src/main/java/trantantai/trantantai/config/MoMoConfig.java:1-40` - Exact pattern to follow: @Configuration + @ConfigurationProperties, private fields with getters/setters

  **API/Type References**:
  - `src/main/resources/application.properties` - Where to add new properties (add after existing momo.* section)

  **Documentation References**:
  - OpenAI API base URL: `https://api.openai.com/v1/moderations`
  - Model name: `omni-moderation-latest`

  **WHY Each Reference Matters**:
  - `MoMoConfig.java` shows the exact Spring Boot configuration pattern used in this codebase - follow it exactly
  - `application.properties` shows the naming convention (`prefix.property-name` with kebab-case)

  **Acceptance Criteria**:

  **Automated Verification:**
  ```bash
  # Agent runs to verify file exists and has correct structure:
  cat src/main/java/trantantai/trantantai/config/OpenAIConfig.java | grep -E "@Configuration|@ConfigurationProperties|apiKey|moderationEndpoint|moderationModel"
  # Assert: Shows all 5 patterns found
  
  # Verify application.properties has entries:
  cat src/main/resources/application.properties | grep "openai."
  # Assert: Shows openai.api-key, openai.moderation-endpoint, openai.moderation-model
  
  # Verify environment variable reference:
  cat src/main/resources/application.properties | grep "OPENAI_API_KEY"
  # Assert: Contains ${OPENAI_API_KEY} placeholder
  ```

  **Commit**: YES
  - Message: `feat(config): add OpenAI configuration for content moderation`
  - Files: `src/main/java/trantantai/trantantai/config/OpenAIConfig.java`, `src/main/resources/application.properties`
  - Pre-commit: N/A (config only)

---

- [ ] 2. Create Moderation Result DTO

  **What to do**:
  - Create `ModerationResultVm.java` as a Java record in `viewmodels/` package
  - Include fields: `flagged` (boolean), `categories` (Map<String, Boolean>), `categoryScores` (Map<String, Double>)
  - Add `@Schema` annotations for OpenAPI documentation
  - Create a static factory method `from(Map<String, Object> apiResponse)` to parse OpenAI response

  **Must NOT do**:
  - ❌ Do NOT use traditional POJO class - use Java record (project standard)
  - ❌ Do NOT include raw API response fields not needed by callers

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple DTO creation with established pattern
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Task 3
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References** (existing code to follow):
  - `src/main/java/trantantai/trantantai/viewmodels/ImageUploadVm.java` - Java record pattern with @Schema annotations
  - `src/main/java/trantantai/trantantai/viewmodels/ReviewGetVm.java` - Example of static `from()` factory method pattern

  **API/Type References**:
  - OpenAI Moderation API response structure:
    ```json
    {
      "results": [{
        "flagged": true,
        "categories": { "violence": true, "hate": false, ... },
        "category_scores": { "violence": 0.95, "hate": 0.01, ... }
      }]
    }
    ```

  **WHY Each Reference Matters**:
  - `ImageUploadVm.java` shows the exact record syntax with @Schema used in this project
  - OpenAI response structure determines what fields we need to map

  **Acceptance Criteria**:

  **Automated Verification:**
  ```bash
  # Verify file exists and is a record:
  cat src/main/java/trantantai/trantantai/viewmodels/ModerationResultVm.java | grep -E "public record|flagged|categories|categoryScores|@Schema"
  # Assert: Shows record declaration and all fields
  
  # Verify has factory method:
  cat src/main/java/trantantai/trantantai/viewmodels/ModerationResultVm.java | grep "static.*from"
  # Assert: Shows static from method
  ```

  **Commit**: YES
  - Message: `feat(dto): add ModerationResultVm for OpenAI moderation responses`
  - Files: `src/main/java/trantantai/trantantai/viewmodels/ModerationResultVm.java`
  - Pre-commit: N/A

---

- [ ] 3. Create OpenAI Moderation Service

  **What to do**:
  - Create `OpenAIModerationService.java` in `services/` package
  - Inject `OpenAIConfig` via constructor
  - Create `RestTemplate` inline in constructor (like `MoMoService.java`)
  - **Configure 10-second timeout** on RestTemplate using `SimpleClientHttpRequestFactory`
  - Implement `moderateText(String text)` method:
    - Build JSON request with `model` and `input` (string)
    - Set `Authorization: Bearer {apiKey}` header
    - POST to moderation endpoint
    - Parse response into `ModerationResultVm`
    - Log: `"Text moderation: flagged={}, categories={}"`
    - Return `ModerationResultVm`
  - Implement `moderateImage(byte[] imageBytes, String mimeType)` method:
    - Build base64 string: `data:{mimeType};base64,{base64encoded}`
    - Build JSON request with multimodal input array
    - POST to moderation endpoint
    - Parse and return `ModerationResultVm`
    - Log result
  - Handle exceptions:
    - `RestClientException` → throw `IllegalStateException("Content moderation service unavailable")`
    - Parse errors → throw `IllegalStateException("Content moderation failed")`

  **Must NOT do**:
  - ❌ Do NOT inject RestTemplate as a bean (create inline)
  - ❌ Do NOT implement retry logic
  - ❌ Do NOT log the actual content being moderated
  - ❌ Do NOT catch and swallow exceptions silently

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Service implementation following established MoMoService pattern
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 2 (sequential)
  - **Blocks**: Tasks 4, 5
  - **Blocked By**: Tasks 1, 2

  **References**:

  **Pattern References** (existing code to follow):
  - `src/main/java/trantantai/trantantai/services/MoMoService.java:16-96` - RestTemplate pattern with HttpHeaders, HttpEntity, restTemplate.exchange()
  - `src/main/java/trantantai/trantantai/services/MoMoService.java:21-25` - Constructor with config injection and inline RestTemplate creation

  **API/Type References**:
  - `src/main/java/trantantai/trantantai/config/OpenAIConfig.java` - Config to inject (from Task 1)
  - `src/main/java/trantantai/trantantai/viewmodels/ModerationResultVm.java` - Return type (from Task 2)

  **Documentation References**:
  - OpenAI Moderation API:
    - Endpoint: `POST https://api.openai.com/v1/moderations`
    - Text request: `{"model": "omni-moderation-latest", "input": "text here"}`
    - Image request: `{"model": "omni-moderation-latest", "input": [{"type": "image_url", "image_url": {"url": "data:image/jpeg;base64,..."}}]}`
    - Auth header: `Authorization: Bearer sk-xxx`

  **WHY Each Reference Matters**:
  - `MoMoService.java` is the template for ALL external API calls in this codebase - follow its exact pattern for headers, entity, exchange
  - OpenAI API docs specify exact request format needed

  **Acceptance Criteria**:

  **Automated Verification:**
  ```bash
  # Verify service file exists with required methods:
  cat src/main/java/trantantai/trantantai/services/OpenAIModerationService.java | grep -E "public class|moderateText|moderateImage|RestTemplate|OpenAIConfig"
  # Assert: Shows class, both methods, RestTemplate and config
  
  # Verify timeout configuration:
  cat src/main/java/trantantai/trantantai/services/OpenAIModerationService.java | grep -E "setTimeout|SimpleClientHttpRequestFactory|10000"
  # Assert: Shows timeout configuration (10000ms = 10s)
  
  # Verify logging:
  cat src/main/java/trantantai/trantantai/services/OpenAIModerationService.java | grep -E "Logger|logger.info"
  # Assert: Shows logger usage
  
  # Verify Authorization header:
  cat src/main/java/trantantai/trantantai/services/OpenAIModerationService.java | grep "Bearer"
  # Assert: Shows Bearer token auth pattern
  ```

  **Commit**: YES
  - Message: `feat(service): add OpenAIModerationService for text and image moderation`
  - Files: `src/main/java/trantantai/trantantai/services/OpenAIModerationService.java`
  - Pre-commit: N/A

---

- [ ] 4. Integrate Text Moderation in ReviewService

  **What to do**:
  - Inject `OpenAIModerationService` into `ReviewService` constructor
  - In `addReview()` method (the overload with imageUrls at line 84):
    - After `canUserReview()` check and before `sanitizeHtml()`
    - Call `moderationService.moderateText(comment)`
    - If `result.flagged()` is true, throw `IllegalArgumentException("Review contains inappropriate content")`
  - Wrap moderation call in try-catch:
    - Catch `IllegalStateException` (service unavailable)
    - Re-throw as `IllegalStateException("Unable to verify content. Please try again later.")`

  **Must NOT do**:
  - ❌ Do NOT modify the method signature
  - ❌ Do NOT add @Transactional (not used in this service currently)
  - ❌ Do NOT moderate if comment is null or empty (skip moderation)
  - ❌ Do NOT change existing validation order

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Small, focused integration - adding a few lines to existing method
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 5)
  - **Blocks**: Task 6
  - **Blocked By**: Task 3

  **References**:

  **Pattern References** (existing code to follow):
  - `src/main/java/trantantai/trantantai/services/ReviewService.java:84-113` - The `addReview()` method to modify
  - `src/main/java/trantantai/trantantai/services/ReviewService.java:86-88` - Example of throwing `IllegalStateException` for business rule
  - `src/main/java/trantantai/trantantai/services/ReviewService.java:93-95` - Example of throwing `IllegalArgumentException` for validation

  **API/Type References**:
  - `src/main/java/trantantai/trantantai/services/OpenAIModerationService.java` - Service to inject (from Task 3)
  - `src/main/java/trantantai/trantantai/viewmodels/ModerationResultVm.java` - Return type to check

  **WHY Each Reference Matters**:
  - `ReviewService.java:84-113` is the exact method being modified - understand current flow
  - Exception patterns at lines 86-88 and 93-95 show how to throw the right exception types

  **Acceptance Criteria**:

  **Automated Verification:**
  ```bash
  # Verify moderation service is injected:
  cat src/main/java/trantantai/trantantai/services/ReviewService.java | grep "OpenAIModerationService"
  # Assert: Shows import and field/constructor parameter
  
  # Verify moderation call exists:
  cat src/main/java/trantantai/trantantai/services/ReviewService.java | grep "moderateText"
  # Assert: Shows moderateText call
  
  # Verify inappropriate content message:
  cat src/main/java/trantantai/trantantai/services/ReviewService.java | grep "inappropriate content"
  # Assert: Shows error message
  ```

  **Manual E2E Test (requires running app + valid API key):**
  ```bash
  # Test 1: Clean review should succeed
  curl -X POST http://localhost:8080/api/v1/reviews \
    -H "Content-Type: application/json" \
    -H "Cookie: JSESSIONID=<session>" \
    -d '{"bookId":"<id>","rating":5,"comment":"Great book, highly recommend!"}' \
    -w "\nHTTP Status: %{http_code}\n"
  # Assert: HTTP 201 Created
  
  # Test 2: Harmful review should be rejected
  curl -X POST http://localhost:8080/api/v1/reviews \
    -H "Content-Type: application/json" \
    -H "Cookie: JSESSIONID=<session>" \
    -d '{"bookId":"<id>","rating":1,"comment":"I will kill everyone who reads this"}' \
    -w "\nHTTP Status: %{http_code}\n"
  # Assert: HTTP 400 Bad Request with error "inappropriate content"
  ```

  **Commit**: YES
  - Message: `feat(review): integrate text moderation before saving reviews`
  - Files: `src/main/java/trantantai/trantantai/services/ReviewService.java`
  - Pre-commit: N/A

---

- [ ] 5. Integrate Image Moderation in ImageApiController

  **What to do**:
  - Inject `OpenAIModerationService` into `ImageApiController` constructor
  - In `uploadReviewImage()` method (line 116):
    - After `imageValidator.validateImage(file)` (line 128)
    - Before `imageUploadService.uploadImage()` (line 131)
    - Get file bytes: `byte[] imageBytes = file.getBytes()`
    - Get MIME type: `String mimeType = file.getContentType()`
    - Call `moderationService.moderateImage(imageBytes, mimeType)`
    - If `result.flagged()` is true, return 400: `ResponseEntity.badRequest().body(Map.of("error", "Image contains inappropriate content"))`
  - Handle `IllegalStateException`:
    - Return 503: `ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Unable to verify image. Please try again later."))`

  **Must NOT do**:
  - ❌ Do NOT moderate admin book images (`uploadBookImage()` method)
  - ❌ Do NOT upload to Cloudinary before moderation passes
  - ❌ Do NOT change the existing try-catch structure - add new catches

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Small, focused integration - adding moderation before upload
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 4)
  - **Blocks**: Task 6
  - **Blocked By**: Task 3

  **References**:

  **Pattern References** (existing code to follow):
  - `src/main/java/trantantai/trantantai/controllers/ImageApiController.java:116-154` - The `uploadReviewImage()` method to modify
  - `src/main/java/trantantai/trantantai/controllers/ImageApiController.java:146-148` - Example of returning 400 with error map
  - `src/main/java/trantantai/trantantai/controllers/ImageApiController.java:149-152` - Example of returning 500 with error map

  **API/Type References**:
  - `src/main/java/trantantai/trantantai/services/OpenAIModerationService.java` - Service to inject (from Task 3)
  - `org.springframework.web.multipart.MultipartFile` - Has `getBytes()` and `getContentType()` methods

  **WHY Each Reference Matters**:
  - `ImageApiController.java:116-154` is the exact method being modified - understand flow
  - Error response patterns at lines 146-152 show exactly how to format error responses

  **Acceptance Criteria**:

  **Automated Verification:**
  ```bash
  # Verify moderation service is injected:
  cat src/main/java/trantantai/trantantai/controllers/ImageApiController.java | grep "OpenAIModerationService"
  # Assert: Shows import and field/constructor parameter
  
  # Verify moderation call in uploadReviewImage only:
  grep -A 50 "uploadReviewImage" src/main/java/trantantai/trantantai/controllers/ImageApiController.java | grep "moderateImage"
  # Assert: Shows moderateImage call
  
  # Verify admin upload NOT modified:
  grep -A 30 "uploadBookImage" src/main/java/trantantai/trantantai/controllers/ImageApiController.java | grep "moderateImage"
  # Assert: NO match (admin images not moderated)
  
  # Verify 503 status handling:
  cat src/main/java/trantantai/trantantai/controllers/ImageApiController.java | grep "SERVICE_UNAVAILABLE"
  # Assert: Shows 503 handling
  ```

  **Manual E2E Test (requires running app + valid API key):**
  ```bash
  # Test 1: Clean image should upload
  curl -X POST http://localhost:8080/api/v1/images/reviews \
    -H "Cookie: JSESSIONID=<session>" \
    -F "file=@/path/to/clean-image.jpg" \
    -w "\nHTTP Status: %{http_code}\n"
  # Assert: HTTP 201 Created with Cloudinary URL
  
  # Test 2: Explicit image should be rejected (need test image)
  # Assert: HTTP 400 with error "inappropriate content"
  # Assert: NO Cloudinary URL returned (not uploaded)
  ```

  **Commit**: YES
  - Message: `feat(image): integrate image moderation before Cloudinary upload`
  - Files: `src/main/java/trantantai/trantantai/controllers/ImageApiController.java`
  - Pre-commit: N/A

---

- [ ] 6. End-to-End Verification and Documentation

  **What to do**:
  - Start the application with `OPENAI_API_KEY` environment variable set
  - Verify application starts without errors
  - Test text moderation:
    - Submit clean review → succeeds
    - Submit review with violent content → rejected with 400
  - Test image moderation:
    - Upload clean image → succeeds, returns Cloudinary URL
    - Upload explicit image → rejected with 400, no Cloudinary upload
  - Test fail-closed behavior:
    - Set invalid API key → all moderation requests return 503/400
  - Verify logging:
    - Check application logs for moderation results (flagged status, categories)
    - Verify NO content is logged (only metadata)
  - Add inline documentation comments to new files

  **Must NOT do**:
  - ❌ Do NOT commit test data or test images
  - ❌ Do NOT expose API key in logs or error messages

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Requires running application and verifying behavior
  - **Skills**: [`playwright`]
    - `playwright`: For browser-based testing of image upload UI if needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 4 (final)
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 4, 5

  **References**:

  **Pattern References**:
  - All files created in Tasks 1-5

  **Documentation References**:
  - OpenAI Moderation categories: `violence`, `hate`, `harassment`, `self-harm`, `sexual`, `sexual/minors`

  **Acceptance Criteria**:

  **Automated Verification (with running app):**
  ```bash
  # Start app with API key
  export OPENAI_API_KEY="sk-your-key"
  mvn spring-boot:run &
  sleep 30  # Wait for startup
  
  # Verify app started
  curl http://localhost:8080/actuator/health
  # Assert: {"status":"UP"}
  
  # Test clean review (need valid session and book ID)
  curl -X POST http://localhost:8080/api/v1/reviews \
    -H "Content-Type: application/json" \
    -H "Cookie: JSESSIONID=<session>" \
    -d '{"bookId":"<id>","rating":5,"comment":"Excellent book!"}' \
    -w "\nHTTP Status: %{http_code}\n"
  # Assert: HTTP 201
  
  # Check logs for moderation entry
  grep "moderation" logs/app.log | tail -5
  # Assert: Shows "Text moderation: flagged=false"
  ```

  **Evidence to Capture:**
  - [ ] Screenshot or log of successful clean review creation
  - [ ] Screenshot or log of rejected harmful review (HTTP 400)
  - [ ] Screenshot or log of successful clean image upload
  - [ ] Screenshot or log of rejected harmful image (HTTP 400)
  - [ ] Log showing moderation call with flagged=false for clean content
  - [ ] Log showing moderation call with flagged=true for harmful content

  **Commit**: NO (verification only, no code changes)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(config): add OpenAI configuration for content moderation` | OpenAIConfig.java, application.properties | grep for properties |
| 2 | `feat(dto): add ModerationResultVm for OpenAI moderation responses` | ModerationResultVm.java | grep for record |
| 3 | `feat(service): add OpenAIModerationService for text and image moderation` | OpenAIModerationService.java | grep for methods |
| 4 | `feat(review): integrate text moderation before saving reviews` | ReviewService.java | grep for moderateText |
| 5 | `feat(image): integrate image moderation before Cloudinary upload` | ImageApiController.java | grep for moderateImage |
| 6 | (no commit) | - | E2E tests |

---

## Success Criteria

### Verification Commands
```bash
# All files exist
ls -la src/main/java/trantantai/trantantai/config/OpenAIConfig.java
ls -la src/main/java/trantantai/trantantai/services/OpenAIModerationService.java
ls -la src/main/java/trantantai/trantantai/viewmodels/ModerationResultVm.java

# Properties configured
grep "openai\." src/main/resources/application.properties

# Integrations in place
grep "moderateText" src/main/java/trantantai/trantantai/services/ReviewService.java
grep "moderateImage" src/main/java/trantantai/trantantai/controllers/ImageApiController.java

# App compiles
mvn compile -q && echo "BUILD SUCCESS"
```

### Final Checklist
- [ ] All "Must Have" present (env var, timeout, fail-closed, logging, error messages)
- [ ] All "Must NOT Have" absent (no hardcoded keys, no admin image moderation, no retries)
- [ ] Application compiles without errors
- [ ] Clean content passes moderation
- [ ] Harmful content is rejected
- [ ] API unavailable triggers fail-closed behavior
- [ ] Logs show moderation decisions without content
