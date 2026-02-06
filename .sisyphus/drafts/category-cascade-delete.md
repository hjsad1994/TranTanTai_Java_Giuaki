# Draft: Category Cascade Delete & Admin Cart Access

## Requirements (confirmed)
- When deleting a category, show a WARNING that it will affect products
- When deleting a category, DELETE ALL PRODUCTS in that category (cascade delete)
- Admin should NOT be able to access shopping cart
- Add/Edit/Delete functionality should work correctly

## Technical Decisions
- **Cascade Behavior**: Delete ALL books in category when category is deleted (user's explicit requirement)
- **Warning Method**: JavaScript confirm dialog before deletion showing how many books will be deleted
- **API Response**: REST API should return the count of deleted books in response

## Research Findings

### Current State Analysis:

**CategoryService.deleteCategoryById** (line 40-42):
- Just calls `categoryRepository.deleteById(id)` - NO cascade logic
- No book checking at all

**CategoryApiController DELETE** (line 100-115):
- Checks if books exist
- Returns 409 CONFLICT if books exist (WRONG per user requirement - should CASCADE delete)
- Does NOT delete books

**CategoryController DELETE** (line 73-78):
- No book checking
- Just deletes category directly
- Leaves orphaned books

**AdminController DELETE** (line 179-183):
- Same as CategoryController
- No book checking, potential orphan books

**IBookRepository** (line 15):
- Has `findByCategoryId(String categoryId)` method - can find books
- Does NOT have `deleteByCategoryId` - need to add or iterate

**Admin Layout** (admin/layout.html):
- NO cart links in sidebar
- Admin panel is properly isolated from cart functionality

**User Layout** (layout.html line 186):
- Cart link is properly restricted with `sec:authorize="hasRole('USER')"`
- Cart shows only for USER role, not ADMIN

### Security Configuration:
- `/cart/**` requires `hasRole("USER")` - ADMIN is blocked
- This is CORRECT - no change needed at security level

## Open Questions
- NONE - requirements are clear

## Scope Boundaries
- INCLUDE: 
  - Cascade delete implementation in CategoryService
  - Update CategoryApiController to cascade delete instead of blocking
  - Update CategoryController to show warning with book count
  - Update AdminController to show warning with book count
  - Update admin/categories/list.html with dynamic warning showing book count
  - Update category/list.html with dynamic warning showing book count
  - Add method to count books by category

- EXCLUDE:
  - Modifying security config (already correct)
  - Modifying admin layout (no cart links exist)
  - Cart functionality changes
  - Book CRUD operations (working correctly)

## Test Strategy Decision
- **Infrastructure exists**: Need to verify (likely no dedicated test infrastructure)
- **User wants tests**: Not explicitly requested
- **QA approach**: Manual verification via browser/API testing
