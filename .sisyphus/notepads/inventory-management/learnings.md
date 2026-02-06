# Inventory Management - Learnings

## Patterns & Conventions

## 2026-02-05: Session 2 - Initial Analysis

### Codebase Patterns Discovered
- Book entity uses `@Document(collection = "book")` for MongoDB
- Validation uses Jakarta annotations: `@Min`, `@Positive`, `@NotBlank`
- ViewModels are Java records with `from()` static factory methods
- Thymeleaf templates use Bootstrap 5 + custom CSS variables
- Admin templates have consistent structure: breadcrumb, form-card, form-body
- Forms use `th:field`, `th:object`, `th:classappend` for validation

### Completed Work (Previous Session)
- Task 1: COMPLETED - Book.java has `@Min(0) private Integer quantity = 0`
- Task 2: COMPLETED - BookService has `decrementStock()` method using MongoDB atomic $inc
- Both BookPostVm and BookGetVm already include `quantity` field

### UI Patterns
- Admin forms: `.admin-form-card`, `.form-header`, `.form-body`
- Form groups: `.form-group` with `.form-label` and `.form-control`
- Form rows use CSS grid: `.form-row { grid-template-columns: repeat(2, 1fr) }`
- Badges: `.category-tag` for styled pill badges
- Tables: `.admin-table` with specific row patterns

## 2026-02-05: Session 2 - Completion

### All Tasks Completed Successfully
- Task 1: Book entity + DTOs with quantity field
- Task 2: BookService.decrementStock() with atomic MongoDB $inc
- Task 3: Admin add/edit forms with quantity input
- Task 4: Admin book list with quantity column + stock badges
- Task 5: Book detail page with stock status + disabled button
- Task 6: Book list page with out-of-stock indicators
- Task 7: CartService checkout with stock validation + auto-decrement

### Key Implementation Patterns Used
- Stock badges: `.stock-in` (green #d1fae5), `.stock-out` (red #fee2e2), `.stock-low` (orange #fef3c7)
- Button disable: `th:disabled="${book.quantity == 0}"` + CSS `opacity: 0.5; cursor: not-allowed`
- Stock validation: Check all items BEFORE decrement, then atomic decrement with boolean return
- Constructor injection for service dependencies in Spring
