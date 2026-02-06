# MoMo QR Payment Integration

## TL;DR

> **Quick Summary**: Integrate MoMo QR payment into the bookstore checkout flow. When user selects MOMO payment method and clicks "Xác nhận đặt hàng", create a pending invoice, call MoMo API to get QR code, redirect to payment page, and update order status via IPN callback.
> 
> **Deliverables**:
> - MoMo SDK dependency and configuration
> - PaymentStatus enum and Invoice entity updates
> - MoMoService for API integration
> - MoMoController for IPN callback handling
> - Modified checkout flow for MOMO vs COD
> - New momo-payment.html page with QR display
> 
> **Estimated Effort**: Medium (8-12 hours)
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 → Task 2 → Task 3 → Task 5 → Task 7 → Task 8

---

## Context

### Original Request
Tích hợp thanh toán MoMo QR khi user click "Xác nhận đặt hàng" với phương thức MOMO:
1. Create MoMo payment request
2. Display QR code for payment
3. Handle IPN callback to verify payment
4. Sync order status with MoMo

### Interview Summary
**Key Discussions**:
- MoMo credentials: Use public sandbox test credentials
- QR display: New dedicated `/cart/momo-payment` page with QR, amount, instructions
- Timeout: No auto-cancel - keep simple (manual cancel only)
- IPN URL: Configurable in application.properties for ngrok/tunnel testing
- Tests: No automated tests - manual verification with browser + MoMo sandbox

**Research Findings**:
- MoMo SDK: `io.github.momo-wallet:momopayment:1.0`
- API returns: payUrl, qrCodeUrl, deeplink, transId
- IPN requires HMAC-SHA256 signature verification with Encoder.signHmacSHA256()
- IPN must return HTTP 204 No Content
- Sandbox endpoint: `https://test-payment.momo.vn/v2/gateway/api`

### Gap Analysis (Self-Review)
**Identified Gaps** (addressed in plan):
- Need to handle case when MoMo API call fails → Show error, don't create pending invoice
- Need return URL for when user completes payment on MoMo → `/cart/momo-return`
- Need to prevent duplicate IPN processing → Check if already PAID before updating
- COD flow must remain unchanged → Add paymentMethod check in placeOrder

---

## Work Objectives

### Core Objective
Enable MoMo QR payment in the checkout flow so users can pay via MoMo wallet instead of COD.

### Concrete Deliverables
- `pom.xml` - MoMo SDK dependency added
- `application.properties` - MoMo configuration (sandbox credentials, URLs)
- `PaymentStatus.java` - New enum for payment states
- `PaymentMethod.java` - New enum for payment methods
- `Invoice.java` - Updated with paymentStatus, paymentMethod, momoTransactionId, momoRequestId fields
- `MoMoConfig.java` - Configuration class for MoMo properties
- `MoMoService.java` - Service for MoMo API calls
- `MoMoController.java` - Controller for IPN callback
- `CartController.java` - Modified placeOrder for MOMO flow
- `CartService.java` - New methods for pending invoice and payment updates
- `SecurityConfig.java` - IPN endpoint public access
- `momo-payment.html` - QR code display page
- `checkout.html` - Minor update for MOMO flow

### Definition of Done
- [ ] User can complete checkout with COD (existing flow unchanged)
- [ ] User can complete checkout with MOMO and see QR code page
- [ ] MoMo IPN callback updates invoice to PAID status
- [ ] User sees success page after payment confirmed
- [ ] Application compiles and runs without errors

### Must Have
- MoMo SDK integration with sandbox credentials
- QR code display on dedicated page
- IPN callback with signature verification
- Invoice status tracking (PENDING_PAYMENT → PAID)
- COD flow unchanged

### Must NOT Have (Guardrails)
- NO automatic payment timeout/cancellation
- NO refund functionality
- NO admin UI for payment management
- NO additional payment methods beyond COD and MOMO
- NO changes to existing Book, User, Category entities
- NO WebSocket for real-time updates (use polling instead)
- DO NOT break existing COD checkout flow

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: YES (spring-boot-starter-test)
- **User wants tests**: NO (manual verification only)
- **Framework**: N/A
- **QA approach**: Manual verification with browser + MoMo sandbox

