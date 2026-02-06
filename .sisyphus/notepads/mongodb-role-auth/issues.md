# MongoDB Role-Based Authorization - Issues

This notepad tracks problems, gotchas, and their solutions.

## [2026-02-05T14:17:55.429Z] Session Started

No issues yet.


## Issue #1: User Cannot Add Books to Cart
**Date:** 2026-02-05
**Status:** ✅ FIXED

### Problem Description
User role could not add books to cart. The "Add to cart" button was visible in UI but clicking it resulted in 403 Access Denied error.

### Root Cause
**Spring Security Request Matcher Order Issue:**

In `SecurityConfig.java`, the order of request matchers was incorrect:
```java
// WRONG ORDER (lines 35-44):
.requestMatchers("/books/add", "/books/add/**").hasRole("ADMIN")  // ← Line 35
// ... other rules ...
.requestMatchers("/books/add-to-cart").hasRole("USER")            // ← Line 44
```

**Problem:** The pattern `/books/add/**` on line 35 was evaluated BEFORE the specific `/books/add-to-cart` rule on line 44. 

In Spring Security, `**` wildcard matches any number of path segments, so `/books/add/**` matches:
- `/books/add`
- `/books/add/123`
- `/books/add-to-cart` ← **THIS WAS THE ISSUE!**

Since Spring Security uses **first-match-wins** strategy, the ADMIN-only rule caught `/books/add-to-cart` before the USER rule could apply.

### Solution
**Reorder request matchers: More specific rules BEFORE broader patterns**

```java
// CORRECT ORDER (lines 35-44):
.requestMatchers("/books/add-to-cart").hasRole("USER")            // ← Specific first!
.requestMatchers("/books/add", "/books/add/**").hasRole("ADMIN")  // ← Broad after
```

### Fix Applied
Moved USER cart rules (lines 43-44) to appear BEFORE ADMIN book management rules (lines 35-37).

**File:** `trantantai/src/main/java/trantantai/trantantai/config/SecurityConfig.java`

**Changes:**
- Lines 35-37: Now USER cart rules (moved up)
- Lines 39-41: Now ADMIN book rules (moved down)

### Verification
✓ Compilation successful: `./mvnw compile -q` exits 0
✓ More specific pattern `/books/add-to-cart` now evaluated first
✓ USER role can now access add-to-cart endpoint

### Critical Learning
**Spring Security Request Matcher Ordering Rules:**
1. **Order matters!** First matching rule wins
2. **Specific before broad:** Always place specific paths before wildcard patterns
3. **Common mistake:** `/api/public/**` after `/api/**` will never match
4. **Test order:** If endpoint X should have rule A but rule B catches it, move A higher

**Pattern specificity hierarchy:**
1. Exact paths: `/books/add-to-cart` (most specific)
2. Path variables: `/books/{id}`
3. Single wildcards: `/books/*` (one segment)
4. Glob patterns: `/books/**` (any segments - least specific)

### Future Prevention
- Always define USER-specific patterns BEFORE overlapping ADMIN patterns
- Review matcher order when adding new endpoints
- Test both roles after security config changes
- Consider using path variables `/books/add/{id}` instead of `/books/add/**` when possible


## Issue #2: User Không Thấy Nút "Add to Cart"
**Date:** 2026-02-05
**Status:** 🔍 INVESTIGATING

### Triệu chứng
User không thấy nút "Add to cart" trên trang danh sách sách.

### Các nguyên nhân có thể

#### 1. User đang login với Admin account
**Vấn đề:** Admin account có role ADMIN, KHÔNG có role USER.
**Business rule:** ADMIN chỉ quản lý sách, không được dùng giỏ hàng.

**Giải pháp:**
- Logout khỏi admin account
- Đăng ký user mới HOẶC login với user account

#### 2. User account thiếu role USER
**Nguyên nhân:** User đã tồn tại TRƯỚC KHI implement role system.
**Dấu hiệu:** User đăng ký/tạo trước khi có DataInitializer và setDefaultRole.

**Giải pháp:**
- Option A: Xóa user cũ trong MongoDB, đăng ký lại
- Option B: Manually thêm role USER vào user trong database
- Option C: Tạo user mới

#### 3. Role entities chưa được seed
**Nguyên nhân:** DataInitializer chưa chạy, không có ADMIN/USER role trong database.

**Kiểm tra:**
Check MongoDB collection `role`:
```javascript
db.role.find()
// Phải có 2 documents: {name: "ADMIN"}, {name: "USER"}
```

**Giải pháp:**
- Restart application để DataInitializer chạy
- Hoặc manually insert roles vào MongoDB

#### 4. Thymeleaf Spring Security chưa hoạt động
**Nguyên nhân:** sec:authorize không được xử lý.

**Kiểm tra:**
- View page source trong browser
- Nếu thấy `sec:authorize="hasRole('USER')"` trong HTML → dependency thiếu
- Nếu không thấy form nào → sec:authorize đang hoạt động đúng

**Giải pháp:**
- Verify `thymeleaf-extras-springsecurity6` trong pom.xml
- Clean và rebuild: `./mvnw clean compile`

### Hướng dẫn Troubleshooting

**Bước 1: Xác định user đang login**
- Nếu username = "admin" → Đây là admin account, không có USER role
- Nếu username khác → Kiểm tra tiếp

**Bước 2: Test với user mới**
1. Logout
2. Đăng ký user mới với username: "testuser", password: "test123"
3. Login với testuser/test123
4. Vào /books
5. Phải thấy nút "Add to cart"

**Bước 3: Kiểm tra roles trong MongoDB**
```javascript
// Check roles collection
db.role.find()

// Check user roles
db.user.findOne({username: "testuser"})
// Phải có field: roles: [ObjectId(...)]
```

**Bước 4: Kiểm tra Console Logs**
Khi start application, phải thấy:
```
>>> Created admin user: admin/admin123
```
Nếu không thấy → DataInitializer chưa chạy

### Expected Behavior
- **Admin user:** Thấy "Edit", "Delete" buttons. KHÔNG thấy "Add to cart"
- **Regular user:** Thấy "Add to cart" button. KHÔNG thấy "Edit", "Delete"
- **Anonymous:** Redirect to login page

