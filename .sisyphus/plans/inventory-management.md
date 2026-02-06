# Book Inventory Management Feature

## TL;DR

> **Quick Summary**: Thêm hệ thống quản lý số lượng tồn kho sách, bao gồm CRUD cho admin, hiển thị trên frontend với xử lý hết hàng, và tự động giảm khi checkout.
> 
> **Deliverables**:
> - Backend: `quantity` field trong Book entity + validation
> - Admin UI: Form thêm/sửa có input quantity + list hiển thị số lượng + low stock badge
> - Public UI: Stock status trên /book/{id} + disable add-to-cart khi hết hàng
> - Checkout: Validation stock + auto decrement
> 
> **Estimated Effort**: Medium (6-8 tasks)
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 → Task 2 → Task 5 → Task 6 → Task 7

---

## Context

### Original Request
Hệ thống chưa có quản lý số lượng CRUD số lượng sách. Cần thêm backend cho việc CRUD số lượng tồn kho của sách, tích hợp vào frontend tại http://localhost:8080/book/{id}, check khi số lượng hết hoặc không còn hàng, và CRUD số lượng chỉ có admin mới có thể làm điều đấy.

### Interview Summary
**Key Discussions**:
- Out of stock behavior → Disable nút "Thêm vào giỏ" + show badge "Hết hàng"
- Auto decrement → Giảm ngay khi checkout (Invoice không có status)
- Default quantity → 0 cho sách mới, 20 cho sách hiện có
- Low stock warning → Badge khi quantity ≤ 5 trong admin list
- Insufficient stock → Từ chối toàn bộ đơn
- Test strategy → Manual QA (Playwright + curl)

**Research Findings**:
- Book entity dùng MongoDB (@Document)
- Security dùng pattern `.hasRole("ADMIN")` cho `/books/add`, `/books/edit`
- Quantity CRUD sẽ inherit security từ existing book endpoints
- Invoice không có status field - decrement ngay khi checkout
- Existing patterns: `@Positive` validation, DTO records, Thymeleaf forms

### Metis Review
**Identified Gaps** (addressed):
- Invoice.status không tồn tại → Decrement ngay khi checkout
- Race condition khi checkout → Dùng MongoDB atomic `$inc` với điều kiện
- Cart-stock mismatch → Validate stock tại checkout, reject nếu không đủ
- Existing data migration → Set quantity = 20 cho sách hiện có

---

## Work Objectives

### Core Objective
Thêm quản lý số lượng tồn kho sách với CRUD cho admin, hiển thị trạng thái trên frontend, và tự động trừ khi đặt hàng thành công.

### Concrete Deliverables
- `Book.java` với field `quantity` (Integer, min=0)
- `BookPostVm.java` và `BookGetVm.java` với `quantity`
- `BookService.java` với method `decrementStock(bookId, quantity)`
- `admin/books/add.html` và `edit.html` với input quantity
- `admin/books/list.html` với cột số lượng + low stock badge
- `book/detail.html` với stock status + conditional disable button
- `CartService.java` với stock validation + decrement logic

### Definition of Done
- [x] Admin có thể CRUD quantity qua form add/edit book
- [x] Trang /book/{id} hiển thị "Hết hàng" và disable button khi quantity = 0
- [x] Admin list hiển thị quantity + badge cảnh báo khi ≤ 5
- [x] Checkout tự động giảm quantity và reject nếu không đủ hàng
- [x] Non-admin users không thể trực tiếp edit quantity

### Must Have
- Field `quantity` với validation `@Min(0)`
- Atomic decrement để tránh race condition
- Stock validation trước khi tạo invoice
- UI feedback rõ ràng cho trạng thái hết hàng

