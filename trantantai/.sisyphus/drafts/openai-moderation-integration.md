# Draft: OpenAI Moderation API Integration

## Requirements (confirmed from user request)
- Integrate OpenAI Moderation API (`omni-moderation-latest` model)
- Moderate product review comments (text) in `ReviewService.addReview()` BEFORE saving
- Moderate uploaded review images in `ImageApiController.uploadReviewImage()` BEFORE returning URL
- Reject content if `flagged=true` in API response
- Return user-friendly error messages when content is rejected
- Use Java HttpClient (no additional dependencies)
- Store API key securely in `application.properties` as `openai.api-key`

## Technical Decisions (based on codebase patterns)
- **HTTP Client**: Use Java 11+ HttpClient (per requirement, consistent with avoiding new deps)
- **Config Pattern**: Follow Cloudinary pattern - `@Value` annotation + `@Bean` configuration
- **Service Pattern**: Create `OpenAIModerationService` following `ImageUploadService` pattern (constructor injection)
- **Error Handling**: Follow existing pattern - `Map.of("error", message)` as JSON body
- **Validation Integration**: Call moderation service BEFORE existing validations complete

## Research Findings

### Codebase Patterns Found:
- External API integration: `MoMoService` uses `RestTemplate` - but we'll use HttpClient per requirement
- Config: `application.properties` stores API keys with prefix pattern (`cloudinary.*`, `momo.*`)
- Controllers return `Map.of("error", message)` for error responses
- Services throw `IllegalArgumentException` / `IllegalStateException` for business errors
- Existing Vietnamese error messages: `"Vui long dang nhap de danh gia"`, `"Ban can mua san pham nay de danh gia"`

### OpenAI Moderation API (from librarian research):
- Endpoint: `POST https://api.openai.com/v1/moderations`
- Model: `omni-moderation-latest` (supports text + images) - FREE to use
- Headers: `Authorization: Bearer {API_KEY}`, `Content-Type: application/json`
- Text input: `{"model": "omni-moderation-latest", "input": "text"}`
- Image + text input: `{"model": "omni-moderation-latest", "input": [{"type":"text","text":"..."}, {"type":"image_url","image_url":{"url":"..."}}]}`
- Response: `{"results": [{"flagged": true/false, "categories": {...}, "category_scores": {...}}]}`

### API Error Handling Best Practices:
- HTTP 429: Rate limit - implement exponential backoff with jitter
- HTTP 500/503: Server errors - retry with backoff
- HTTP 401/403: Auth errors - don't retry, fail fast
- Reuse HttpClient instance (thread-safe, efficient)
- Set connect timeout (30s) and request timeout (60s)

### Image Moderation Decision:
- Use Cloudinary URL directly (public CDN, already available after upload)
- No need for base64 encoding since Cloudinary URLs are publicly accessible

## Integration Points Identified
1. **Text Moderation**: `ReviewService.addReview()` at line 103 (BEFORE `sanitizeHtml`)
2. **Image Moderation**: `ImageApiController.uploadReviewImage()` at line 131 (AFTER upload, BEFORE response)
   - Note: Need to moderate the Cloudinary URL returned from upload

## Files to Create
- `src/main/java/trantantai/trantantai/services/OpenAIModerationService.java` - API client
- `src/main/java/trantantai/trantantai/config/OpenAIConfig.java` - Configuration properties
- `src/main/java/trantantai/trantantai/exceptions/ContentModerationException.java` - Custom exception (optional)

## Files to Modify
- `src/main/resources/application.properties` - Add `openai.api-key`
- `src/main/java/trantantai/trantantai/services/ReviewService.java` - Add moderation call
- `src/main/java/trantantai/trantantai/controllers/ImageApiController.java` - Add moderation call

## User Decisions (Confirmed)
- [x] Error message language: **Vietnamese** - `"Noi dung cua ban vi pham chinh sach cong dong"`
- [x] Violation logging: **Yes** - Log flagged categories to server logs (e.g., `"Content flagged: harassment, violence"`)
- [x] API failure handling: **Fail-open with retry** - Retry 3x with exponential backoff, then ALLOW if API unavailable
- [x] Test strategy: **Manual verification** - Use Swagger/curl to test endpoints

## Scope Boundaries
- INCLUDE: Text moderation, image moderation, API key config, error handling
- EXCLUDE: Admin dashboard for reviewing flagged content, moderation override mechanism
