# BookHaven: Order Management & Cart Persistence

## TL;DR

> **Quick Summary**: Implement 3 features - User Order History, Admin Order Management with status lifecycle (PROCESSING→SHIPPED→DELIVERED→CANCELLED), and Persistent Cart that survives logout/login with merge capability.
> 
> **Deliverables**:
> - User can view their order history at `/orders`
> - Admin can manage all orders at `/admin/orders` with status updates
> - Cart persists to MongoDB on logout, restores on login with smart merge
> 
> **Estimated Effort**: Medium-Large (~16-20 tasks)
> **Parallel Execution**: YES - 5 waves
> **Critical Path**: Task 1 → Task 4 → Task 7 → Task 10 → Task 14

---

## Context

### Original Request
Implement User Order History, Admin Order Management, and Persistent Cart for BookHaven e-commerce Spring Boot + MongoDB application.

### Interview Summary
**Key Decisions**:
- OrderStatus: Separate enum (PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- Cart Merge: Combine both carts, same items add quantities
- Cancel + Stock: Yes - restore inventory when order cancelled
- User Order History: Simple list, sorted by date
- Test Strategy: Manual browser verification

### Metis Review
**Identified Gaps** (resolved with defaults):

| Gap | Resolution |
|-----|------------|
| Status transitions rules | Any-to-any allowed, but DELIVERED/CANCELLED are terminal |
| User self-cancel | Admin-only (out of scope for user) |
| Cart item unavailable on restore | Skip item with flash message |
| Price changed on restore | Use current price (standard behavior) |
| Quantity exceeds stock on merge | Cap at available stock, notify user |
| Order history pagination | Yes, 10 per page |
| Cart save failure on logout | Log error, continue logout |
| Concurrent admin updates | No optimistic locking (out of scope) |

---

## Work Objectives

### Core Objective
Enable order lifecycle management for admins, order history viewing for users, and cart persistence across login sessions.

### Concrete Deliverables
1. `OrderStatus.java` enum with PROCESSING, SHIPPED, DELIVERED, CANCELLED
2. Extended `Invoice.java` with orderStatus field
3. `UserCart.java` entity for MongoDB cart persistence
4. `IUserCartRepository.java` for cart CRUD
5. Extended `IInvoiceRepository.java` with findByUserId, pagination
6. `OrderService.java` for order business logic
7. Custom `LogoutHandler` to save cart
8. Custom `AuthenticationSuccessHandler` to restore/merge cart
9. `OrderController.java` for user order history
10. Extended `AdminController.java` with order management
11. `templates/user/orders.html` - user order list
12. `templates/admin/orders/list.html` - admin order list
13. `templates/admin/orders/detail.html` - admin order detail with status update
14. Updated `templates/admin/layout.html` - orders menu link
15. Updated `templates/admin/dashboard.html` - order statistics
16. Updated `templates/layout.html` - user orders link in dropdown

### Definition of Done
- [ ] User can view their orders at `/orders` sorted by date
- [ ] Admin can view all orders at `/admin/orders` with pagination
- [ ] Admin can update order status via dropdown
- [ ] Admin can cancel order and stock is restored
- [ ] Cart persists on logout for logged-in users
- [ ] Cart restores on login with merge if session has items
- [ ] All navigation links work correctly

### Must Have
- OrderStatus enum separate from PaymentStatus
- Stock restoration on order cancel
- Cart merge strategy (sum quantities)
- Pagination for admin order list

### Must NOT Have (Guardrails)
- Email notifications (out of scope)
- User self-cancel orders (admin-only)
- Reorder from history feature
- Bulk status updates
- Order export (CSV/PDF)
- Refund processing
- Shipping tracking integration
- Order editing after placement
- Optimistic locking for concurrent updates

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO (no test folder found)
- **User wants tests**: Manual-only
- **Framework**: N/A
- **QA approach**: Manual browser verification

### Manual Verification Approach
Each task includes specific browser/curl verification steps that agents can execute.

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately) - Foundation:
├── Task 1: Create OrderStatus enum
├── Task 2: Create UserCart entity + repository
└── Task 3: Create IUserCartRepository

Wave 2 (After Wave 1) - Data Layer:
├── Task 4: Extend Invoice entity with orderStatus
├── Task 5: Extend IInvoiceRepository with queries
└── Task 6: Modify CartService for DB operations

