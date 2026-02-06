# Draft: OpenAI Moderation Feature

## Requirements (confirmed)
- Text moderation for review comments before saving
- Image moderation for uploaded review images
- API key stored securely via environment variable `OPENAI_API_KEY`
- Use `omni-moderation-latest` model (supports text + images)
- Reject content with clear error messages

## Research Findings

### Existing Codebase Patterns
- **Config**: `@ConfigurationProperties(prefix = "openai")` pattern (like MoMoConfig)
- **HTTP Client**: RestTemplate (used in MoMoService)
- **DTOs**: Java records with `*GetVm`/`*PostVm` naming + `@Schema` annotations
- **Package**: `trantantai.trantantai.viewmodels`
- **Error responses**: `Map.of("error", "message")` format
- **Exceptions**: Standard Java exceptions (IllegalArgumentException, IllegalStateException)
- **Logging**: `java.util.logging.Logger` (NOT SLF4J)
- **Test infrastructure**: Does NOT exist (no tests)

### OpenAI Moderation API
- Endpoint: `POST https://api.openai.com/v1/moderations`
- Model: `omni-moderation-latest`
- Text input: `{"model": "omni-moderation-latest", "input": "text here"}`
- Image input: `{"model": "...", "input": [{"type": "text", "text": "..."}, {"type": "image_url", "image_url": {"url": "..."}}]}`
- Response: `{ "results": [{ "flagged": boolean, "categories": {...}, "category_scores": {...} }] }`
- Categories: sexual, hate, harassment, violence, self-harm (with sub-categories)

### Integration Points (confirmed)
1. **Text moderation**: In `ReviewService.addReview()` - before saving
2. **Image moderation**: In `ImageApiController.uploadReviewImage()` - timing TBD

## Technical Decisions (CONFIRMED)
- HTTP status code: **400 Bad Request** (consistent with existing patterns)
- Image moderation timing: **Before Cloudinary upload** (no harmful content stored)
- Fallback behavior: **Fail closed - reject content** (safer approach)
- Logging: **Log all moderation calls** (flagged status + categories, NOT content)
- Test strategy: **Manual verification only** (no test infrastructure setup)

## Open Questions
(All resolved - see Technical Decisions above)

## Scope Boundaries
- INCLUDE: Text moderation, image moderation, config, service, DTOs, error handling
- EXCLUDE: Admin dashboard for reviewing flagged content, moderation appeals