### Must NOT Have (Guardrails)
- ❌ Invoice status field (không mở rộng scope)
- ❌ Stock reservation system (giữ hàng trong giỏ)
- ❌ Inventory history/audit log
- ❌ Batch import/export quantity
- ❌ Multi-warehouse support
- ❌ Automatic reorder/notification features
- ❌ "Notify me when in stock" feature
- ❌ Partial order (cho phép mua một phần)

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO (project không có test setup)
- **User wants tests**: NO - Manual QA
- **Framework**: None
- **QA approach**: Playwright browser automation + curl commands

### Automated Verification (ALWAYS)
Mọi acceptance criteria sẽ được verify bằng:
- **Frontend/UI**: Playwright skill (navigate, click, assert, screenshot)
- **API/Backend**: curl commands via Bash
- **Database**: MongoDB queries via mongosh hoặc application logs

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately - Backend Core):
├── Task 1: Add quantity field to Book entity + DTOs

Wave 2 (After Wave 1 - Parallel UI + Service):
├── Task 2: Update BookService with decrementStock method
├── Task 3: Update Admin Add/Edit forms với quantity input
├── Task 4: Update Admin List với quantity column + low stock badge

Wave 3 (After Wave 2 - Integration):
├── Task 5: Update book/detail.html với stock status + disable button
├── Task 6: Update book/list.html với out-of-stock handling

Wave 4 (After Wave 3 - Checkout Flow):
└── Task 7: Integrate stock validation + decrement into CartService checkout

Critical Path: Task 1 → Task 2 → Task 5 → Task 7
Parallel Speedup: ~35% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2, 3, 4, 5, 6 | None |
| 2 | 1 | 7 | 3, 4 |
| 3 | 1 | None | 2, 4 |
| 4 | 1 | None | 2, 3 |
| 5 | 1 | 7 | 6 |
| 6 | 1 | None | 5 |
| 7 | 2, 5 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Dispatch |
|------|-------|---------------------|
| 1 | 1 | delegate_task(category="quick", load_skills=[]) |
| 2 | 2, 3, 4 | 3 parallel agents with category="quick" |
| 3 | 5, 6 | 2 parallel agents, task 5 needs load_skills=["playwright"] |
| 4 | 7 | delegate_task(category="unspecified-high", load_skills=[]) |

---

## TODOs

### Task 1: Add quantity field to Book entity and DTOs

**What to do**:
- Thêm field `quantity` (Integer) vào `Book.java` với `@Min(0)` validation
- Thêm `quantity` vào `BookPostVm.java` record
- Thêm `quantity` vào `BookGetVm.java` record và update `from()` method
- Set default value = 0 cho field trong entity

**Must NOT do**:
- Thêm các field khác như `reservedQuantity`, `stockStatus`
- Tạo migration script (MongoDB schemaless)
- Thay đổi validation logic khác

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Single file changes with clear pattern to follow
- **Skills**: `[]`
  - Reason: No special skills needed, simple Java edits
- **Skills Evaluated but Omitted**:
  - `playwright`: Not needed for backend changes

**Parallelization**:
- **Can Run In Parallel**: NO (this is the foundation)
- **Parallel Group**: Wave 1 (solo)
- **Blocks**: Tasks 2, 3, 4, 5, 6, 7
- **Blocked By**: None (can start immediately)

**References**:

**Pattern References** (existing code to follow):
- `trantantai/src/main/java/trantantai/trantantai/entities/Book.java:29-30` - Validation pattern với `@Positive` cho price field
- `trantantai/src/main/java/trantantai/trantantai/viewmodels/BookPostVm.java:8-26` - Record structure cho input DTO
- `trantantai/src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java:8-33` - Record structure + factory method pattern `from()`

**API/Type References**:
- `jakarta.validation.constraints.Min` - Annotation for minimum value validation

**WHY Each Reference Matters**:
- `Book.java:29-30`: Copy the validation annotation pattern (`@Min(0) private Integer quantity`)
- `BookPostVm.java`: Add `Integer quantity` parameter to the record, follow existing parameter style
- `BookGetVm.java`: Add `Integer quantity` field + update `from()` method to map `book.getQuantity()`