Wave 3 (After Wave 2) - Service Layer:
├── Task 7: Create OrderService
├── Task 8: Create LogoutHandler for cart save
└── Task 9: Create AuthenticationSuccessHandler for cart restore

Wave 4 (After Wave 3) - Controller Layer:
├── Task 10: Create OrderController (user)
├── Task 11: Extend AdminController (admin orders)
└── Task 12: Update SecurityConfig with handlers

Wave 5 (After Wave 4) - View Layer:
├── Task 13: Create user orders view
├── Task 14: Create admin orders list view
├── Task 15: Create admin order detail view
├── Task 16: Update admin layout (orders link)
├── Task 17: Update admin dashboard (stats)
└── Task 18: Update user layout (orders link)

Critical Path: Task 1 → Task 4 → Task 7 → Task 11 → Task 15
Parallel Speedup: ~50% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 4, 7 | 2, 3 |
| 2 | None | 6, 8 | 1, 3 |
| 3 | None | 6, 8 | 1, 2 |
| 4 | 1 | 5, 7 | 5 |
| 5 | 4 | 7, 10, 11 | 6 |
| 6 | 2, 3 | 8, 9 | 4, 5 |
| 7 | 4, 5 | 10, 11 | 8, 9 |
| 8 | 6 | 12 | 7, 9 |
| 9 | 6 | 12 | 7, 8 |
| 10 | 5, 7 | 13 | 11, 12 |
| 11 | 5, 7 | 14, 15, 17 | 10, 12 |
| 12 | 8, 9 | None | 10, 11 |
| 13 | 10 | 18 | 14, 15, 16, 17 |
| 14 | 11 | 16 | 13, 15, 17 |
| 15 | 11 | None | 13, 14, 16, 17 |
| 16 | 14 | None | 13, 15, 17, 18 |
| 17 | 11 | None | 13, 14, 15, 16, 18 |
| 18 | 13 | None | 14, 15, 16, 17 |

---

## TODOs

---

### Wave 1: Foundation Layer (Start Immediately)

---

- [ ] 1. Create OrderStatus Enum

  **What to do**:
  - Create new file `constants/OrderStatus.java`
  - Add enum values: PROCESSING, SHIPPED, DELIVERED, CANCELLED
  - Add display name method for Vietnamese labels

  **Must NOT do**:
  - Do NOT modify PaymentStatus enum
  - Do NOT add complex state machine logic

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple enum creation, single file, no complex logic
  - **Skills**: None required
    - Simple Java enum, no special skills needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Tasks 4, 7
  - **Blocked By**: None (can start immediately)

  **References**:
  - `src/main/java/trantantai/trantantai/constants/PaymentStatus.java` - Enum pattern to follow
  - `src/main/java/trantantai/trantantai/constants/PaymentMethod.java` - Another enum example

  **Files to Create**:
  - `src/main/java/trantantai/trantantai/constants/OrderStatus.java`

  **Acceptance Criteria**:
  ```java
  // File exists and compiles
  // Contains: PROCESSING, SHIPPED, DELIVERED, CANCELLED
  // Has getDisplayName() returning Vietnamese: "Đang xử lý", "Đang giao", "Đã giao", "Đã hủy"
  ```

  **Automated Verification**:
  ```bash
  # Agent runs compilation check:
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  # Assert: File exists at constants/OrderStatus.java
  ```

  **Commit**: YES
  - Message: `feat(order): add OrderStatus enum for order lifecycle`
  - Files: `constants/OrderStatus.java`

---

- [ ] 2. Create UserCart Entity

  **What to do**:
  - Create `entities/UserCart.java` as MongoDB @Document
  - Fields: id (String), userId (String), cartItems (List<CartItem>), lastUpdated (Date)
  - Create embedded `CartItem` class (or reuse Item from daos)
  - Include getters/setters, equals/hashCode

  **Must NOT do**:
  - Do NOT use @DBRef for user (just store userId string for simplicity)
  - Do NOT add complex cart logic here (just data structure)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple entity creation following existing patterns
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Tasks 6, 8
  - **Blocked By**: None (can start immediately)

  **References**:
  - `src/main/java/trantantai/trantantai/entities/Invoice.java:17-56` - @Document pattern
  - `src/main/java/trantantai/trantantai/daos/Item.java` - Cart item structure to embed
  - `src/main/java/trantantai/trantantai/entities/ItemInvoice.java` - Embedded document pattern

  **Files to Create**:
  - `src/main/java/trantantai/trantantai/entities/UserCart.java`

  **Acceptance Criteria**:
  ```java
  // @Document(collection = "user_carts")
  // Has fields: id, userId, cartItems (List<Item>), lastUpdated
  // Compiles without errors
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(cart): add UserCart entity for cart persistence`
  - Files: `entities/UserCart.java`

