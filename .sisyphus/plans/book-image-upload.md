# Book Image Upload Feature

## TL;DR

> **Quick Summary**: Add image upload functionality to admin book forms and display book images across the storefront, using the existing Cloudinary API infrastructure.
> 
> **Deliverables**:
> - Admin book add form with drag-drop image upload (up to 3 images)
> - Admin book edit form with existing image display + add/remove capability
> - Homepage, book list, and book detail pages showing actual book images
> - Graceful fallback to CSS placeholder when no images exist
> 
> **Estimated Effort**: Medium (5-7 tasks, ~4-6 hours implementation)
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 → Task 3/4 → Task 7

---

## Context

### Original Request
Add image upload functionality for books in a Spring Boot + Thymeleaf bookstore application. The backend infrastructure (Cloudinary, ImageUploadService, API endpoints) already exists - only frontend template changes are needed.

### Interview Summary
**Key Discussions**:
- **Image limit**: Up to 3 images per book
- **Fallback**: Keep existing CSS placeholder when no images (no static assets needed)
- **Testing**: No automated tests - use `lsp_diagnostics` verification only
- **Removal UX**: Individual image removal only (X button per image, no bulk delete)

**Research Findings**:
- `POST /api/v1/images/books` API exists, ADMIN-only, returns `{publicId, url, originalFilename, size}`
- `ImageValidator` accepts JPEG/PNG/GIF/WebP, max 5MB
- Forms use `th:object="${book}"` with `th:field="*{property}"` binding
- CSRF token pattern: `const csrfToken = /*[[${_csrf.token}]]*/ '';`
- Book entity has `imageUrls: List<String>` field already implemented

### Self-Review (Gap Analysis)
**Identified Gaps** (addressed):
- Hidden input naming for List binding → Use indexed pattern: `imageUrls[0]`, `imageUrls[1]`, `imageUrls[2]`
- Upload state management → Use JavaScript array to track uploaded URLs before form submit
- Image ordering → First uploaded image is primary (index 0), displayed on cards
- Delete from Cloudinary on remove → Not implemented (out of scope - images persist in Cloudinary)

---

## Work Objectives

### Core Objective
Enable admins to upload book cover images during book creation/editing, and display these images to customers on the storefront.

### Concrete Deliverables
- `admin/books/add.html` - Image upload component with preview grid
- `admin/books/edit.html` - Show existing images + add/remove functionality
- `home/index.html` - Display `book.imageUrls[0]` in featured book cards
- `book/list.html` - Display book images in catalog grid
- `book/detail.html` - Image gallery with primary image + thumbnails

### Definition of Done
- [ ] Admin can upload up to 3 images when adding a new book
- [ ] Admin can view/add/remove images when editing existing books
- [ ] Homepage shows book cover images (first image from imageUrls)
- [ ] Book list page shows book cover images
- [ ] Book detail page shows all images in a gallery format
- [ ] Pages gracefully fallback to CSS placeholder when no images exist
- [ ] `lsp_diagnostics` reports no errors on modified files

### Must Have
- Drag-and-drop upload zone with click fallback
- Image preview before form submission
- Individual image removal with X button
- Loading state during upload
- Error handling for upload failures
- Responsive image display

### Must NOT Have (Guardrails)
- ❌ DO NOT modify any Java backend code (API exists)
- ❌ DO NOT add "delete all images" button
- ❌ DO NOT implement image cropping/editing
- ❌ DO NOT add images to cart page (out of scope)
- ❌ DO NOT delete images from Cloudinary on removal (just remove from form)
- ❌ DO NOT add image upload to category management
- ❌ DO NOT over-engineer with complex gallery libraries (keep it simple)

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO (no test framework in templates)
- **User wants tests**: NO - Manual verification only
- **Framework**: N/A
- **QA approach**: `lsp_diagnostics` for syntax errors + visual verification

### Verification Approach

Each task will be verified using:
1. **`lsp_diagnostics`** - Ensure no Thymeleaf/HTML syntax errors
2. **Visual inspection** - Describe expected UI appearance

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: Create reusable image upload JavaScript module
└── Task 2: Add image upload/display CSS styles