### Manual Verification Procedures

Each TODO includes verification steps that can be executed manually or via automated browser tools.

**Verification Evidence Location**: `.sisyphus/evidence/`

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately - Foundation):
├── Task 1: Add MoMo dependency to pom.xml
├── Task 2: Add MoMo config to application.properties
└── Task 3: Create PaymentStatus and PaymentMethod enums

Wave 2 (After Wave 1 - Core Implementation):
├── Task 4: Update Invoice entity with new fields
├── Task 5: Create MoMoConfig class
└── Task 6: Update SecurityConfig for IPN endpoint

Wave 3 (After Wave 2 - Services):
├── Task 7: Create MoMoService
└── Task 8: Update CartService with payment methods

Wave 4 (After Wave 3 - Controllers & UI):
├── Task 9: Create MoMoController (IPN)
├── Task 10: Update CartController for MOMO flow
└── Task 11: Create momo-payment.html

Wave 5 (After Wave 4 - Final):
└── Task 12: Integration testing and verification

Critical Path: Task 1 → Task 2 → Task 5 → Task 7 → Task 10 → Task 11 → Task 12
Parallel Speedup: ~35% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 5, 7 | 2, 3 |
| 2 | None | 5, 7 | 1, 3 |
| 3 | None | 4, 8 | 1, 2 |
| 4 | 3 | 8, 10 | 5, 6 |
| 5 | 1, 2 | 7 | 4, 6 |
| 6 | None | 9 | 4, 5 |
| 7 | 5 | 8, 9, 10 | 8 (partially) |
| 8 | 3, 4 | 10 | 7 |
| 9 | 6, 7 | 12 | 10, 11 |
| 10 | 4, 7, 8 | 11, 12 | 9 |
| 11 | 10 | 12 | 9 |
| 12 | 9, 10, 11 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Approach |
|------|-------|---------------------|
| 1 | 1, 2, 3 | Run in parallel - independent config/setup |
| 2 | 4, 5, 6 | Run in parallel after Wave 1 |
| 3 | 7, 8 | Run in parallel after Wave 2 |
| 4 | 9, 10, 11 | Run 9 and 10 in parallel, then 11 |
| 5 | 12 | Final integration verification |

---

## TODOs

### Task 1: Add MoMo SDK Dependency

- [ ] 1. Add MoMo SDK dependency to pom.xml

  **What to do**:
  - Add MoMo payment SDK dependency to pom.xml
  - Add Sonatype OSSRH repository for the SDK

  **Must NOT do**:
  - Do not modify any other dependencies
  - Do not change Spring Boot version

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple XML modification, single file change
  - **Skills**: None needed
    - Simple Maven dependency addition

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Tasks 5, 7 (need SDK available)
  - **Blocked By**: None

  **References**:
  - `trantantai/pom.xml:32-98` - Current dependencies section (add after springdoc dependency around line 80)
  - MoMo SDK: https://github.com/momo-wallet/java - Maven coordinates

  **Acceptance Criteria**:
  ```bash
  # Agent runs in trantantai directory:
  mvn dependency:resolve -DincludeArtifactIds=momopayment
  # Assert: Shows "momopayment:jar:1.0" resolved successfully
  
  mvn compile -q
  # Assert: Exit code 0 (compilation succeeds)
  ```

  **Commit**: YES (groups with Task 2, 3)
  - Message: `feat(payment): add MoMo SDK dependency and configuration`
  - Files: `pom.xml`

---

### Task 2: Add MoMo Configuration Properties