---

- [ ] 3. Create IUserCartRepository

  **What to do**:
  - Create `repositories/IUserCartRepository.java`
  - Extend MongoRepository<UserCart, String>
  - Add: `Optional<UserCart> findByUserId(String userId)`
  - Add: `void deleteByUserId(String userId)`

  **Must NOT do**:
  - Do NOT add complex queries yet

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple repository interface
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Tasks 6, 8
  - **Blocked By**: None

  **References**:
  - `src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java` - Repository pattern
  - `src/main/java/trantantai/trantantai/repositories/IUserRepository.java` - Custom query methods

  **Files to Create**:
  - `src/main/java/trantantai/trantantai/repositories/IUserCartRepository.java`

  **Acceptance Criteria**:
  ```java
  // Extends MongoRepository<UserCart, String>
  // Has findByUserId(String) method
  // Has deleteByUserId(String) method
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: NO (groups with Task 2)

---

### Wave 2: Data Layer (After Wave 1)

---

- [ ] 4. Extend Invoice Entity with OrderStatus

  **What to do**:
  - Add `orderStatus` field of type `OrderStatus` to Invoice.java
  - Default value: `OrderStatus.PROCESSING`
  - Add getter/setter
  - Update toString() to include orderStatus
  - Update all-args constructor

  **Must NOT do**:
  - Do NOT change existing fields
  - Do NOT modify payment-related logic

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple field addition to existing entity
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6)
  - **Blocks**: Tasks 5, 7
  - **Blocked By**: Task 1

  **References**:
  - `src/main/java/trantantai/trantantai/entities/Invoice.java` - File to modify (all lines)
  - `src/main/java/trantantai/trantantai/constants/OrderStatus.java` - Enum to use (created in Task 1)

  **Files to Modify**:
  - `src/main/java/trantantai/trantantai/entities/Invoice.java`

  **Acceptance Criteria**:
  ```java
  // New field: private OrderStatus orderStatus = OrderStatus.PROCESSING;
  // Getter: getOrderStatus()
  // Setter: setOrderStatus(OrderStatus)
  // Constructor updated
  // toString() updated
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(order): add orderStatus field to Invoice entity`
  - Files: `entities/Invoice.java`

---

- [ ] 5. Extend IInvoiceRepository with Custom Queries

  **What to do**:
  - Add: `List<Invoice> findByUserIdOrderByInvoiceDateDesc(String userId)`
  - Add: `Page<Invoice> findAll(Pageable pageable)` (already inherited, but add findAllByOrderByInvoiceDateDesc)
  - Add: `Page<Invoice> findAllByOrderByInvoiceDateDesc(Pageable pageable)`
  - Add: `long countByOrderStatus(OrderStatus status)`

  **Must NOT do**:
  - Do NOT add complex aggregation queries

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple Spring Data method naming conventions
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 6)
  - **Blocks**: Tasks 7, 10, 11
  - **Blocked By**: Task 4

  **References**:
  - `src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java` - File to modify
  - `src/main/java/trantantai/trantantai/repositories/IBookRepository.java` - Pagination example

  **Files to Modify**:
  - `src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java`

  **Acceptance Criteria**:
  ```java
  // Has: findByUserIdOrderByInvoiceDateDesc(String userId)
  // Has: findAllByOrderByInvoiceDateDesc(Pageable pageable)
  // Has: countByOrderStatus(OrderStatus status)
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(order): add order query methods to IInvoiceRepository`
  - Files: `repositories/IInvoiceRepository.java`

---

- [ ] 6. Extend CartService for Database Operations

  **What to do**:
  - Inject IUserCartRepository
  - Add method: `saveCartToDatabase(HttpSession session, String userId)` - converts session cart to UserCart and saves
  - Add method: `restoreCartFromDatabase(HttpSession session, String userId)` - loads UserCart, merges with session, updates session
  - Add method: `mergeCart(Cart sessionCart, UserCart dbCart)` - merge logic with quantity summing
  - Handle edge cases: item not in stock (skip), quantity exceeds stock (cap)

  **Must NOT do**:
  - Do NOT modify existing session cart methods
  - Do NOT add price change detection (use current price)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Core business logic requiring careful implementation
  - **Skills**: None required
    - Standard Spring service patterns

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5)
  - **Blocks**: Tasks 8, 9
  - **Blocked By**: Tasks 2, 3

  **References**:
  - `src/main/java/trantantai/trantantai/services/CartService.java` - File to modify (all lines)
  - `src/main/java/trantantai/trantantai/daos/Cart.java` - Cart structure
  - `src/main/java/trantantai/trantantai/daos/Item.java` - Item structure
  - `src/main/java/trantantai/trantantai/services/BookService.java:getBookById` - For stock validation

  **Files to Modify**:
  - `src/main/java/trantantai/trantantai/services/CartService.java`

  **Acceptance Criteria**:
  ```java
  // IUserCartRepository injected
  // saveCartToDatabase(session, userId) - saves session cart to DB
  // restoreCartFromDatabase(session, userId) - restores and merges
  // mergeCart() - sums quantities, caps at stock
  // Invalid items skipped with logging
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(cart): add database persistence methods to CartService`
  - Files: `services/CartService.java`

---

### Wave 3: Service Layer (After Wave 2)

---

- [ ] 7. Create OrderService

  **What to do**:
  - Create `services/OrderService.java`
  - Inject: IInvoiceRepository, BookService
  - Methods:
    - `List<Invoice> getOrdersByUserId(String userId)` - for user order history
    - `Page<Invoice> getAllOrders(int page, int size)` - for admin list
    - `Optional<Invoice> getOrderById(String id)` - for detail view
    - `Invoice updateOrderStatus(String orderId, OrderStatus newStatus)` - status update
    - `Invoice cancelOrder(String orderId)` - cancel with stock restoration
    - `Map<String, Long> getOrderStatistics()` - counts by status for dashboard
  - Populate transient Book objects in ItemInvoice when returning orders

  **Must NOT do**:
  - Do NOT add email notifications
  - Do NOT add status transition validation (any-to-any allowed except terminal states)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Core business logic with multiple methods
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 9)
  - **Blocks**: Tasks 10, 11
  - **Blocked By**: Tasks 4, 5

  **References**:
  - `src/main/java/trantantai/trantantai/services/BookService.java` - Service pattern, stock methods
  - `src/main/java/trantantai/trantantai/services/CartService.java:144-152` - Status update pattern
  - `src/main/java/trantantai/trantantai/entities/Invoice.java` - Entity structure
  - `src/main/java/trantantai/trantantai/entities/ItemInvoice.java:19-20` - @Transient Book field

  **Files to Create**:
  - `src/main/java/trantantai/trantantai/services/OrderService.java`

  **Acceptance Criteria**:
  ```java
  // @Service annotated
  // getOrdersByUserId - returns list sorted by date desc
  // getAllOrders - returns Page with pagination
  // getOrderById - returns Optional<Invoice>
  // updateOrderStatus - changes status, saves
  // cancelOrder - sets CANCELLED, restores stock via BookService
  // getOrderStatistics - returns map with counts
  // Book objects populated in ItemInvoice
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(order): add OrderService for order management`
  - Files: `services/OrderService.java`

---

- [ ] 8. Create Custom LogoutHandler for Cart Save

  **What to do**:
  - Create `config/CustomLogoutHandler.java` implementing LogoutHandler
  - In logout(), get user from Authentication, get session, call CartService.saveCartToDatabase()
  - Handle case where user is not authenticated (skip save)
  - Log errors but don't fail logout

  **Must NOT do**:
  - Do NOT modify existing logout flow
  - Do NOT throw exceptions that block logout

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single handler with clear responsibility
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 7, 9)
  - **Blocks**: Task 12
  - **Blocked By**: Task 6

  **References**:
  - `src/main/java/trantantai/trantantai/config/SecurityConfig.java:78-84` - Current logout config
  - `src/main/java/trantantai/trantantai/services/CartService.java` - saveCartToDatabase method
  - Spring Security LogoutHandler interface

  **Files to Create**:
  - `src/main/java/trantantai/trantantai/config/CustomLogoutHandler.java`

  **Acceptance Criteria**:
  ```java
  // Implements LogoutHandler
  // @Component annotated
  // logout() method gets user, saves cart
  // Handles null authentication gracefully
  // Logs errors without throwing
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(cart): add LogoutHandler to save cart before session destroy`
  - Files: `config/CustomLogoutHandler.java`

---

- [ ] 9. Create Custom AuthenticationSuccessHandler for Cart Restore

  **What to do**:
  - Create `config/CustomAuthSuccessHandler.java` implementing AuthenticationSuccessHandler
  - In onAuthenticationSuccess(), get user, restore cart from DB, merge with session cart
  - Show flash message if items were skipped (out of stock or deleted)
  - Redirect to original destination or home

  **Must NOT do**:
  - Do NOT change default redirect behavior
  - Do NOT throw exceptions that block login

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single handler with clear responsibility
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 7, 8)
  - **Blocks**: Task 12
  - **Blocked By**: Task 6

  **References**:
  - `src/main/java/trantantai/trantantai/config/SecurityConfig.java:63-68` - Form login config
  - `src/main/java/trantantai/trantantai/services/CartService.java` - restoreCartFromDatabase method
  - Spring Security AuthenticationSuccessHandler interface

  **Files to Create**:
  - `src/main/java/trantantai/trantantai/config/CustomAuthSuccessHandler.java`

  **Acceptance Criteria**:
  ```java
  // Implements AuthenticationSuccessHandler
  // @Component annotated
  // onAuthenticationSuccess() restores/merges cart
  // Redirects to "/" (or saved request)
  // Handles errors gracefully
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: NO (groups with Task 8)