Wave 2 (After Wave 1):
├── Task 3: Implement admin/books/add.html image upload
├── Task 4: Implement admin/books/edit.html image management
├── Task 5: Update home/index.html with image display
├── Task 6: Update book/list.html with image display
└── Task 7: Update book/detail.html with image gallery

Wave 3 (After Wave 2):
└── Task 8: Final verification across all pages
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 3, 4 | 2 |
| 2 | None | 3, 4, 5, 6, 7 | 1 |
| 3 | 1, 2 | 8 | 4, 5, 6, 7 |
| 4 | 1, 2 | 8 | 3, 5, 6, 7 |
| 5 | 2 | 8 | 3, 4, 6, 7 |
| 6 | 2 | 8 | 3, 4, 5, 7 |
| 7 | 2 | 8 | 3, 4, 5, 6 |
| 8 | 3, 4, 5, 6, 7 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Dispatch |
|------|-------|---------------------|
| 1 | 1, 2 | Parallel - both can start immediately |
| 2 | 3, 4, 5, 6, 7 | Parallel - all have Wave 1 dependencies met |
| 3 | 8 | Sequential - final integration task |

---

## TODOs

### Wave 1: Foundation (No Dependencies)

- [ ] 1. Create Reusable Image Upload JavaScript Module

  **What to do**:
  - Create inline `<script>` module in admin templates for image upload functionality
  - Implement functions: `initImageUpload(config)`, `uploadImage(file)`, `removeImage(index)`, `getImageUrls()`
  - Handle drag-drop events, file validation, upload progress, error states
  - Store uploaded image URLs in JavaScript array for form submission
  - Generate hidden inputs dynamically: `imageUrls[0]`, `imageUrls[1]`, `imageUrls[2]`

  **Must NOT do**:
  - Do NOT create separate .js file (keep inline in template for simplicity)
  - Do NOT implement image cropping or editing
  - Do NOT call DELETE API when removing images (just remove from form state)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Frontend JavaScript with DOM manipulation and async fetch calls
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Drag-drop interactions, loading states, error handling UX

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Tasks 3, 4
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References** (existing code to follow):
  - `admin/inventory/list.html:520-650` - CSRF token extraction pattern and fetch() usage
  - `book/detail.html:895-1000` - Image upload pattern from review system (similar structure)

  **API References** (endpoint to call):
  - `ImageApiController.java:52-105` - POST /api/v1/images/books endpoint specification
  - `ImageUploadVm.java:1-23` - Response shape: `{publicId, url, originalFilename, size}`

  **Technical References**:
  - `ImageValidator.java:25-45` - Validation rules: JPEG/PNG/GIF/WebP, max 5MB

  **Acceptance Criteria**:

  ```
  lsp_diagnostics verification:
  - Run lsp_diagnostics on the file where this script will be added
  - Assert: No HTML/Thymeleaf syntax errors reported
  
  Code structure verification:
  - Module exposes: initImageUpload(), uploadImage(), removeImage(), getImageUrls()
  - CSRF token extracted using Thymeleaf inline: /*[[${_csrf.token}]]*/
  - Fetch calls POST /api/v1/images/books with multipart/form-data
  - Response parsed to extract `url` field
  - Hidden inputs generated with name pattern: imageUrls[0], imageUrls[1], imageUrls[2]
  ```

  **Commit**: NO (groups with Task 3)

---