- [ ] 2. Add MoMo config to application.properties

  **What to do**:
  - Add MoMo sandbox credentials and endpoint configuration
  - Use MoMo's public test credentials:
    - partnerCode: `MOMOBKUN20180529`
    - accessKey: `klm05TvNBzhg7h7j`
    - secretKey: `at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa`
  - Add configurable IPN and return URLs

  **Must NOT do**:
  - Do not modify existing properties
  - Do not use production credentials

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple properties file addition
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Tasks 5, 7
  - **Blocked By**: None

  **References**:
  - `trantantai/src/main/resources/application.properties:1-29` - Current config (add after line 29)
  - MoMo sandbox docs: https://developers.momo.vn/v3/docs/payment/onboarding/test-instructions

  **Configuration to add**:
  ```properties
  # MoMo Payment Configuration (Sandbox)
  momo.partner-code=MOMOBKUN20180529
  momo.access-key=klm05TvNBzhg7h7j
  momo.secret-key=at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa
  momo.endpoint=https://test-payment.momo.vn/v2/gateway/api/create
  momo.query-endpoint=https://test-payment.momo.vn/v2/gateway/api/query
  momo.return-url=http://localhost:8080/cart/momo-return
  momo.ipn-url=http://localhost:8080/api/momo/ipn
  ```

  **Acceptance Criteria**:
  ```bash
  # Agent runs:
  grep -c "momo.partner-code" trantantai/src/main/resources/application.properties
  # Assert: Output is "1"
  
  grep "momo.endpoint" trantantai/src/main/resources/application.properties
  # Assert: Contains "test-payment.momo.vn"
  ```

  **Commit**: YES (groups with Task 1, 3)
  - Message: `feat(payment): add MoMo SDK dependency and configuration`
  - Files: `application.properties`

---

### Task 3: Create Payment Enums

- [ ] 3. Create PaymentStatus and PaymentMethod enums

  **What to do**:
  - Create `PaymentStatus` enum in `constants` package with values: PENDING_PAYMENT, PAID, PAYMENT_FAILED, COD_PENDING
  - Create `PaymentMethod` enum in `constants` package with values: COD, MOMO

  **Must NOT do**:
  - Do not modify existing enums (Role, Provider)
  - Do not add more payment methods than COD and MOMO

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple enum creation, follows existing pattern
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Task 4, 8
  - **Blocked By**: None

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/constants/Role.java` - Follow this enum pattern
  - `trantantai/src/main/java/trantantai/trantantai/constants/Provider.java` - Another enum example

  **Files to create**:
  1. `trantantai/src/main/java/trantantai/trantantai/constants/PaymentStatus.java`
  2. `trantantai/src/main/java/trantantai/trantantai/constants/PaymentMethod.java`

  **Acceptance Criteria**:
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0
  
  # Verify files exist:
  ls src/main/java/trantantai/trantantai/constants/PaymentStatus.java
  ls src/main/java/trantantai/trantantai/constants/PaymentMethod.java
  # Assert: Both files exist
  ```

  **Commit**: YES (groups with Task 1, 2)
  - Message: `feat(payment): add MoMo SDK dependency and configuration`
  - Files: `PaymentStatus.java`, `PaymentMethod.java`

---

### Task 4: Update Invoice Entity

- [ ] 4. Update Invoice entity with payment fields

  **What to do**:
  - Add `paymentStatus` field (PaymentStatus enum, default: COD_PENDING)
  - Add `paymentMethod` field (PaymentMethod enum)
  - Add `momoTransactionId` field (String, nullable)
  - Add `momoRequestId` field (String, nullable) - for querying status
  - Add getters/setters for new fields
  - Update constructors and toString()

  **Must NOT do**:
  - Do not change existing field types
  - Do not remove any existing fields
  - Do not change collection name

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Entity field additions, straightforward
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6)
  - **Blocks**: Tasks 8, 10
  - **Blocked By**: Task 3 (needs enums)

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/entities/Invoice.java:1-121` - Current entity structure
  - `trantantai/src/main/java/trantantai/trantantai/constants/PaymentStatus.java` - Enum to use (from Task 3)

  **Acceptance Criteria**:
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0
  
  grep -c "paymentStatus" src/main/java/trantantai/trantantai/entities/Invoice.java
  # Assert: Output >= 2 (field + getter/setter)
  
  grep -c "momoTransactionId" src/main/java/trantantai/trantantai/entities/Invoice.java
  # Assert: Output >= 2
  ```

  **Commit**: YES
  - Message: `feat(payment): update Invoice entity with payment tracking fields`
  - Files: `Invoice.java`

---

### Task 5: Create MoMoConfig Class