---

### Wave 4: Controller Layer (After Wave 3)

---

- [ ] 10. Create OrderController for User Order History

  **What to do**:
  - Create `controllers/OrderController.java`
  - Endpoint: `GET /orders` - shows user's order history
  - Get current user from Authentication, call OrderService.getOrdersByUserId()
  - Pass orders to view with pagination (10 per page)
  - Handle empty orders gracefully

  **Must NOT do**:
  - Do NOT add order cancellation for users (admin-only)
  - Do NOT add reorder functionality

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple controller with single endpoint
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 11, 12)
  - **Blocks**: Task 13
  - **Blocked By**: Tasks 5, 7

  **References**:
  - `src/main/java/trantantai/trantantai/controllers/CartController.java` - Controller pattern, user authentication
  - `src/main/java/trantantai/trantantai/services/OrderService.java` - Service to use

  **Files to Create**:
  - `src/main/java/trantantai/trantantai/controllers/OrderController.java`

  **Acceptance Criteria**:
  ```java
  // @Controller, @RequestMapping("/orders")
  // GET /orders returns "user/orders" view
  // Model has: orders, currentPage, totalPages
  // Only authenticated users can access
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(order): add OrderController for user order history`
  - Files: `controllers/OrderController.java`

---

- [ ] 11. Extend AdminController with Order Management

  **What to do**:
  - Add endpoints to AdminController.java:
    - `GET /admin/orders` - list all orders with pagination
    - `GET /admin/orders/{id}` - order detail view
    - `POST /admin/orders/{id}/status` - update order status
    - `POST /admin/orders/{id}/cancel` - cancel order with stock restore
  - Use OrderService for all operations
  - Add success/error flash messages

  **Must NOT do**:
  - Do NOT add bulk operations
  - Do NOT add order export

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Multiple endpoints with business logic
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 10, 12)
  - **Blocks**: Tasks 14, 15, 17
  - **Blocked By**: Tasks 5, 7

  **References**:
  - `src/main/java/trantantai/trantantai/controllers/AdminController.java` - File to modify (all 233 lines)
  - `src/main/java/trantantai/trantantai/services/OrderService.java` - Service to use
  - `src/main/java/trantantai/trantantai/controllers/AdminController.java:43-64` - List pattern with pagination

  **Files to Modify**:
  - `src/main/java/trantantai/trantantai/controllers/AdminController.java`

  **Acceptance Criteria**:
  ```java
  // OrderService injected
  // GET /admin/orders - list with pagination
  // GET /admin/orders/{id} - detail view
  // POST /admin/orders/{id}/status?status=SHIPPED - updates status
  // POST /admin/orders/{id}/cancel - cancels with stock restore
  // Flash messages for success/error
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(admin): add order management endpoints to AdminController`
  - Files: `controllers/AdminController.java`

