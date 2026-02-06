# Draft: Book Image Upload Feature

## Requirements (confirmed)
- **Max images per book**: 3 images
- **Fallback display**: Keep existing CSS placeholder when no images
- **Testing**: No automated tests - use `lsp_diagnostics` for verification
- **Image removal**: Individual removal only (X button on each image)

## Technical Decisions
- **Upload API**: Use existing `POST /api/v1/images/books` (returns `{publicId, url}`)
- **CSRF Pattern**: `const csrfToken = /*[[${_csrf.token}]]*/ '';`
- **Form Binding**: Hidden inputs `<input type="hidden" name="imageUrls[0]" th:value="..."/>`
- **Image Storage**: URLs stored in `Book.imageUrls` (List<String>)

## Research Findings
- **admin/books/add.html**: Form uses `th:object="${book}"`, no image UI exists (lines 48-137)
- **admin/books/edit.html**: Same structure, needs show existing + add/remove
- **home/index.html**: Book cards at lines 76-100, uses `.book-card-cover` CSS placeholder
- **book/list.html**: Cards at lines 765-820, uses `.book-card-visual` with SVG icon
- **book/detail.html**: Large cover at lines 30-42, uses `.book-cover-large` CSS placeholder
- **ImageApiController**: `POST /api/v1/images/books` requires ADMIN role, returns ImageUploadVm

## Scope Boundaries
- **INCLUDE**: Admin add/edit forms, homepage, book list, book detail
- **EXCLUDE**: Backend changes (API already exists), review images, user-uploaded content
- **EXCLUDE**: Cart page images (would be nice but not in scope)

## Open Questions
- None - all clarified by user

## CSS Variables to Use (admin.css)
- `--admin-accent`: #0d9488 (teal)
- `--admin-bg`: #fafafa
- `--admin-surface`: #ffffff
- `--admin-border`: #e7e5e4
- `--admin-danger`: #dc2626 (for delete buttons)
- `--admin-success`: #16a34a (for success states)