- [ ] 5. Create MoMoConfig configuration class

  **What to do**:
  - Create `MoMoConfig.java` in `config` package
  - Use `@Configuration` and `@ConfigurationProperties(prefix = "momo")`
  - Map all momo.* properties: partnerCode, accessKey, secretKey, endpoint, queryEndpoint, returnUrl, ipnUrl
  - Create `@Bean` for MoMo Environment initialization

  **Must NOT do**:
  - Do not hardcode credentials (use properties)
  - Do not create production environment (sandbox only for now)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard Spring Boot config class pattern
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 6)
  - **Blocks**: Task 7
  - **Blocked By**: Tasks 1, 2 (needs SDK and properties)

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/config/SecurityConfig.java` - Follow config class pattern
  - `trantantai/src/main/java/trantantai/trantantai/config/MongoConfig.java` - Another config example
  - MoMo SDK Environment: https://github.com/momo-wallet/java - Environment class usage

  **File to create**:
  - `trantantai/src/main/java/trantantai/trantantai/config/MoMoConfig.java`

  **Acceptance Criteria**:
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0
  
  grep "@ConfigurationProperties" src/main/java/trantantai/trantantai/config/MoMoConfig.java
  # Assert: Contains @ConfigurationProperties(prefix = "momo")
  ```

  **Commit**: YES
  - Message: `feat(payment): add MoMo configuration class`
  - Files: `MoMoConfig.java`

---

### Task 6: Update SecurityConfig for IPN Endpoint