---

- [ ] 12. Update SecurityConfig with Custom Handlers

  **What to do**:
  - Inject CustomLogoutHandler and CustomAuthSuccessHandler
  - Update `.logout()` to add `.addLogoutHandler(customLogoutHandler)` BEFORE invalidateHttpSession
  - Update `.formLogin()` to use `.successHandler(customAuthSuccessHandler)`
  - Update `.oauth2Login()` to use same success handler
  - Add `/orders/**` to USER role authorization

  **Must NOT do**:
  - Do NOT remove existing security rules
  - Do NOT change CSRF settings

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Configuration changes only
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 10, 11)
  - **Blocks**: None
  - **Blocked By**: Tasks 8, 9

  **References**:
  - `src/main/java/trantantai/trantantai/config/SecurityConfig.java` - File to modify (all 97 lines)
  - `src/main/java/trantantai/trantantai/config/CustomLogoutHandler.java` - To inject
  - `src/main/java/trantantai/trantantai/config/CustomAuthSuccessHandler.java` - To inject

  **Files to Modify**:
  - `src/main/java/trantantai/trantantai/config/SecurityConfig.java`

  **Acceptance Criteria**:
  ```java
  // CustomLogoutHandler added before invalidateHttpSession
  // CustomAuthSuccessHandler used for form and oauth2 login
  // /orders/** requires USER role
  // Application starts without errors
  ```

  **Automated Verification**:
  ```bash
  cd trantantai && mvn spring-boot:run &
  sleep 30
  curl -s http://localhost:8080 | grep -q "BookHaven"
  # Assert: Exit code 0
  # Then stop the server
  ```

  **Commit**: YES
  - Message: `feat(security): configure custom auth handlers for cart persistence`
  - Files: `config/SecurityConfig.java`