**Acceptance Criteria**:

**Automated Verification**:
```bash
# 1. Verify entity compiles with new field
cd trantantai && mvn compile -q
# Assert: BUILD SUCCESS

# 2. Verify field exists in Book class
grep -n "quantity" src/main/java/trantantai/trantantai/entities/Book.java
# Assert: Shows "private Integer quantity" with @Min(0)

# 3. Verify BookPostVm has quantity
grep -n "quantity" src/main/java/trantantai/trantantai/viewmodels/BookPostVm.java
# Assert: Shows "Integer quantity" in record parameters

# 4. Verify BookGetVm has quantity and from() method updated
grep -A5 "from(" src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java
# Assert: Shows quantity being mapped from book
```

**Evidence to Capture:**
- [x] Terminal output from mvn compile
- [x] Grep output showing quantity field in all 3 files

**Commit**: YES
- Message: `feat(book): add quantity field for inventory management`
- Files: `Book.java`, `BookPostVm.java`, `BookGetVm.java`
- Pre-commit: `mvn compile -q`

---

### Task 2: Update BookService with decrementStock method and existing book migration

**What to do**:
- Thêm method `decrementStock(String bookId, int quantity)` vào `BookService.java`
- Method phải dùng MongoDB atomic `$inc` operator với điều kiện `quantity >= requested`
- Return boolean (true = success, false = insufficient stock)
- Thêm method `migrateExistingBooksQuantity()` để set quantity = 20 cho sách chưa có
- Update `updateBook()` method để handle quantity khi edit

**Must NOT do**:
- Tạo repository method mới (dùng MongoTemplate trực tiếp)
- Add stock logging/history
- Tạo separate Inventory entity

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Service layer changes with clear atomic operation pattern
- **Skills**: `[]`
  - Reason: Standard Spring/MongoDB operations
- **Skills Evaluated but Omitted**:
  - `playwright`: Not applicable to backend

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 2 (with Tasks 3, 4)
- **Blocks**: Task 7
- **Blocked By**: Task 1

**References**:

**Pattern References**:
- `trantantai/src/main/java/trantantai/trantantai/services/BookService.java:54-62` - Existing `updateBook()` method pattern
- `trantantai/src/main/java/trantantai/trantantai/services/BookService.java:17-27` - Service injection và constructor pattern

**API/Type References**:
- `org.springframework.data.mongodb.core.MongoTemplate` - For atomic operations
- `org.springframework.data.mongodb.core.query.Update` - For `$inc` operator
- `org.springframework.data.mongodb.core.query.Criteria` - For conditional update

**External References**:
- MongoDB $inc operator: https://www.mongodb.com/docs/manual/reference/operator/update/inc/