- [ ] 2. Add Image Upload and Display CSS Styles

  **What to do**:
  - Add image upload zone styles to `admin.css` (drag-drop area, hover states, loading)
  - Add image preview grid styles (thumbnail container, remove button)
  - Add book image display styles to main stylesheet or inline in templates
  - Ensure responsive behavior for all image components
  - Use existing CSS variables: `--admin-accent`, `--admin-border`, `--admin-danger`

  **Must NOT do**:
  - Do NOT modify existing book card styles that work correctly
  - Do NOT add complex animations (keep simple transitions)
  - Do NOT use external CSS libraries

  **Recommended Agent Profile**:
  - **Category**: `artistry`
    - Reason: CSS styling work requiring visual design sensibility
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Responsive design, visual hierarchy, consistent styling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Tasks 3, 4, 5, 6, 7
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References** (existing styles to match):
  - `admin.css:14-99` - CSS variables to use (--admin-accent, --admin-bg, etc.)
  - `admin.css:700-900` - Form card and input styling patterns
  - `book/detail.html:398-419` - Review image upload dropzone pattern (similar structure)

  **Style Guide**:
  - Primary accent: `--admin-accent` (#0d9488 teal)
  - Danger/delete: `--admin-danger` (#dc2626 red)
  - Borders: `--admin-border` (#e7e5e4)
  - Border radius: `--radius-md` (12px) for cards, `--radius-sm` (8px) for buttons

  **Acceptance Criteria**:

  ```
  lsp_diagnostics verification:
  - Run lsp_diagnostics on admin.css
  - Assert: No CSS syntax errors
  
  Style classes created:
  - .image-upload-zone (drag-drop area)
  - .image-upload-zone.dragover (hover state)
  - .image-upload-zone.uploading (loading state)
  - .image-preview-grid (thumbnail container)
  - .image-preview-item (individual thumbnail wrapper)
  - .image-preview-remove (X button overlay)
  - .book-image (for public pages - responsive image display)
  ```

  **Commit**: NO (groups with Task 3)

---

### Wave 2: Implementation (Depends on Wave 1)

- [ ] 3. Implement Admin Book Add Form Image Upload

  **What to do**:
  - Add image upload section to `admin/books/add.html` after quantity field
  - Include drag-drop zone with click-to-upload fallback
  - Show preview grid for uploaded images (up to 3)
  - Add hidden inputs for `imageUrls` form binding
  - Include the JavaScript module from Task 1
  - Add inline CSS or reference styles from Task 2

  **Must NOT do**:
  - Do NOT change existing form fields (title, author, price, category, quantity)
  - Do NOT modify form action or method
  - Do NOT add more than 3 image slots

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Thymeleaf template + JavaScript integration
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Form UX, upload interactions, preview display

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5, 6, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Tasks 1, 2

  **References**:

  **Pattern References** (existing code to follow):
  - `admin/books/add.html:48-137` - Current form structure (add after line 121)
  - `admin/books/add.html:149-206` - Inline style block pattern
  - `admin/inventory/list.html:526-527` - CSRF token extraction pattern

  **Form Binding Reference**:
  - Hidden inputs must use: `name="imageUrls[0]"`, `name="imageUrls[1]"`, `name="imageUrls[2]"`
  - Spring will bind these to `List<String> imageUrls` in Book object

  **Acceptance Criteria**:

  ```
  lsp_diagnostics verification:
  - Run lsp_diagnostics on admin/books/add.html
  - Assert: No Thymeleaf/HTML errors
  
  Structure verification:
  - Image upload section exists after quantity field (around line 122)
  - Drag-drop zone with class .image-upload-zone
  - File input with accept="image/jpeg,image/png,image/gif,image/webp"
  - Preview grid container with class .image-preview-grid
  - Hidden inputs container for imageUrls binding
  - Maximum 3 images enforced in JavaScript
  ```

  **Commit**: YES
  - Message: `feat(admin): add image upload to book add form`
  - Files: `admin/books/add.html`, `admin.css` (if styles added there)
  - Pre-commit: `lsp_diagnostics` check

---

- [ ] 4. Implement Admin Book Edit Form Image Management

  **What to do**:
  - Add image management section to `admin/books/edit.html`
  - Display existing images from `book.imageUrls` using Thymeleaf iteration
  - Allow removing existing images (X button)
  - Allow adding new images (up to 3 total)
  - Preserve image order (first = primary)
  - Include same JavaScript module and styles as add form

  **Must NOT do**:
  - Do NOT change existing form fields
  - Do NOT allow more than 3 total images
  - Do NOT call Cloudinary delete API when removing

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Thymeleaf template with dynamic JavaScript state management
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Edit form UX, state synchronization

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 5, 6, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Tasks 1, 2

  **References**:

  **Pattern References** (existing code to follow):
  - `admin/books/edit.html:48-138` - Current form structure
  - `admin/categories/edit.html:96-103` - Pattern for pre-populating form values

  **Thymeleaf Iteration Pattern**:
  ```html
  <div th:each="imageUrl, iter : ${book.imageUrls}">
      <img th:src="${imageUrl}" />
      <input type="hidden" th:name="'imageUrls[' + ${iter.index} + ']'" th:value="${imageUrl}"/>
  </div>
  ```

  **Acceptance Criteria**:

  ```
  lsp_diagnostics verification:
  - Run lsp_diagnostics on admin/books/edit.html
  - Assert: No Thymeleaf/HTML errors
  
  Structure verification:
  - Existing images displayed using th:each="imageUrl : ${book.imageUrls}"
  - Each existing image has remove button
  - Hidden inputs pre-populated with existing URLs
  - Upload zone for adding new images (if < 3 total)
  - Upload zone hidden when 3 images already exist
  ```

  **Commit**: YES
  - Message: `feat(admin): add image management to book edit form`
  - Files: `admin/books/edit.html`
  - Pre-commit: `lsp_diagnostics` check

---

- [ ] 5. Update Homepage with Book Image Display

  **What to do**:
  - Modify `home/index.html` featured books section (lines 76-100)
  - Add conditional: if `book.imageUrls` not empty, show `<img>` with first URL
  - Keep existing CSS placeholder as fallback when no images
  - Ensure image fits within existing card dimensions
  - Add appropriate `alt` text using book title

  **Must NOT do**:
  - Do NOT remove the CSS placeholder structure (needed for fallback)
  - Do NOT change card layout or dimensions
  - Do NOT add complex image galleries (just show first image)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Thymeleaf conditional rendering with responsive images
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Image display, fallback handling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 4, 6, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Task 2

  **References**:

  **Pattern References** (existing code to follow):
  - `home/index.html:76-100` - Current book card structure
  - `home/index.html:82` - Category null check pattern: `${book.category != null ? ... : 'Sách'}`

  **Conditional Display Pattern**:
  ```html
  <!-- Show image if exists -->
  <img th:if="${book.imageUrls != null && !book.imageUrls.isEmpty()}"
       th:src="${book.imageUrls[0]}"
       th:alt="${book.title}"
       class="book-card-image"/>
  <!-- Show CSS placeholder if no image -->
  <div th:unless="${book.imageUrls != null && !book.imageUrls.isEmpty()}"
       class="book-card-front">
      <!-- existing placeholder content -->
  </div>
  ```

  **Acceptance Criteria**:

  ```
  lsp_diagnostics verification:
  - Run lsp_diagnostics on home/index.html
  - Assert: No Thymeleaf/HTML errors
  
  Structure verification:
  - Conditional th:if checks book.imageUrls not null and not empty
  - Image uses th:src="${book.imageUrls[0]}"
  - Fallback uses th:unless with same condition
  - CSS placeholder preserved inside th:unless block
  - Image has alt text: th:alt="${book.title}"
  ```

  **Commit**: YES
  - Message: `feat(home): display book cover images on homepage`
  - Files: `home/index.html`
  - Pre-commit: `lsp_diagnostics` check

---

- [ ] 6. Update Book List Page with Image Display

  **What to do**:
  - Modify `book/list.html` book cards (lines 765-820)
  - Add conditional: if `book.imageUrls` exists, show image instead of SVG icon
  - Keep existing SVG book icon as fallback
  - Style image to fit within `.book-card-cover` dimensions
  - Maintain existing hover effects and card interactions

  **Must NOT do**:
  - Do NOT remove the SVG icon (needed for fallback)
  - Do NOT change card dimensions or layout
  - Do NOT modify wishlist button or other card features

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Template modification with style integration
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Card design, image fitting

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 4, 5, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Task 2

  **References**:

  **Pattern References** (existing code to follow):
  - `book/list.html:774-780` - Current SVG icon structure
  - `book/list.html:785-786` - Category conditional pattern

  **Image Styling**:
  ```css
  .book-card-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      position: absolute;
      top: 0;
      left: 0;
  }
  ```

  **Acceptance Criteria**:

  ```
  lsp_diagnostics verification:
  - Run lsp_diagnostics on book/list.html
  - Assert: No Thymeleaf/HTML errors
  
  Structure verification:
  - Conditional check: th:if="${book.imageUrls != null && !book.imageUrls.isEmpty()}"
  - Image inside .book-card-cover div
  - SVG icon wrapped in th:unless for fallback
  - Image styled to cover the card area (object-fit: cover)
  ```

  **Commit**: YES
  - Message: `feat(catalog): display book images in book list`
  - Files: `book/list.html`
  - Pre-commit: `lsp_diagnostics` check

---

- [ ] 7. Update Book Detail Page with Image Gallery

  **What to do**:
  - Modify `book/detail.html` book cover section (lines 30-42)
  - If multiple images: show primary image large + thumbnail row below
  - If single image: show just the image (no thumbnails)
  - If no images: keep CSS placeholder
  - Add JavaScript for thumbnail click → update main image
  - Ensure responsive layout for gallery

  **Must NOT do**:
  - Do NOT add lightbox/modal (keep it simple)
  - Do NOT remove CSS placeholder structure
  - Do NOT change book info section layout

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Gallery UI with JavaScript interactions
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Gallery UX, image switching interactions

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 4, 5, 6)
  - **Blocks**: Task 8
  - **Blocked By**: Task 2

  **References**:

  **Pattern References** (existing code to follow):
  - `book/detail.html:30-42` - Current cover structure
  - `book/detail.html:199-220` - Related books card pattern (for thumbnail styling)

  **Gallery Structure**:
  ```html
  <div class="book-gallery" th:if="${book.imageUrls != null && !book.imageUrls.isEmpty()}">
      <img id="mainBookImage" th:src="${book.imageUrls[0]}" class="book-main-image"/>
      <div class="book-thumbnails" th:if="${#lists.size(book.imageUrls) > 1}">
          <img th:each="url, iter : ${book.imageUrls}" 
               th:src="${url}" 
               th:classappend="${iter.index == 0} ? 'active'"
               onclick="setMainImage(this.src)"/>
      </div>
  </div>
  ```

  **Acceptance Criteria**:

  ```
  lsp_diagnostics verification:
  - Run lsp_diagnostics on book/detail.html
  - Assert: No Thymeleaf/HTML errors
  
  Structure verification:
  - Gallery container with conditional display
  - Main image element with id="mainBookImage"
  - Thumbnail row only shown when multiple images (th:if="${#lists.size(book.imageUrls) > 1}")
  - Each thumbnail has click handler to update main image
  - CSS placeholder preserved in th:unless block
  - setMainImage() JavaScript function defined
  ```

  **Commit**: YES
  - Message: `feat(detail): add image gallery to book detail page`
  - Files: `book/detail.html`
  - Pre-commit: `lsp_diagnostics` check

---

### Wave 3: Final Verification

- [ ] 8. Final Verification Across All Pages

  **What to do**:
  - Run `lsp_diagnostics` on all modified files
  - Verify no syntax errors in any template
  - Document verification results

  **Must NOT do**:
  - Do NOT make code changes in this task (verification only)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple verification task, no complex work
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (sequential)
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 3, 4, 5, 6, 7

  **References**:
  - All files modified in previous tasks

  **Acceptance Criteria**:

  ```
  lsp_diagnostics verification:
  Run on each file:
  - admin/books/add.html → Assert: No errors
  - admin/books/edit.html → Assert: No errors  
  - home/index.html → Assert: No errors
  - book/list.html → Assert: No errors
  - book/detail.html → Assert: No errors
  - admin.css → Assert: No errors
  ```

  **Commit**: NO (verification only)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 3 | `feat(admin): add image upload to book add form` | add.html, admin.css | lsp_diagnostics |
| 4 | `feat(admin): add image management to book edit form` | edit.html | lsp_diagnostics |
| 5 | `feat(home): display book cover images on homepage` | index.html | lsp_diagnostics |
| 6 | `feat(catalog): display book images in book list` | list.html | lsp_diagnostics |
| 7 | `feat(detail): add image gallery to book detail page` | detail.html | lsp_diagnostics |

---

## Success Criteria

### Verification Commands
```bash
# Run LSP diagnostics on each modified file
lsp_diagnostics admin/books/add.html
lsp_diagnostics admin/books/edit.html
lsp_diagnostics home/index.html
lsp_diagnostics book/list.html
lsp_diagnostics book/detail.html
lsp_diagnostics admin.css
```

### Final Checklist
- [ ] All "Must Have" features present
- [ ] All "Must NOT Have" exclusions respected
- [ ] No LSP errors on any modified file
- [ ] All 5 commits created with proper messages