---

### Wave 5: View Layer (After Wave 4)

---

- [ ] 13. Create User Orders View

  **What to do**:
  - Create `templates/user/orders.html`
  - Use main layout.html fragments (header, footer)
  - Display order list: date, total, status (with badge), item count
  - Show order items expandable/collapsible
  - Empty state when no orders
  - Pagination if > 10 orders

  **Must NOT do**:
  - Do NOT add cancel button (admin-only)
  - Do NOT add reorder button

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Frontend view with styling
  - **Skills**: `['frontend-ui-ux']`
    - Thymeleaf + Bootstrap styling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5 (all view tasks)
  - **Blocks**: Task 18
  - **Blocked By**: Task 10

  **References**:
  - `src/main/resources/templates/layout.html` - Main layout fragments
  - `src/main/resources/templates/book/cart.html` - Cart styling reference
  - `src/main/resources/templates/admin/books/list.html:162-188` - Pagination pattern

  **Files to Create**:
  - `src/main/resources/templates/user/orders.html`

  **Acceptance Criteria**:
  ```
  # Via Playwright browser automation:
  1. Login as USER
  2. Navigate to: http://localhost:8080/orders
  3. Assert: Page title contains "Đơn hàng"
  4. Assert: Order list or empty state visible
  5. Screenshot: .sisyphus/evidence/task-13-orders-view.png
  ```

  **Commit**: YES
  - Message: `feat(ui): add user orders history view`
  - Files: `templates/user/orders.html`

---