- [ ] 6. Update SecurityConfig to allow public IPN access

  **What to do**:
  - Add `/api/momo/ipn` to permitAll() list
  - Add CSRF exception for `/api/momo/**` endpoints
  - Keep all other security rules unchanged

  **Must NOT do**:
  - Do not change authentication for other endpoints
  - Do not disable CSRF globally
  - Do not modify OAuth2 configuration

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Small addition to existing config
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5)
  - **Blocks**: Task 9
  - **Blocked By**: None

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/config/SecurityConfig.java:28-58` - Current requestMatchers configuration

  **Acceptance Criteria**:
  ```bash
  # Agent runs:
  grep "momo" trantantai/src/main/java/trantantai/trantantai/config/SecurityConfig.java
  # Assert: Contains "/api/momo" permitAll configuration
  
  mvn compile -q -f trantantai/pom.xml
  # Assert: Exit code 0
  ```

  **Commit**: YES
  - Message: `feat(payment): configure security for MoMo IPN callback`
  - Files: `SecurityConfig.java`

---

### Task 7: Create MoMoService

- [ ] 7. Create MoMoService for API integration

  **What to do**:
  - Create `MoMoService.java` in `services` package
  - Inject MoMoConfig for credentials
  - Implement `createPayment(String orderId, long amount, String orderInfo)` method:
    - Generate unique requestId
    - Call CreateOrderMoMo.process() with CAPTURE_WALLET
    - Return PaymentResponse (payUrl, qrCodeUrl, deeplink)
  - Implement `queryPaymentStatus(String orderId, String requestId)` method:
    - Call QueryTransactionStatus.process()
    - Return status info
  - Implement `verifyIpnSignature(MoMoIpnRequest request)` method:
    - Build raw data string in alphabetical order
    - Use Encoder.signHmacSHA256() to calculate signature
    - Compare with received signature

  **Must NOT do**:
  - Do not handle refunds
  - Do not implement payment timeout logic

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Core integration logic, needs careful implementation
  - **Skills**: None needed
    - MoMo SDK usage is well-documented in research findings

  **Parallelization**:
  - **Can Run In Parallel**: YES (partially with Task 8)
  - **Parallel Group**: Wave 3
  - **Blocks**: Tasks 8, 9, 10
  - **Blocked By**: Task 5 (needs MoMoConfig)

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/services/CartService.java` - Follow service pattern
  - MoMo SDK CreateOrderMoMo: https://github.com/momo-wallet/java/blob/master/src/main/java/com/mservice/processor/CreateOrderMoMo.java
  - MoMo SDK QueryTransactionStatus: https://github.com/momo-wallet/java/blob/master/src/main/java/com/mservice/processor/QueryTransactionStatus.java
  - MoMo SDK Encoder: https://github.com/momo-wallet/java/blob/master/src/main/java/com/mservice/shared/utils/Encoder.java

  **File to create**:
  - `trantantai/src/main/java/trantantai/trantantai/services/MoMoService.java`

  **Acceptance Criteria**:
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0
  
  grep "CreateOrderMoMo" src/main/java/trantantai/trantantai/services/MoMoService.java
  # Assert: Contains CreateOrderMoMo.process usage
  
  grep "verifyIpnSignature" src/main/java/trantantai/trantantai/services/MoMoService.java
  # Assert: Contains signature verification method
  ```

  **Commit**: YES
  - Message: `feat(payment): implement MoMoService for payment API integration`
  - Files: `MoMoService.java`

---

### Task 8: Update CartService with Payment Methods

- [ ] 8. Update CartService with payment handling methods

  **What to do**:
  - Modify `saveCart()` to accept `PaymentMethod` parameter
  - For COD: Set paymentStatus=COD_PENDING, paymentMethod=COD (existing behavior)
  - For MOMO: Set paymentStatus=PENDING_PAYMENT, paymentMethod=MOMO
  - Add `createPendingInvoice(HttpSession session, PaymentMethod method)` method:
    - Creates invoice with PENDING_PAYMENT status
    - Does NOT clear cart yet (wait for payment confirmation)
    - Returns the created Invoice (need ID for MoMo orderId)
  - Add `updatePaymentStatus(String invoiceId, PaymentStatus status, String transactionId)` method:
    - Updates invoice payment status
    - Sets momoTransactionId if provided
  - Add `findInvoiceById(String id)` method
  - Add `clearCartAfterPayment(HttpSession session)` method

  **Must NOT do**:
  - Do not change stock decrement logic (keep reservation on order creation)
  - Do not remove existing saveCart functionality for COD

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Core business logic changes
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES (with Task 7)
  - **Parallel Group**: Wave 3
  - **Blocks**: Task 10
  - **Blocked By**: Tasks 3, 4 (needs enums and Invoice updates)

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/services/CartService.java:69-120` - Current saveCart() method
  - `trantantai/src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java` - Repository interface

  **Acceptance Criteria**:
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0
  
  grep "createPendingInvoice" src/main/java/trantantai/trantantai/services/CartService.java
  # Assert: Contains new method
  
  grep "updatePaymentStatus" src/main/java/trantantai/trantantai/services/CartService.java
  # Assert: Contains new method
  ```

  **Commit**: YES
  - Message: `feat(payment): update CartService with payment status handling`
  - Files: `CartService.java`

---

### Task 9: Create MoMoController for IPN

- [ ] 9. Create MoMoController for IPN callback handling

  **What to do**:
  - Create `MoMoController.java` in `controllers` package
  - Use `@RestController` and `@RequestMapping("/api/momo")`
  - Implement `POST /ipn` endpoint:
    - Accept MoMo IPN request body (create DTO class `MoMoIpnRequest`)
    - Verify signature using MoMoService
    - If resultCode == 0: Update invoice to PAID
    - If resultCode != 0: Update invoice to PAYMENT_FAILED
    - Return HTTP 204 No Content
  - Add logging for debugging
  - Handle duplicate IPN (check if already PAID before updating)

  **Must NOT do**:
  - Do not add authentication to IPN endpoint
  - Do not return response body (MoMo expects 204)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Critical payment callback handling
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES (with Tasks 10, 11)
  - **Parallel Group**: Wave 4
  - **Blocks**: Task 12
  - **Blocked By**: Tasks 6, 7 (needs SecurityConfig and MoMoService)

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java` - REST controller pattern
  - MoMo IPN structure from research findings

  **Files to create**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/MoMoController.java`
  - `trantantai/src/main/java/trantantai/trantantai/viewmodels/MoMoIpnRequest.java`

  **Acceptance Criteria**:
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0
  
  grep "@PostMapping" src/main/java/trantantai/trantantai/controllers/MoMoController.java
  # Assert: Contains POST /ipn endpoint
  
  grep "204" src/main/java/trantantai/trantantai/controllers/MoMoController.java
  # Assert: Returns 204 No Content
  ```

  **Commit**: YES
  - Message: `feat(payment): implement MoMo IPN callback controller`
  - Files: `MoMoController.java`, `MoMoIpnRequest.java`

---

### Task 10: Update CartController for MOMO Flow