**WHY Each Reference Matters**:
- `BookService.java:60-82`: Follow existing service method patterns for consistency
- MongoTemplate: Required for atomic `findAndModify` with condition (repository doesn't support this)
- $inc operator: Ensures atomic decrement without race conditions

**Acceptance Criteria**:

**Automated Verification**:
```bash
# 1. Verify service compiles
cd trantantai && mvn compile -q
# Assert: BUILD SUCCESS

# 2. Verify decrementStock method exists
grep -n "decrementStock" src/main/java/trantantai/trantantai/services/BookService.java
# Assert: Shows method signature "decrementStock(String bookId, int quantity)"

# 3. Verify atomic $inc usage
grep -n "\$inc\|Update.update" src/main/java/trantantai/trantantai/services/BookService.java
# Assert: Shows MongoDB Update with inc operator

# 4. Test via application (sau khi chạy app)
# Start application first: mvn spring-boot:run
# Then test API:
curl -s http://localhost:8080/api/v1/books | head -c 500
# Assert: Response contains books (app running correctly)
```

**Evidence to Capture:**
- [x] Compile output
- [x] Method signature in service file

**Commit**: YES
- Message: `feat(book): add atomic stock decrement service method`
- Files: `BookService.java`
- Pre-commit: `mvn compile -q`

---

### Task 3: Update Admin Add/Edit forms with quantity input

**What to do**:
- Thêm input field cho `quantity` vào `admin/books/add.html`
- Thêm input field cho `quantity` vào `admin/books/edit.html`
- Input type = number, min = 0
- Label: "Số lượng tồn kho"
- Đặt trong form-row section theo pattern hiện có

**Must NOT do**:
- Thay đổi layout tổng thể
- Thêm JavaScript validation phức tạp
- Tạo CSS mới

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Simple HTML form field additions following existing pattern
- **Skills**: `[]`
  - Reason: Basic Thymeleaf template edits
- **Skills Evaluated but Omitted**:
  - `frontend-ui-ux`: Overkill for simple form field
  - `playwright`: Verification only, not needed during implementation

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 2 (with Tasks 2, 4)
- **Blocks**: None
- **Blocked By**: Task 1

**References**:

**Pattern References**:
- `trantantai/src/main/resources/templates/admin/books/add.html:75-106` - Existing form-row pattern với input fields
- `trantantai/src/main/resources/templates/admin/books/edit.html:75-106` - Same pattern in edit form

**WHY Each Reference Matters**:
- `add.html:75-106`: Copy the exact form-row structure (label + input + error) for quantity field
- `edit.html:75-106`: Same structure, but with `th:value` binding for existing data

**Acceptance Criteria**:

**Automated Verification (Playwright)**:
```
# Admin Add Form - quantity input exists
1. Navigate to: http://localhost:8080/admin/books/add (as admin)
2. Assert: input[name="quantity"] exists and is visible
3. Assert: input has type="number" and min="0"
4. Assert: Label "Số lượng tồn kho" or similar exists near input
5. Screenshot: .sisyphus/evidence/task-3-admin-add-form.png

# Admin Edit Form - quantity input exists with value
1. Navigate to: http://localhost:8080/admin/books/edit/{any-book-id} (as admin)
2. Assert: input[name="quantity"] exists and is visible
3. Assert: input has a numeric value (from existing book)
4. Screenshot: .sisyphus/evidence/task-3-admin-edit-form.png
```

**Evidence to Capture:**
- [x] Screenshots of add form with quantity field
- [x] Screenshots of edit form with quantity field populated

**Commit**: YES
- Message: `feat(admin): add quantity input to book add/edit forms`
- Files: `admin/books/add.html`, `admin/books/edit.html`
- Pre-commit: None (template files)

---

### Task 4: Update Admin Books List with quantity column and low stock badge

**What to do**:
- Thêm column "Số lượng" vào table header trong `admin/books/list.html`
- Thêm cell hiển thị quantity cho mỗi row
- Thêm badge "Sắp hết" màu vàng/cam khi quantity ≤ 5
- Thêm badge "Hết hàng" màu đỏ khi quantity = 0

**Must NOT do**:
- Thêm sorting/filtering by stock
- Tạo separate low stock page
- Thêm bulk update functionality

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Table column addition with conditional badge
- **Skills**: `[]`
  - Reason: Basic Thymeleaf template edits
- **Skills Evaluated but Omitted**:
  - `frontend-ui-ux`: Simple badge, not full UI design
  - `playwright`: For verification only

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 2 (with Tasks 2, 3)
- **Blocks**: None
- **Blocked By**: Task 1

**References**:

**Pattern References**:
- `trantantai/src/main/resources/templates/admin/books/list.html:84-91` - Table header structure
- `trantantai/src/main/resources/templates/admin/books/list.html:93-137` - Table body row structure
- `trantantai/src/main/resources/templates/admin/books/list.html:112` - Badge styling pattern (`.category-tag`)

**WHY Each Reference Matters**:
- `list.html:84-90`: Add `<th>Số lượng</th>` following existing column pattern
- `list.html:92-138`: Add `<td>` with quantity display and conditional badge logic
- Line 112 `.category-tag`: Reuse or create similar badge class for low stock indicator

**Acceptance Criteria**:

**Automated Verification (Playwright)**:
```
# Admin List - quantity column exists
1. Navigate to: http://localhost:8080/admin/books (as admin)
2. Assert: Table header contains "Số lượng" column
3. Assert: Each row has quantity value displayed
4. Screenshot: .sisyphus/evidence/task-4-admin-list.png

# Low stock badge displays correctly
1. Find a book row with quantity <= 5 (hoặc tạo test book với quantity = 3)
2. Assert: Row displays badge with warning style (yellow/orange)
3. Screenshot: .sisyphus/evidence/task-4-low-stock-badge.png

# Out of stock badge displays correctly  
1. Find a book row with quantity = 0 (hoặc tạo test book với quantity = 0)
2. Assert: Row displays "Hết hàng" badge with danger style (red)
3. Screenshot: .sisyphus/evidence/task-4-out-of-stock-badge.png
```

**Evidence to Capture:**
- [x] Screenshot of admin list with quantity column
- [x] Screenshot showing low stock badge
- [x] Screenshot showing out of stock badge

**Commit**: YES
- Message: `feat(admin): add quantity column with stock status badges to book list`
- Files: `admin/books/list.html`
- Pre-commit: None (template file)

---

### Task 5: Update book detail page with stock status and conditional add-to-cart button

**What to do**:
- Hiển thị stock status trong book-detail-meta section của `book/detail.html`
- Khi quantity > 0: Hiển thị "Còn hàng" (badge xanh) + số lượng
- Khi quantity = 0: Hiển thị "Hết hàng" (badge đỏ)
- Disable button "Thêm vào giỏ hàng" khi quantity = 0
- Button disabled có style mờ đi + không clickable

**Must NOT do**:
- Thêm "Thông báo khi có hàng" feature
- Ẩn hoàn toàn button (chỉ disable)
- Thêm quantity selector cho user

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Template edits with conditional rendering
- **Skills**: `["playwright"]`
  - Reason: Need to verify UI behavior, button state, and visual display
- **Skills Evaluated but Omitted**:
  - `frontend-ui-ux`: Not redesigning, just adding conditional elements

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 3 (with Task 6)
- **Blocks**: Task 7
- **Blocked By**: Task 1

**References**:

**Pattern References**:
- `trantantai/src/main/resources/templates/book/detail.html:73-93` - book-detail-meta section (price, category display)
- `trantantai/src/main/resources/templates/book/detail.html:95-121` - Add to cart form và button

**WHY Each Reference Matters**:
- `detail.html:73-93`: Add stock status display in same section as price/category metadata
- `detail.html:95-121`: Modify button to have `th:disabled="${book.quantity <= 0}"` and disabled styling

**Acceptance Criteria**:

**Automated Verification (Playwright)**:
```
# In-stock book detail page
1. Navigate to: http://localhost:8080/book/{in-stock-book-id}
2. Assert: Stock status shows "Còn hàng" or similar with green badge
3. Assert: button.add-to-cart-btn is ENABLED (not disabled)
4. Assert: Button is clickable
5. Screenshot: .sisyphus/evidence/task-5-in-stock-detail.png

# Out-of-stock book detail page
1. Navigate to: http://localhost:8080/book/{out-of-stock-book-id} (quantity = 0)
2. Assert: Stock status shows "Hết hàng" with red badge
3. Assert: button.add-to-cart-btn is DISABLED
4. Assert: Button has disabled visual style (opacity, cursor)
5. Click button → Assert: No action (form not submitted)
6. Screenshot: .sisyphus/evidence/task-5-out-of-stock-detail.png
```

**Evidence to Capture:**
- [x] Screenshot of in-stock book detail with enabled button
- [x] Screenshot of out-of-stock book detail with disabled button

**Commit**: YES
- Message: `feat(book): show stock status and disable add-to-cart when out of stock`
- Files: `book/detail.html`
- Pre-commit: None (template file)

---

### Task 6: Update book list page with out-of-stock handling

**What to do**:
- Trong `book/list.html`, thêm stock status indicator cho mỗi book card/row
- Hiển thị badge "Hết hàng" overlay trên sách hết hàng
- Disable hoặc hide "Add to cart" button cho sách hết hàng trong list view
- Giữ nguyên việc hiển thị sách (không ẩn sách hết hàng)

**Must NOT do**:
- Ẩn hoàn toàn sách hết hàng khỏi list
- Thêm filter "Chỉ hiện sách còn hàng"
- Thay đổi pagination logic

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: Template modifications with conditional rendering
- **Skills**: `["playwright"]`
  - Reason: Need to verify visual display of badges and button states
- **Skills Evaluated but Omitted**:
  - `frontend-ui-ux`: Simple badge addition, not redesign

**Parallelization**:
- **Can Run In Parallel**: YES
- **Parallel Group**: Wave 3 (with Task 5)
- **Blocks**: None
- **Blocked By**: Task 1

**References**:

**Pattern References**:
- `trantantai/src/main/resources/templates/book/list.html` - Book list table/cards structure
- `trantantai/src/main/resources/templates/admin/books/list.html:112` - Badge styling reference

**WHY Each Reference Matters**:
- `book/list.html`: Add conditional badge and button state based on `book.quantity`
- Badge from admin list: Reuse similar styling for consistency

**Acceptance Criteria**:

**Automated Verification (Playwright)**:
```
# Book list page shows stock indicators
1. Navigate to: http://localhost:8080/books
2. Assert: Page loads with book list
3. Find a book with quantity = 0
4. Assert: Out-of-stock book shows "Hết hàng" badge or indicator
5. Assert: Add-to-cart button for that book is disabled or hidden
6. Screenshot: .sisyphus/evidence/task-6-book-list-oos.png

# In-stock books remain normal
1. Find a book with quantity > 0
2. Assert: No "Hết hàng" badge
3. Assert: Add-to-cart button is enabled
4. Screenshot: .sisyphus/evidence/task-6-book-list-in-stock.png
```

**Evidence to Capture:**
- [x] Screenshot of book list with out-of-stock badge visible
- [x] Screenshot showing in-stock book with enabled button

**Commit**: YES
- Message: `feat(book): add out-of-stock indicators to book list page`
- Files: `book/list.html`
- Pre-commit: None (template file)

---

### Task 7: Integrate stock validation and decrement into CartService checkout

**What to do**:
- Trong `CartService.saveCart()` method, thêm stock validation TRƯỚC khi tạo invoice
- Check: với mỗi item trong cart, verify `book.quantity >= item.quantity`
- Nếu bất kỳ item nào không đủ stock → throw exception với message rõ ràng
- Nếu đủ stock → gọi `bookService.decrementStock()` cho mỗi item
- Decrement phải atomic và rollback nếu có lỗi

**Must NOT do**:
- Implement partial order (mua được bao nhiêu mua bấy nhiêu)
- Thêm stock reservation (giữ hàng khi add to cart)
- Modify Invoice entity

**Recommended Agent Profile**:
- **Category**: `unspecified-high`
  - Reason: Critical business logic với transaction và error handling
- **Skills**: `[]`
  - Reason: Backend service integration, no special skills needed
- **Skills Evaluated but Omitted**:
  - `playwright`: For verification later, not implementation

**Parallelization**:
- **Can Run In Parallel**: NO
- **Parallel Group**: Wave 4 (final, solo)
- **Blocks**: None (this is the final task)
- **Blocked By**: Tasks 2, 5

**References**:

**Pattern References**:
- `trantantai/src/main/java/trantantai/trantantai/services/CartService.java:65-99` - Existing `saveCart()` method (checkout flow)
- `trantantai/src/main/java/trantantai/trantantai/services/BookService.java` - `decrementStock()` method from Task 2

**API/Type References**:
- `trantantai/src/main/java/trantantai/trantantai/entities/Item.java` - Cart item với `bookId` và `quantity`
- `trantantai/src/main/java/trantantai/trantantai/entities/Cart.java` - Cart structure với items list

**WHY Each Reference Matters**:
- `CartService.saveCart()`: This is THE checkout method - must add validation here
- `decrementStock()`: Call this for each cart item after validation passes
- `Item.java`: Understand structure to get bookId and requested quantity

**Acceptance Criteria**:

**Automated Verification (Playwright + curl)**:

```
# Scenario 1: Successful checkout with stock decrement
1. Setup: Ensure book X has quantity = 10
2. Login as user
3. Navigate to book X detail page
4. Add to cart (quantity = 2)
5. Go to cart, checkout
6. Assert: Checkout succeeds (invoice created)
7. Verify: Book X quantity is now 8 (decreased by 2)

# Terminal verification:
curl -s http://localhost:8080/api/v1/books/{bookId} | jq '.quantity'
# Assert: Returns 8 (was 10, bought 2)

# Scenario 2: Checkout fails when insufficient stock
1. Setup: Ensure book Y has quantity = 1
2. Add book Y to cart (quantity = 3)
3. Attempt checkout
4. Assert: Error message displayed "Không đủ hàng trong kho" or similar
5. Assert: No invoice created
6. Verify: Book Y quantity still = 1 (unchanged)
7. Screenshot: .sisyphus/evidence/task-7-insufficient-stock-error.png
```

**Evidence to Capture:**
- [x] Screenshot of successful checkout
- [x] API response showing decremented quantity
- [x] Screenshot of insufficient stock error message

**Commit**: YES
- Message: `feat(cart): add stock validation and auto-decrement on checkout`
- Files: `CartService.java`
- Pre-commit: `mvn compile -q`

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(book): add quantity field for inventory management` | Book.java, BookPostVm.java, BookGetVm.java | mvn compile |
| 2 | `feat(book): add atomic stock decrement service method` | BookService.java | mvn compile |
| 3 | `feat(admin): add quantity input to book add/edit forms` | admin/books/add.html, edit.html | Visual check |
| 4 | `feat(admin): add quantity column with stock status badges` | admin/books/list.html | Visual check |
| 5 | `feat(book): show stock status and disable add-to-cart when out of stock` | book/detail.html | Playwright |
| 6 | `feat(book): add out-of-stock indicators to book list page` | book/list.html | Playwright |
| 7 | `feat(cart): add stock validation and auto-decrement on checkout` | CartService.java | Full flow test |

---

## Success Criteria

### Verification Commands
```bash
# Build project
cd trantantai && mvn clean compile -q
# Expected: BUILD SUCCESS

# Start application
cd trantantai && mvn spring-boot:run
# Expected: Application starts on port 8080

# Test API - get book with quantity
curl -s http://localhost:8080/api/v1/books | jq '.[0].quantity'
# Expected: Returns integer (quantity value)
```

### Final Checklist
- [x] All "Must Have" present:
  - [x] `quantity` field in Book entity
  - [x] Admin can CRUD quantity via forms
  - [x] `/book/{id}` shows stock status
  - [x] Add-to-cart disabled when quantity = 0
  - [x] Checkout validates and decrements stock
- [x] All "Must NOT Have" absent:
  - [x] No Invoice.status field added
  - [x] No stock reservation system
  - [x] No inventory history logging
  - [x] No partial order support
- [x] Security verified: Non-admin cannot edit quantity directly