- [ ] 14. Create Admin Orders List View

  **What to do**:
  - Create `templates/admin/orders/list.html`
  - Use admin layout fragments (sidebar, topbar)
  - Table: Order ID, Customer, Date, Total, Payment Status, Order Status, Actions
  - Status badges with colors (Processing=blue, Shipped=orange, Delivered=green, Cancelled=red)
  - Pagination (same pattern as books list)
  - Link to detail page

  **Must NOT do**:
  - Do NOT add inline status edit (do it in detail page)
  - Do NOT add bulk actions

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Complex admin table with styling
  - **Skills**: `['frontend-ui-ux']`
    - Admin dashboard styling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5
  - **Blocks**: Task 16
  - **Blocked By**: Task 11

  **References**:
  - `src/main/resources/templates/admin/layout.html` - Admin layout fragments
  - `src/main/resources/templates/admin/books/list.html` - Table + pagination pattern (all 866 lines)
  - `src/main/resources/templates/admin/inventory/list.html` - Another list example

  **Files to Create**:
  - `src/main/resources/templates/admin/orders/list.html`

  **Acceptance Criteria**:
  ```
  # Via Playwright browser automation:
  1. Login as ADMIN
  2. Navigate to: http://localhost:8080/admin/orders
  3. Assert: Table with columns (ID, Customer, Date, Total, Status)
  4. Assert: Pagination visible if orders > 20
  5. Assert: Status badges have appropriate colors
  6. Screenshot: .sisyphus/evidence/task-14-admin-orders-list.png
  ```

  **Commit**: YES
  - Message: `feat(admin): add orders list view`
  - Files: `templates/admin/orders/list.html`

---

- [ ] 15. Create Admin Order Detail View

  **What to do**:
  - Create `templates/admin/orders/detail.html`
  - Order info: ID, Customer, Date, Total, Payment method, Payment status, Order status
  - Items table: Book title, Quantity, Price, Subtotal
  - Status update dropdown with all OrderStatus values
  - Cancel button (with confirmation modal)
  - Back to list link

  **Must NOT do**:
  - Do NOT add order editing
  - Do NOT add refund functionality

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Detail page with forms and modals
  - **Skills**: `['frontend-ui-ux']`
    - Bootstrap modals, forms

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5
  - **Blocks**: None
  - **Blocked By**: Task 11

  **References**:
  - `src/main/resources/templates/admin/books/edit.html` - Detail/edit page pattern
  - `src/main/resources/templates/admin/books/list.html:196-228` - Delete modal pattern

  **Files to Create**:
  - `src/main/resources/templates/admin/orders/detail.html`

  **Acceptance Criteria**:
  ```
  # Via Playwright browser automation:
  1. Login as ADMIN
  2. Navigate to: http://localhost:8080/admin/orders/{orderId}
  3. Assert: Order details visible
  4. Assert: Items table visible
  5. Assert: Status dropdown with options
  6. Assert: Cancel button present
  7. Screenshot: .sisyphus/evidence/task-15-admin-order-detail.png
  ```

  **Commit**: YES
  - Message: `feat(admin): add order detail view with status update`
  - Files: `templates/admin/orders/detail.html`

---

- [ ] 16. Update Admin Layout with Orders Link

  **What to do**:
  - Update `templates/admin/layout.html`
  - Change orders nav link from `href="#"` to `href="/admin/orders"` (around line 100)
  - Optionally add dynamic badge showing pending orders count

  **Must NOT do**:
  - Do NOT change other navigation links
  - Do NOT modify layout structure

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple link update
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5
  - **Blocks**: None
  - **Blocked By**: Task 14

  **References**:
  - `src/main/resources/templates/admin/layout.html:99-109` - Orders nav item to update

  **Files to Modify**:
  - `src/main/resources/templates/admin/layout.html`

  **Acceptance Criteria**:
  ```html
  <!-- Line ~100: href="#" changed to href="/admin/orders" -->
  ```

  **Automated Verification**:
  ```bash
  grep -q 'href="/admin/orders"' trantantai/src/main/resources/templates/admin/layout.html
  # Assert: Exit code 0
  ```

  **Commit**: NO (groups with Task 14)

---

- [ ] 17. Update Admin Dashboard with Order Statistics

  **What to do**:
  - Update `templates/admin/dashboard.html`
  - Add order statistics cards: Total Orders, Processing, Shipped, Delivered
  - Get stats from controller via model attribute (from OrderService.getOrderStatistics())
  - Update AdminController.dashboard() to add stats

  **Must NOT do**:
  - Do NOT add complex charts (just numbers)
  - Do NOT add real-time updates

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Adding stats display to existing page
  - **Skills**: `['frontend-ui-ux']`
    - Dashboard card styling

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5
  - **Blocks**: None
  - **Blocked By**: Task 11

  **References**:
  - `src/main/resources/templates/admin/dashboard.html` - File to modify
  - `src/main/java/trantantai/trantantai/controllers/AdminController.java:31-40` - Dashboard method to update

  **Files to Modify**:
  - `src/main/resources/templates/admin/dashboard.html`
  - `src/main/java/trantantai/trantantai/controllers/AdminController.java` (dashboard method)

  **Acceptance Criteria**:
  ```
  # Via Playwright browser automation:
  1. Login as ADMIN
  2. Navigate to: http://localhost:8080/admin
  3. Assert: Order statistics cards visible
  4. Assert: Shows counts for Processing, Shipped, Delivered
  5. Screenshot: .sisyphus/evidence/task-17-dashboard-stats.png
  ```

  **Commit**: YES
  - Message: `feat(admin): add order statistics to dashboard`
  - Files: `templates/admin/dashboard.html`, `controllers/AdminController.java`