- [ ] 10. Update CartController to handle MOMO payment flow

  **What to do**:
  - Modify `placeOrder()` method:
    - If paymentMethod == COD: Keep existing flow (saveCart with COD)
    - If paymentMethod == MOMO:
      1. Create pending invoice using CartService
      2. Call MoMoService.createPayment() with invoice ID as orderId
      3. Store MoMo response data in session (qrCodeUrl, payUrl, amount)
      4. Redirect to `/cart/momo-payment`
  - Add `GET /cart/momo-payment` endpoint:
    - Display QR code page with payment info from session
  - Add `GET /cart/momo-return` endpoint:
    - MoMo redirects here after user completes payment
    - Query payment status
    - Show success or failure message
    - Clear cart if successful
  - Add `GET /cart/check-payment-status/{invoiceId}` endpoint (AJAX):
    - Returns JSON with payment status for polling

  **Must NOT do**:
  - Do not change showCart() or showCheckout() methods
  - Do not modify removeFromCart or updateCart endpoints

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Complex controller logic with multiple flows
  - **Skills**: None needed

  **Parallelization**:
  - **Can Run In Parallel**: YES (with Task 9)
  - **Parallel Group**: Wave 4
  - **Blocks**: Tasks 11, 12
  - **Blocked By**: Tasks 4, 7, 8 (needs Invoice, MoMoService, CartService updates)

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/CartController.java:93-117` - Current placeOrder method
  - `trantantai/src/main/java/trantantai/trantantai/controllers/CartController.java:76-91` - showCheckout pattern

  **Acceptance Criteria**:
  ```bash
  # Agent runs in trantantai directory:
  mvn compile -q
  # Assert: Exit code 0
  
  grep "momo-payment" src/main/java/trantantai/trantantai/controllers/CartController.java
  # Assert: Contains /cart/momo-payment endpoint
  
  grep "momo-return" src/main/java/trantantai/trantantai/controllers/CartController.java
  # Assert: Contains /cart/momo-return endpoint
  ```

  **Commit**: YES
  - Message: `feat(payment): update CartController with MoMo payment flow`
  - Files: `CartController.java`

---

### Task 11: Create MoMo Payment Page

- [ ] 11. Create momo-payment.html template

  **What to do**:
  - Create `momo-payment.html` in `templates/book/` directory
  - Display:
    - QR code image from qrCodeUrl
    - Payment amount
    - Order ID
    - Instructions for scanning QR
    - "Open MoMo App" button (deeplink)
    - "I've completed payment" button (redirects to momo-return)
  - Add auto-refresh/polling to check payment status every 5 seconds
  - Use existing layout (header, footer from layout.html)
  - Match existing checkout.html styling

  **Must NOT do**:
  - Do not use WebSocket (use polling instead)
  - Do not add new CSS files (use inline or existing styles)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: UI template with JavaScript polling
  - **Skills**: [`frontend-ui-ux`]
    - `frontend-ui-ux`: Match existing premium UI style from checkout.html

  **Parallelization**:
  - **Can Run In Parallel**: NO (needs Task 10 for endpoints)
  - **Parallel Group**: Wave 4 (after Task 10)
  - **Blocks**: Task 12
  - **Blocked By**: Task 10 (needs controller endpoints)

  **References**:
  - `trantantai/src/main/resources/templates/book/checkout.html` - UI styling pattern to follow
  - `trantantai/src/main/resources/templates/book/checkout.html:378-438` - MoMo section styling

  **File to create**:
  - `trantantai/src/main/resources/templates/book/momo-payment.html`

  **Acceptance Criteria**:

  **Using Playwright browser automation:**
  ```
  1. Start application: mvn spring-boot:run
  2. Navigate to: http://localhost:8080/login
  3. Login with test user credentials
  4. Add item to cart and go to checkout
  5. Select MOMO payment and submit
  6. Assert: Redirected to /cart/momo-payment
  7. Assert: QR code image is visible
  8. Assert: Payment amount is displayed
  9. Screenshot: .sisyphus/evidence/task-11-momo-payment-page.png
  ```

  **Commit**: YES
  - Message: `feat(payment): create MoMo QR payment page`
  - Files: `momo-payment.html`

---

### Task 12: Integration Testing and Verification

- [ ] 12. Integration testing and end-to-end verification

  **What to do**:
  - Verify complete MOMO payment flow:
    1. Start application
    2. Login as user
    3. Add book to cart
    4. Go to checkout
    5. Select MOMO payment
    6. Verify QR page displays
    7. (Optional) Test with MoMo sandbox if IPN URL is configured
  - Verify COD flow still works unchanged
  - Verify IPN endpoint is accessible
  - Document any issues found

  **Must NOT do**:
  - Do not deploy to production
  - Do not use real MoMo credentials

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: End-to-end verification with browser
  - **Skills**: [`playwright`]
    - `playwright`: Browser automation for full flow testing

  **Parallelization**:
  - **Can Run In Parallel**: NO (final task)
  - **Parallel Group**: Wave 5 (final)
  - **Blocks**: None
  - **Blocked By**: Tasks 9, 10, 11

  **References**:
  - All previously created files
  - MoMo sandbox testing: https://developers.momo.vn/v3/docs/payment/onboarding/test-instructions

  **Acceptance Criteria**:

  **Full Integration Verification (using Playwright):**
  ```
  # COD Flow:
  1. Navigate to http://localhost:8080
  2. Login with user credentials
  3. Add book to cart
  4. Go to checkout, select COD, submit
  5. Assert: Success message appears
  6. Assert: Cart is empty
  
  # MOMO Flow:
  1. Add another book to cart
  2. Go to checkout, select MOMO, submit
  3. Assert: Redirected to /cart/momo-payment
  4. Assert: QR code image visible (src contains qrCodeUrl)
  5. Assert: Amount matches cart total
  6. Screenshot: .sisyphus/evidence/task-12-final-verification.png
  ```

  **IPN Endpoint Test:**
  ```bash
  # Agent runs:
  curl -X POST http://localhost:8080/api/momo/ipn \
    -H "Content-Type: application/json" \
    -d '{"partnerCode":"test","orderId":"test","resultCode":0}' \
    -w "%{http_code}"
  # Assert: Returns 204 (or 400 for invalid signature - both indicate endpoint is working)
  ```

  **Commit**: NO (verification only)

---

## Commit Strategy

| After Task(s) | Message | Files | Verification |
|---------------|---------|-------|--------------|
| 1, 2, 3 | `feat(payment): add MoMo SDK dependency and configuration` | pom.xml, application.properties, PaymentStatus.java, PaymentMethod.java | mvn compile |
| 4 | `feat(payment): update Invoice entity with payment tracking fields` | Invoice.java | mvn compile |
| 5 | `feat(payment): add MoMo configuration class` | MoMoConfig.java | mvn compile |
| 6 | `feat(payment): configure security for MoMo IPN callback` | SecurityConfig.java | mvn compile |
| 7 | `feat(payment): implement MoMoService for payment API integration` | MoMoService.java | mvn compile |
| 8 | `feat(payment): update CartService with payment status handling` | CartService.java | mvn compile |
| 9 | `feat(payment): implement MoMo IPN callback controller` | MoMoController.java, MoMoIpnRequest.java | mvn compile |
| 10 | `feat(payment): update CartController with MoMo payment flow` | CartController.java | mvn compile |
| 11 | `feat(payment): create MoMo QR payment page` | momo-payment.html | mvn spring-boot:run |

---

## Success Criteria

### Verification Commands
```bash
# Compile check
cd trantantai && mvn compile -q
# Expected: Exit code 0

# Run application
cd trantantai && mvn spring-boot:run
# Expected: Application starts on port 8080

# IPN endpoint accessible
curl -X POST http://localhost:8080/api/momo/ipn -H "Content-Type: application/json" -d '{}' -w "%{http_code}"
# Expected: 204 or 400 (endpoint works)
```

### Final Checklist
- [ ] All "Must Have" present:
  - [x] MoMo SDK integrated
  - [x] QR code display on dedicated page
  - [x] IPN callback with signature verification
  - [x] Invoice status tracking
  - [x] COD flow unchanged
- [ ] All "Must NOT Have" absent:
  - [x] No automatic timeout
  - [x] No refund functionality
  - [x] No admin UI
  - [x] No extra payment methods
- [ ] Application compiles and runs
- [ ] Both COD and MOMO flows work
