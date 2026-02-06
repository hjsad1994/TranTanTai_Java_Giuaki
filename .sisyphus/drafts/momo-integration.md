# Draft: MoMo QR Payment Integration

## Requirements (from user)
- Integrate MoMo QR payment when user selects MOMO payment method
- Create MoMo payment request
- Display QR code for payment
- Handle IPN callback to verify payment
- Sync order status with MoMo

## Current Implementation Analysis

### Invoice Entity (Invoice.java)
- Fields: id, invoiceDate, price, itemInvoices (List<ItemInvoice>), userId
- **NO payment status field exists** - needs to be added
- **NO payment method field exists** - needs to be added
- **NO MoMo transaction ID field** - needs to be added

### CartController.java
- `showCheckout()` - displays checkout page with cart items
- `placeOrder()` - POST endpoint that:
  1. Validates cart is not empty
  2. Calls `cartService.saveCart(session)` immediately
  3. Shows success message and redirects to cart
- **Current flow saves invoice immediately regardless of payment method**
- **No differentiation between COD and MOMO flow**

### CartService.java
- `saveCart()` method:
  1. Validates stock availability
  2. Decrements stock atomically
  3. Creates Invoice with items
  4. Saves to MongoDB
  5. Clears cart
- **Problem**: Stock is decremented before payment confirmation for MOMO

### checkout.html
- Has COD and MOMO radio buttons
- MOMO section has placeholder for QR code
- Form submits to `/cart/place-order` with paymentMethod param

### SecurityConfig.java
- `/cart/**` requires ROLE_USER
- **IPN callback will need to be public** (MoMo server POST)

### pom.xml
- No MoMo SDK dependency yet
- Spring Boot 4.0.2, Java 25

## Research Findings (COMPLETE)

### MoMo SDK Integration Details
**Source**: https://github.com/momo-wallet/java

1. **Maven Dependency**:
   ```xml
   <dependency>
       <groupId>io.github.momo-wallet</groupId>
       <artifactId>momopayment</artifactId>
       <version>1.0</version>
   </dependency>
   ```

2. **Environment Configuration**:
   - `Environment.selectEnv("dev")` for sandbox
   - `Environment.selectEnv("prod")` for production
   - Requires: partnerCode, accessKey, secretKey

3. **Create Payment**:
   ```java
   PaymentResponse response = CreateOrderMoMo.process(
       environment, orderId, requestId, amount, orderInfo,
       returnURL, notifyURL, extraData, RequestType.CAPTURE_WALLET, true
   );
   // Returns: payUrl, qrCodeUrl, deeplink, transId
   ```

4. **IPN Callback**:
   - MoMo POST JSON with: partnerCode, orderId, amount, transId, resultCode, signature
   - Must verify HMAC-SHA256 signature using Encoder.signHmacSHA256()
   - Must return HTTP 204 No Content
   - resultCode=0 means success

5. **Query Status**:
   ```java
   QueryStatusTransactionResponse response = QueryTransactionStatus.process(
       environment, orderId, requestId
   );
   ```

### API Endpoints
| Environment | Base URL |
|-------------|----------|
| Sandbox | https://test-payment.momo.vn/v2/gateway/api |
| Production | https://payment.momo.vn/v2/gateway/api |

## Technical Decisions (Confirmed)

1. **Payment Status Model**: 
   - Enum: PENDING_PAYMENT, PAID, PAYMENT_FAILED, COD_PENDING
   
2. **Stock Handling for MOMO**:
   - Reserve stock (decrement) on order creation, mark as PENDING_PAYMENT
   - If payment fails/timeout → restore stock
   - Prevents overselling during payment window

3. **IPN Endpoint**: 
   - `/api/momo/ipn` - public access (permitAll in SecurityConfig)
   - CSRF disabled for this endpoint

## Open Questions (Need User Input)

1. Do you have MoMo sandbox credentials?
2. QR display - new page or modal?
3. Include payment timeout handling?
4. Public URL for IPN testing?

## Scope Boundaries

### INCLUDE
- MoMo SDK dependency in pom.xml
- MoMo config in application.properties
- MoMoService class for API calls
- MoMoController for IPN callback
- Invoice entity updates (paymentStatus, paymentMethod, momoTransactionId)
- PaymentStatus enum
- Modified CartController/CartService for MOMO flow
- QR code display page (momo-payment.html)
- SecurityConfig updates for IPN endpoint

### EXCLUDE
- Payment timeout/cancellation job (background scheduler)
- Admin UI for payment status
- Refund functionality
- Other payment methods