---

- [ ] 18. Update User Layout with Orders Link

  **What to do**:
  - Update `templates/layout.html`
  - In the user dropdown (authenticated), change "Đơn hàng của tôi" link from `/cart` to `/orders`
  - Around lines 120-127

  **Must NOT do**:
  - Do NOT modify admin dropdown items
  - Do NOT change other links

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple link update
  - **Skills**: None required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5
  - **Blocks**: None
  - **Blocked By**: Task 13

  **References**:
  - `src/main/resources/templates/layout.html:120-127` - User dropdown item to update

  **Files to Modify**:
  - `src/main/resources/templates/layout.html`

  **Acceptance Criteria**:
  ```html
  <!-- Line ~121: href="/cart" changed to href="/orders" for "Đơn hàng của tôi" -->
  ```

  **Automated Verification**:
  ```bash
  grep -A2 "Đơn hàng của tôi" trantantai/src/main/resources/templates/layout.html | grep -q 'href="/orders"'
  # Assert: Exit code 0
  ```

  **Commit**: NO (groups with Task 13)

---

## Commit Strategy

| After Task | Message | Files |
|------------|---------|-------|
| 1 | `feat(order): add OrderStatus enum for order lifecycle` | constants/OrderStatus.java |
| 2+3 | `feat(cart): add UserCart entity and repository for persistence` | entities/UserCart.java, repositories/IUserCartRepository.java |
| 4 | `feat(order): add orderStatus field to Invoice entity` | entities/Invoice.java |
| 5 | `feat(order): add order query methods to IInvoiceRepository` | repositories/IInvoiceRepository.java |
| 6 | `feat(cart): add database persistence methods to CartService` | services/CartService.java |
| 7 | `feat(order): add OrderService for order management` | services/OrderService.java |
| 8+9 | `feat(cart): add custom auth handlers for cart persistence` | config/CustomLogoutHandler.java, config/CustomAuthSuccessHandler.java |
| 10 | `feat(order): add OrderController for user order history` | controllers/OrderController.java |
| 11 | `feat(admin): add order management endpoints to AdminController` | controllers/AdminController.java |
| 12 | `feat(security): configure custom auth handlers` | config/SecurityConfig.java |
| 13+18 | `feat(ui): add user orders history view with nav link` | templates/user/orders.html, templates/layout.html |
| 14+16 | `feat(admin): add orders list view with nav link` | templates/admin/orders/list.html, templates/admin/layout.html |
| 15 | `feat(admin): add order detail view with status update` | templates/admin/orders/detail.html |
| 17 | `feat(admin): add order statistics to dashboard` | templates/admin/dashboard.html, controllers/AdminController.java |

---

## Success Criteria

### Verification Commands
```bash
# Build passes
cd trantantai && mvn clean compile

# Application starts
mvn spring-boot:run

# Manual verification via browser:
# 1. Login as USER, go to /orders - see order history
# 2. Login as ADMIN, go to /admin/orders - see all orders
# 3. Update order status - status changes
# 4. Cancel order - stock restored
# 5. Add items to cart, logout, login - cart restored
```

### Final Checklist
- [ ] OrderStatus enum exists with 4 values
- [ ] Invoice has orderStatus field defaulting to PROCESSING
- [ ] UserCart entity persists carts to MongoDB
- [ ] Cart saves on logout for logged-in users
- [ ] Cart restores on login with merge
- [ ] User can view orders at /orders
- [ ] Admin can view/manage orders at /admin/orders
- [ ] Admin can cancel orders with stock restoration
- [ ] All navigation links work correctly
- [ ] No compilation errors
- [ ] Application starts successfully
