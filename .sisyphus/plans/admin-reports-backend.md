# Admin Reports Backend Implementation

## TL;DR

> **Quick Summary**: Implement complete backend for admin reports page - ReportService for business logic, ReportApiController for 7 REST endpoints, 6 ViewModels, MongoDB aggregations, and Excel/PDF export functionality.
> 
> **Deliverables**:
> - ReportService with revenue calculations, top selling books, new customers metrics
> - ReportApiController with 7 API endpoints for charts, tables, exports
> - 6 ViewModel records (ReportOverviewVm, BookSalesVm, RevenueChartVm, CategoryRevenueVm, RevenueTableRowVm, SalesTrendVm)
> - ExcelExportService and PdfExportService for file exports
> - Updated AdminController.reports() with real data
> - Apache POI and OpenPDF dependencies in pom.xml
> 
> **Estimated Effort**: Large (8-12 hours)
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Task 1 (dependencies) → Task 2 (ViewModels) → Task 3 (ReportService) → Task 5 (Controller) → Task 7 (AdminController)

---

## Context

### Original Request
Implement complete backend for admin reports page at `/admin/reports`. The frontend (reports.html) already exists with Thymeleaf template expecting backend data. Need to create services, controllers, ViewModels, and export functionality.

### Interview Summary
**Key Discussions**:
- New customers = users with first order in selected period (User entity has no createdAt field)
- Cost = fixed 70% of revenue (no real cost data available)
- Revenue = only DELIVERED orders with PAID payment status
- Growth = compare to previous period of same length (week vs prev week)
- Manual verification via browser/curl (no test infrastructure)
- Exports include: overview stats + top selling books + revenue table

**Research Findings**:
- ViewModels use Java record pattern with `{Type}Vm` suffix and static `from()` factory methods
- Controllers use constructor injection, `@RestController` for APIs
- Services use `MongoTemplate` for complex aggregation operations
- Frontend expects specific JSON formats for Chart.js integration
- Date range filters: today, week, month, quarter, year, custom

### Key Files Identified
| File | Location | Purpose |
|------|----------|---------|
| pom.xml | `trantantai/pom.xml` | Add Apache POI + OpenPDF dependencies |
| AdminController | `trantantai/src/main/java/trantantai/trantantai/controllers/AdminController.java` | Update reports() method |
| IInvoiceRepository | `trantantai/src/main/java/trantantai/trantantai/repositories/IInvoiceRepository.java` | Reference for repository patterns |
| Invoice entity | `trantantai/src/main/java/trantantai/trantantai/entities/Invoice.java` | Data model reference |
| OrderService | `trantantai/src/main/java/trantantai/trantantai/services/OrderService.java` | Pattern reference |
| reports.html | `trantantai/src/main/resources/templates/admin/reports.html` | Frontend requirements reference |

---

## Work Objectives

### Core Objective
Create the complete backend infrastructure to power the admin reports page with real MongoDB data, including REST API endpoints for dynamic charts, export functionality, and proper date range filtering.

### Concrete Deliverables
1. `trantantai/src/main/java/trantantai/trantantai/viewmodels/ReportOverviewVm.java`
2. `trantantai/src/main/java/trantantai/trantantai/viewmodels/BookSalesVm.java`
3. `trantantai/src/main/java/trantantai/trantantai/viewmodels/RevenueChartVm.java`
4. `trantantai/src/main/java/trantantai/trantantai/viewmodels/CategoryRevenueVm.java`
5. `trantantai/src/main/java/trantantai/trantantai/viewmodels/RevenueTableRowVm.java`
6. `trantantai/src/main/java/trantantai/trantantai/viewmodels/SalesTrendVm.java`
7. `trantantai/src/main/java/trantantai/trantantai/services/ReportService.java`
8. `trantantai/src/main/java/trantantai/trantantai/services/ExcelExportService.java`
9. `trantantai/src/main/java/trantantai/trantantai/services/PdfExportService.java`
10. `trantantai/src/main/java/trantantai/trantantai/controllers/ReportApiController.java`
11. Updated `trantantai/pom.xml` with export dependencies
12. Updated `AdminController.java` reports() method

### Definition of Done
- [ ] `mvn clean compile` succeeds with no errors
- [ ] All 7 API endpoints return valid JSON when accessed via browser/curl
- [ ] Export endpoints return downloadable .xlsx and .pdf files
- [ ] AdminController.reports() displays real data on the reports page
- [ ] Revenue calculations use only DELIVERED + PAID orders
- [ ] Date range filtering works for all 6 range types

### Must Have
- All ViewModels as Java records following existing pattern
- ReportService with MongoTemplate aggregations
- All 7 REST API endpoints functional
- Excel export with Apache POI (XSSFWorkbook)
- PDF export with OpenPDF (PdfPTable)
- Date range support: today, week, month, quarter, year, custom

### Must NOT Have (Guardrails)
- **NO changes to User entity** (no adding createdAt field)
- **NO real cost data** - use fixed 70% cost ratio
- **NO unit tests** - manual verification only
- **NO frontend changes** - reports.html stays unchanged
- **NO modification to existing entities** - use aggregations only
- **NO complex caching** - direct MongoDB queries
- **NO revenue from CANCELLED orders** - only DELIVERED + PAID
- **NO additional dependencies** beyond Apache POI + OpenPDF

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO
- **User wants tests**: Manual verification only
- **Framework**: None - manual testing with curl/browser

### Automated Verification Approach

Each TODO includes executable verification commands the agent can run directly:

**For API Endpoints**: curl commands with expected JSON response validation
**For Build Success**: `mvn clean compile` with exit code check
**For Exports**: Download file and verify content-type header
**For Page Rendering**: Browser navigation to `/admin/reports` via Playwright

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: Add dependencies to pom.xml [no dependencies]
└── Task 2: Create all 6 ViewModels [no dependencies]

Wave 2 (After Wave 1):
├── Task 3: Create ReportService [depends: 2]
├── Task 4: Create ExcelExportService [depends: 1]
└── Task 4b: Create PdfExportService [depends: 1]

Wave 3 (After Wave 2):
├── Task 5: Create ReportApiController [depends: 3, 4, 4b]
└── Task 6: Update AdminController.reports() [depends: 3]

Wave 4 (After Wave 3):
└── Task 7: Integration verification [depends: 5, 6]

Critical Path: Task 1 → Task 4 → Task 5 → Task 7
              Task 2 → Task 3 → Task 5 → Task 7
Parallel Speedup: ~40% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 (pom.xml) | None | 4, 4b | 2 |
| 2 (ViewModels) | None | 3, 5 | 1 |
| 3 (ReportService) | 2 | 5, 6 | 4, 4b |
| 4 (ExcelExport) | 1 | 5 | 3, 4b |
| 4b (PdfExport) | 1 | 5 | 3, 4 |
| 5 (ApiController) | 3, 4, 4b | 7 | 6 |
| 6 (AdminController) | 3 | 7 | 5 |
| 7 (Verification) | 5, 6 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Approach |
|------|-------|---------------------|
| 1 | 1, 2 | Run in parallel - completely independent |
| 2 | 3, 4, 4b | Run in parallel after Wave 1 |
| 3 | 5, 6 | Run in parallel after Wave 2 |
| 4 | 7 | Sequential final verification |

---

## TODOs

### Wave 1: Foundation (No Dependencies)

---

- [ ] 1. Add Apache POI and OpenPDF dependencies to pom.xml

  **What to do**:
  - Open `trantantai/pom.xml`
  - Add Apache POI dependency for Excel export (poi-ooxml 5.5.1)
  - Add OpenPDF dependency for PDF export (openpdf 2.0.3)
  - Add dependencies BEFORE the closing `</dependencies>` tag
  - Run `mvn clean compile` to verify dependencies resolve

  **Dependencies to add**:
  ```xml
  <!-- Apache POI for Excel Export -->
  <dependency>
      <groupId>org.apache.poi</groupId>
      <artifactId>poi-ooxml</artifactId>
      <version>5.5.1</version>
  </dependency>
  
  <!-- OpenPDF for PDF Export -->
  <dependency>
      <groupId>com.github.librepdf</groupId>
      <artifactId>openpdf</artifactId>
      <version>2.0.3</version>
  </dependency>
  ```

  **Must NOT do**:
  - Do NOT change existing dependencies
  - Do NOT modify Spring Boot version
  - Do NOT add any other dependencies

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple file edit with known content, no complex logic
  - **Skills**: None required
    - This is a straightforward XML edit
  - **Skills Evaluated but Omitted**:
    - `git-master`: Not needed - no commit in this task

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Tasks 4, 4b
  - **Blocked By**: None (can start immediately)

  **References**:
  - `trantantai/pom.xml:32-105` - Existing dependencies section, add new deps before line 105 `</dependencies>`
  - Apache POI Maven: `org.apache.poi:poi-ooxml:5.5.1`
  - OpenPDF Maven: `com.github.librepdf:openpdf:2.0.3`

  **Acceptance Criteria**:
  ```bash
  # Agent runs from trantantai directory:
  cd trantantai && mvn dependency:resolve -q
  # Assert: Exit code 0
  
  # Verify POI resolved:
  mvn dependency:tree | grep "poi-ooxml"
  # Assert: Output contains "poi-ooxml:jar:5.5.1"
  
  # Verify OpenPDF resolved:
  mvn dependency:tree | grep "openpdf"
  # Assert: Output contains "openpdf:jar:2.0.3"
  ```

  **Commit**: YES (groups with 2)
  - Message: `feat(reports): add Apache POI and OpenPDF dependencies for export`
  - Files: `pom.xml`
  - Pre-commit: `mvn clean compile -q`

---

- [ ] 2. Create all 6 ViewModel records

  **What to do**:
  - Create 6 Java record files in `trantantai/src/main/java/trantantai/trantantai/viewmodels/`
  - Follow existing ViewModel pattern (see BookGetVm.java for reference)
  - Add `@Schema` annotations for Swagger documentation
  - All ViewModels should be public records

  **ViewModels to create**:

  **2a. ReportOverviewVm.java**:
  ```java
  public record ReportOverviewVm(
      Double totalRevenue,
      Long totalOrders,
      Double avgOrderValue,
      Integer newCustomers,
      Double revenueGrowth,      // % change from previous period
      Double ordersGrowth,       // % change from previous period
      Double avgValueGrowth,     // % change from previous period
      Double customersGrowth     // % change from previous period
  ) {}
  ```

  **2b. BookSalesVm.java**:
  ```java
  public record BookSalesVm(
      String bookId,
      String title,
      String author,
      Integer soldCount,
      Double revenue,
      String imageUrl           // First image from imageUrls list
  ) {}
  ```

  **2c. RevenueChartVm.java**:
  ```java
  public record RevenueChartVm(
      List<String> labels,      // ["T1", "T2", ...] or ["01/01", "02/01", ...]
      List<Double> data         // Revenue values
  ) {}
  ```

  **2d. CategoryRevenueVm.java**:
  ```java
  public record CategoryRevenueVm(
      String categoryId,
      String categoryName,
      Double revenue,
      Double percentage         // Share of total revenue
  ) {}
  ```

  **2e. RevenueTableRowVm.java**:
  ```java
  public record RevenueTableRowVm(
      String period,            // "2024-01-15" or "2024-W03" or "2024-01"
      String periodLabel,       // "15/01/2024" or "Tuần 3" or "Tháng 1"
      Long orderCount,
      Double revenue,
      Double cost,              // revenue * 0.7
      Double profit,            // revenue * 0.3
      Double growthPercent      // vs previous period
  ) {}
  ```

  **2f. SalesTrendVm.java**:
  ```java
  public record SalesTrendVm(
      List<String> labels,
      List<Double> currentPeriod,
      List<Double> previousPeriod
  ) {}
  ```

  **Must NOT do**:
  - Do NOT add constructors (records have implicit ones)
  - Do NOT add setters (records are immutable)
  - Do NOT deviate from record pattern
  - Do NOT add static from() methods yet (not needed for these DTOs)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Boilerplate Java records with clear structure, no complex logic
  - **Skills**: None required
    - Straightforward Java file creation
  - **Skills Evaluated but Omitted**:
    - `git-master`: Not needed - no commit in this task

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Tasks 3, 5
  - **Blocked By**: None (can start immediately)

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/viewmodels/BookGetVm.java` - ViewModel record pattern with @Schema annotations
  - `trantantai/src/main/java/trantantai/trantantai/viewmodels/ReviewStatisticsVm.java` - Statistics ViewModel pattern
  - `trantantai/src/main/resources/templates/admin/reports.html:234-247` - FE expects: title, author, soldCount, revenue for topSellingBooks

  **Acceptance Criteria**:
  ```bash
  # Agent runs from trantantai directory:
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify all files exist:
  ls src/main/java/trantantai/trantantai/viewmodels/ReportOverviewVm.java
  ls src/main/java/trantantai/trantantai/viewmodels/BookSalesVm.java
  ls src/main/java/trantantai/trantantai/viewmodels/RevenueChartVm.java
  ls src/main/java/trantantai/trantantai/viewmodels/CategoryRevenueVm.java
  ls src/main/java/trantantai/trantantai/viewmodels/RevenueTableRowVm.java
  ls src/main/java/trantantai/trantantai/viewmodels/SalesTrendVm.java
  # Assert: All 6 files exist (exit code 0 for each)
  ```

  **Commit**: YES (groups with 1)
  - Message: `feat(reports): add ViewModel records for report data`
  - Files: `src/main/java/trantantai/trantantai/viewmodels/*.java` (6 files)
  - Pre-commit: `mvn compile -q`

---

### Wave 2: Services (After Wave 1)

---

- [ ] 3. Create ReportService with MongoDB aggregations

  **What to do**:
  - Create `trantantai/src/main/java/trantantai/trantantai/services/ReportService.java`
  - Implement all business logic methods using MongoTemplate aggregations
  - Follow existing service patterns (constructor injection, @Service annotation)
  - Revenue calculation: only DELIVERED orders with PAID status
  - Cost calculation: revenue × 0.7, profit: revenue × 0.3
  - New customers: users with first order in the date range

  **Methods to implement**:

  ```java
  @Service
  public class ReportService {
      private final MongoTemplate mongoTemplate;
      private final CategoryService categoryService;
      private final BookService bookService;
      
      // Constructor injection
      
      // Get date range from range string (today, week, month, quarter, year, custom)
      public Date[] getDateRange(String range, Date customStart, Date customEnd);
      
      // Main overview data
      public ReportOverviewVm getOverview(String range, Date startDate, Date endDate);
      
      // Total revenue from DELIVERED + PAID orders
      public Double calculateTotalRevenue(Date startDate, Date endDate);
      
      // Count orders in range
      public Long countOrders(Date startDate, Date endDate);
      
      // Average order value
      public Double calculateAvgOrderValue(Date startDate, Date endDate);
      
      // Count new customers (users with first order in range)
      public Integer countNewCustomers(Date startDate, Date endDate);
      
      // Calculate growth vs previous period
      public Double calculateGrowth(Double current, Double previous);
      
      // Revenue chart data (by day/week/month depending on range)
      public RevenueChartVm getRevenueChart(String range, Date startDate, Date endDate);
      
      // Category revenue distribution
      public List<CategoryRevenueVm> getCategoryRevenue(Date startDate, Date endDate);
      
      // Sales trend (current vs previous period)
      public SalesTrendVm getSalesTrend(String range, Date startDate, Date endDate);
      
      // Top selling books
      public List<BookSalesVm> getTopSellingBooks(Date startDate, Date endDate, int limit);
      
      // Revenue table with pagination
      public List<RevenueTableRowVm> getRevenueTable(String groupBy, Date startDate, Date endDate);
  }
  ```

  **MongoDB Aggregation patterns to use**:
  ```java
  // Revenue by period example
  Aggregation aggregation = Aggregation.newAggregation(
      Aggregation.match(Criteria.where("orderStatus").is(OrderStatus.DELIVERED)
          .and("paymentStatus").is(PaymentStatus.PAID)
          .and("invoiceDate").gte(startDate).lte(endDate)),
      Aggregation.group(/* date grouping */).sum("price").as("revenue").count().as("orderCount"),
      Aggregation.sort(Sort.Direction.ASC, "_id")
  );
  ```

  **Must NOT do**:
  - Do NOT include CANCELLED orders in revenue
  - Do NOT include non-PAID orders in revenue
  - Do NOT use real cost data (use 70% estimate)
  - Do NOT add caching logic
  - Do NOT modify IInvoiceRepository

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
    - Reason: Complex MongoDB aggregations, date math, business logic
  - **Skills**: None required
    - Standard Spring/MongoDB patterns
  - **Skills Evaluated but Omitted**:
    - `git-master`: Not needed - no commit in this task

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 4b)
  - **Blocks**: Tasks 5, 6
  - **Blocked By**: Task 2 (ViewModels)

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/services/OrderService.java` - Service pattern with MongoTemplate, constructor injection, getOrderStatistics() method
  - `trantantai/src/main/java/trantantai/trantantai/services/BookService.java` - MongoTemplate atomic operations pattern
  - `trantantai/src/main/java/trantantai/trantantai/entities/Invoice.java` - Invoice fields: id, invoiceDate, price, itemInvoices, userId, paymentStatus, orderStatus
  - `trantantai/src/main/java/trantantai/trantantai/constants/OrderStatus.java` - DELIVERED enum value
  - `trantantai/src/main/java/trantantai/trantantai/constants/PaymentStatus.java` - PAID enum value
  - `trantantai/src/main/java/trantantai/trantantai/entities/ItemInvoice.java` - Embedded doc with bookId, quantity

  **Acceptance Criteria**:
  ```bash
  # Agent runs from trantantai directory:
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify file exists and has key methods:
  grep -c "getOverview" src/main/java/trantantai/trantantai/services/ReportService.java
  # Assert: Output >= 1
  
  grep -c "calculateTotalRevenue" src/main/java/trantantai/trantantai/services/ReportService.java
  # Assert: Output >= 1
  
  grep -c "getTopSellingBooks" src/main/java/trantantai/trantantai/services/ReportService.java
  # Assert: Output >= 1
  
  grep -c "MongoTemplate" src/main/java/trantantai/trantantai/services/ReportService.java
  # Assert: Output >= 1
  
  # Verify revenue filter
  grep "DELIVERED" src/main/java/trantantai/trantantai/services/ReportService.java
  # Assert: Output contains DELIVERED
  
  grep "PAID" src/main/java/trantantai/trantantai/services/ReportService.java
  # Assert: Output contains PAID
  ```

  **Commit**: NO (groups with Task 5)

---

- [ ] 4. Create ExcelExportService

  **What to do**:
  - Create `trantantai/src/main/java/trantantai/trantantai/services/ExcelExportService.java`
  - Use Apache POI XSSFWorkbook for .xlsx files
  - Export full report: Overview sheet + Top Books sheet + Revenue Table sheet
  - Add cell styling (headers bold, alternating row colors)

  **Methods to implement**:
  ```java
  @Service
  public class ExcelExportService {
      
      public byte[] exportReport(
          ReportOverviewVm overview,
          List<BookSalesVm> topBooks,
          List<RevenueTableRowVm> revenueTable,
          String dateRange
      ) throws IOException;
      
      private void createOverviewSheet(XSSFWorkbook workbook, ReportOverviewVm overview, String dateRange);
      private void createTopBooksSheet(XSSFWorkbook workbook, List<BookSalesVm> books);
      private void createRevenueTableSheet(XSSFWorkbook workbook, List<RevenueTableRowVm> rows);
      private CellStyle createHeaderStyle(XSSFWorkbook workbook);
      private CellStyle createDataStyle(XSSFWorkbook workbook);
  }
  ```

  **Must NOT do**:
  - Do NOT use SXSSFWorkbook (data is small enough for XSSFWorkbook)
  - Do NOT include chart images in Excel
  - Do NOT add custom formulas

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard Apache POI patterns, well-documented library
  - **Skills**: None required
    - POI is well-documented, patterns clear from librarian research
  - **Skills Evaluated but Omitted**:
    - `git-master`: Not needed - no commit in this task

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 4b)
  - **Blocks**: Task 5
  - **Blocked By**: Task 1 (dependencies)

  **References**:
  - Apache POI XSSFWorkbook pattern from librarian research
  - `trantantai/src/main/java/trantantai/trantantai/services/OrderService.java` - Service pattern
  - POI imports: `org.apache.poi.xssf.usermodel.*`, `org.apache.poi.ss.usermodel.*`

  **Acceptance Criteria**:
  ```bash
  # Agent runs from trantantai directory:
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify key imports:
  grep "XSSFWorkbook" src/main/java/trantantai/trantantai/services/ExcelExportService.java
  # Assert: Output contains XSSFWorkbook
  
  grep "exportReport" src/main/java/trantantai/trantantai/services/ExcelExportService.java
  # Assert: Output >= 1
  ```

  **Commit**: NO (groups with Task 5)

---

- [ ] 4b. Create PdfExportService

  **What to do**:
  - Create `trantantai/src/main/java/trantantai/trantantai/services/PdfExportService.java`
  - Use OpenPDF (com.lowagie.text) for PDF generation
  - Export full report: Title, Overview section, Top Books table, Revenue table
  - Add Vietnamese locale support for number formatting

  **Methods to implement**:
  ```java
  @Service
  public class PdfExportService {
      
      public byte[] exportReport(
          ReportOverviewVm overview,
          List<BookSalesVm> topBooks,
          List<RevenueTableRowVm> revenueTable,
          String dateRange
      ) throws DocumentException, IOException;
      
      private void addTitle(Document document, String dateRange) throws DocumentException;
      private void addOverviewSection(Document document, ReportOverviewVm overview) throws DocumentException;
      private PdfPTable createTopBooksTable(List<BookSalesVm> books);
      private PdfPTable createRevenueTable(List<RevenueTableRowVm> rows);
  }
  ```

  **Must NOT do**:
  - Do NOT use iText (use OpenPDF which is LGPL licensed)
  - Do NOT add custom fonts (use default Helvetica)
  - Do NOT embed images

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard OpenPDF patterns, well-documented
  - **Skills**: None required
    - OpenPDF patterns clear from librarian research
  - **Skills Evaluated but Omitted**:
    - `git-master`: Not needed - no commit in this task

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 4)
  - **Blocks**: Task 5
  - **Blocked By**: Task 1 (dependencies)

  **References**:
  - OpenPDF pattern from librarian research (com.lowagie.text.*)
  - `trantantai/src/main/java/trantantai/trantantai/services/OrderService.java` - Service pattern
  - OpenPDF imports: `com.lowagie.text.*`, `com.lowagie.text.pdf.*`

  **Acceptance Criteria**:
  ```bash
  # Agent runs from trantantai directory:
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify key imports:
  grep "com.lowagie" src/main/java/trantantai/trantantai/services/PdfExportService.java
  # Assert: Output contains com.lowagie
  
  grep "PdfPTable" src/main/java/trantantai/trantantai/services/PdfExportService.java
  # Assert: Output contains PdfPTable
  ```

  **Commit**: NO (groups with Task 5)

---

### Wave 3: Controllers (After Wave 2)

---

- [ ] 5. Create ReportApiController with 7 REST endpoints

  **What to do**:
  - Create `trantantai/src/main/java/trantantai/trantantai/controllers/ReportApiController.java`
  - Implement all 7 REST API endpoints
  - Follow existing REST controller patterns (@RestController, @RequestMapping, ResponseEntity)
  - Add Swagger annotations (@Tag, @Operation)
  - Handle date range parameter parsing

  **Endpoints to implement**:
  ```java
  @Tag(name = "Reports", description = "Admin report APIs")
  @RestController
  @RequestMapping("/admin/api/reports")
  public class ReportApiController {
      
      private final ReportService reportService;
      private final ExcelExportService excelExportService;
      private final PdfExportService pdfExportService;
      
      // Constructor injection
      
      // GET /admin/api/reports/overview?range=today|week|month|quarter|year|custom&startDate=&endDate=
      @GetMapping("/overview")
      public ResponseEntity<ReportOverviewVm> getOverview(
          @RequestParam(defaultValue = "month") String range,
          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate);
      
      // GET /admin/api/reports/revenue-chart?range=...
      @GetMapping("/revenue-chart")
      public ResponseEntity<RevenueChartVm> getRevenueChart(...);
      
      // GET /admin/api/reports/category-revenue?range=...
      @GetMapping("/category-revenue")
      public ResponseEntity<List<CategoryRevenueVm>> getCategoryRevenue(...);
      
      // GET /admin/api/reports/sales-trend?range=...
      @GetMapping("/sales-trend")
      public ResponseEntity<SalesTrendVm> getSalesTrend(...);
      
      // GET /admin/api/reports/top-books?range=...&limit=10
      @GetMapping("/top-books")
      public ResponseEntity<List<BookSalesVm>> getTopSellingBooks(...);
      
      // GET /admin/api/reports/revenue-table?groupBy=day|week|month&startDate=&endDate=
      @GetMapping("/revenue-table")
      public ResponseEntity<List<RevenueTableRowVm>> getRevenueTable(...);
      
      // GET /admin/api/reports/export/excel?range=...
      @GetMapping("/export/excel")
      public ResponseEntity<byte[]> exportExcel(...);
      
      // GET /admin/api/reports/export/pdf?range=...
      @GetMapping("/export/pdf")
      public ResponseEntity<byte[]> exportPdf(...);
  }
  ```

  **Export Response Headers**:
  ```java
  // Excel
  headers.setContentType(MediaType.parseMediaType(
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
  headers.setContentDispositionFormData("attachment", "report_" + date + ".xlsx");
  
  // PDF
  headers.setContentType(MediaType.APPLICATION_PDF);
  headers.setContentDispositionFormData("attachment", "report_" + date + ".pdf");
  ```

  **Must NOT do**:
  - Do NOT add authentication (already handled by Spring Security)
  - Do NOT add pagination to chart endpoints
  - Do NOT return raw exceptions (use proper error handling)

  **Recommended Agent Profile**:
  - **Category**: `ultrabrain`
    - Reason: Multiple endpoints, date parsing, proper ResponseEntity handling
  - **Skills**: None required
    - Standard Spring REST patterns
  - **Skills Evaluated but Omitted**:
    - `git-master`: Not needed - commit handled separately

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 6)
  - **Blocks**: Task 7
  - **Blocked By**: Tasks 3, 4, 4b

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/BookApiController.java` - REST controller pattern with @Tag, @Operation, ResponseEntity
  - `trantantai/src/main/resources/templates/admin/reports.html` - Frontend expects these exact endpoints and JSON formats
  - Export content-type headers from librarian research

  **Acceptance Criteria**:
  ```bash
  # Agent runs from trantantai directory:
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify all endpoints exist:
  grep -c "@GetMapping" src/main/java/trantantai/trantantai/controllers/ReportApiController.java
  # Assert: Output >= 8 (7 endpoints + class-level)
  
  grep "overview" src/main/java/trantantai/trantantai/controllers/ReportApiController.java
  grep "revenue-chart" src/main/java/trantantai/trantantai/controllers/ReportApiController.java
  grep "export/excel" src/main/java/trantantai/trantantai/controllers/ReportApiController.java
  grep "export/pdf" src/main/java/trantantai/trantantai/controllers/ReportApiController.java
  # Assert: All grep commands return matches
  ```

  **Commit**: YES
  - Message: `feat(reports): add ReportService, export services, and API controller`
  - Files: `ReportService.java`, `ExcelExportService.java`, `PdfExportService.java`, `ReportApiController.java`
  - Pre-commit: `mvn compile -q`

---

- [ ] 6. Update AdminController.reports() to use real data

  **What to do**:
  - Modify `trantantai/src/main/java/trantantai/trantantai/controllers/AdminController.java`
  - Inject ReportService into constructor
  - Update reports() method to call ReportService and pass real data to model
  - Use default "month" range for initial page load

  **Changes needed**:
  ```java
  // Add to constructor:
  private final ReportService reportService;
  
  public AdminController(BookService bookService, CategoryService categoryService, 
                         OrderService orderService, ReportService reportService) {
      // ... add reportService
  }
  
  // Update reports() method (lines 315-337):
  @GetMapping("/reports")
  public String reports(Model model) {
      model.addAttribute("pageTitle", "Báo cáo & Thống kê");
      model.addAttribute("activeMenu", "reports");
      
      // Get date range for default "month" view
      Date[] dateRange = reportService.getDateRange("month", null, null);
      
      // Overview data
      ReportOverviewVm overview = reportService.getOverview("month", dateRange[0], dateRange[1]);
      model.addAttribute("totalRevenue", overview.totalRevenue());
      model.addAttribute("totalOrders", overview.totalOrders());
      model.addAttribute("avgOrderValue", overview.avgOrderValue());
      model.addAttribute("newCustomers", overview.newCustomers());
      
      // Top selling books
      List<BookSalesVm> topBooks = reportService.getTopSellingBooks(dateRange[0], dateRange[1], 10);
      model.addAttribute("topSellingBooks", topBooks);
      
      // Order statistics (keep existing)
      Map<String, Long> orderStats = orderService.getOrderStatistics();
      model.addAttribute("orderStats", orderStats);
      
      // Basic stats
      model.addAttribute("totalBooks", bookService.getTotalBooks());
      model.addAttribute("totalCategories", categoryService.getAllCategories().size());
      
      return "admin/reports";
  }
  ```

  **Must NOT do**:
  - Do NOT remove existing orderStats logic
  - Do NOT change the return view name
  - Do NOT add new endpoints
  - Do NOT modify other methods in AdminController

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple modification to existing controller
  - **Skills**: None required
    - Standard Spring MVC patterns
  - **Skills Evaluated but Omitted**:
    - `git-master`: Commit handled at end

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 5)
  - **Blocks**: Task 7
  - **Blocked By**: Task 3 (ReportService)

  **References**:
  - `trantantai/src/main/java/trantantai/trantantai/controllers/AdminController.java:315-337` - Current reports() method with TODO comments
  - `trantantai/src/main/java/trantantai/trantantai/controllers/AdminController.java:31-35` - Constructor injection pattern
  - `trantantai/src/main/resources/templates/admin/reports.html:98-163` - Model attributes expected: totalRevenue, totalOrders, avgOrderValue, newCustomers
  - `trantantai/src/main/resources/templates/admin/reports.html:234-247` - topSellingBooks iteration

  **Acceptance Criteria**:
  ```bash
  # Agent runs from trantantai directory:
  cd trantantai && mvn compile -q
  # Assert: Exit code 0
  
  # Verify ReportService injection:
  grep "ReportService" src/main/java/trantantai/trantantai/controllers/AdminController.java
  # Assert: Output contains ReportService
  
  # Verify real data calls:
  grep "getOverview" src/main/java/trantantai/trantantai/controllers/AdminController.java
  grep "getTopSellingBooks" src/main/java/trantantai/trantantai/controllers/AdminController.java
  # Assert: Both grep commands return matches
  ```

  **Commit**: YES
  - Message: `feat(reports): wire AdminController.reports() to use real report data`
  - Files: `AdminController.java`
  - Pre-commit: `mvn compile -q`

---

### Wave 4: Integration Verification (Final)

---

- [ ] 7. Integration verification and final testing

  **What to do**:
  - Run full Maven build
  - Start the application
  - Test all API endpoints via curl
  - Test export downloads
  - Navigate to /admin/reports page
  - Verify data displays correctly

  **Verification steps**:
  1. Full build: `mvn clean package -DskipTests`
  2. Start app: `mvn spring-boot:run` (in background)
  3. Wait for startup (check for "Started TrantantaiApplication")
  4. Test each API endpoint with curl
  5. Test export endpoints (verify file downloads)
  6. Use Playwright to navigate to /admin/reports and take screenshot

  **Must NOT do**:
  - Do NOT modify any code in this task
  - Do NOT write unit tests
  - Do NOT commit anything

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Need Playwright for browser verification
  - **Skills**: [`playwright`]
    - `playwright`: Required for browser navigation and screenshot capture
  - **Skills Evaluated but Omitted**:
    - `git-master`: No commits in verification task

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: None (final sequential task)
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 5, 6

  **References**:
  - All created files from previous tasks
  - Expected API responses from reports.html JavaScript

  **Acceptance Criteria**:

  **Build Verification**:
  ```bash
  cd trantantai && mvn clean package -DskipTests
  # Assert: Exit code 0
  # Assert: Output contains "BUILD SUCCESS"
  ```

  **API Verification** (run after starting app):
  ```bash
  # Overview endpoint
  curl -s "http://localhost:8080/admin/api/reports/overview?range=month" | head -c 200
  # Assert: Response contains "totalRevenue"
  
  # Revenue chart endpoint
  curl -s "http://localhost:8080/admin/api/reports/revenue-chart?range=month" | head -c 200
  # Assert: Response contains "labels"
  
  # Top books endpoint
  curl -s "http://localhost:8080/admin/api/reports/top-books?range=month&limit=5" | head -c 200
  # Assert: Response is JSON array
  
  # Excel export (check headers)
  curl -sI "http://localhost:8080/admin/api/reports/export/excel?range=month" | grep "Content-Type"
  # Assert: Contains "spreadsheetml.sheet"
  
  # PDF export (check headers)
  curl -sI "http://localhost:8080/admin/api/reports/export/pdf?range=month" | grep "Content-Type"
  # Assert: Contains "application/pdf"
  ```

  **Browser Verification** (using Playwright):
  ```
  1. Navigate to: http://localhost:8080/admin/reports
  2. Wait for: selector ".stat-card" to be visible
  3. Assert: Page contains text "Tổng doanh thu"
  4. Assert: Page contains text "Tổng đơn hàng"
  5. Screenshot: .sisyphus/evidence/task-7-reports-page.png
  ```

  **Evidence to Capture**:
  - [ ] Terminal output from `mvn clean package`
  - [ ] curl responses from all API endpoints
  - [ ] Screenshot of /admin/reports page

  **Commit**: NO (verification only)

---

## Commit Strategy

| After Task(s) | Message | Files | Verification |
|---------------|---------|-------|--------------|
| 1, 2 | `feat(reports): add export dependencies and ViewModel records` | pom.xml, 6 ViewModel files | mvn compile |
| 3, 4, 4b, 5 | `feat(reports): add ReportService, export services, and API controller` | 4 service/controller files | mvn compile |
| 6 | `feat(reports): wire AdminController.reports() to use real report data` | AdminController.java | mvn compile |

---

## Success Criteria

### Verification Commands
```bash
# Build succeeds
cd trantantai && mvn clean compile
# Expected: BUILD SUCCESS, exit code 0

# All 12 files created
find src -name "*.java" | grep -E "(Report|Excel|Pdf)" | wc -l
# Expected: 12+ (6 ViewModels + 3 services + 1 controller + AdminController edit)

# API responds with data
curl -s http://localhost:8080/admin/api/reports/overview?range=month
# Expected: JSON with totalRevenue, totalOrders, avgOrderValue, newCustomers

# Exports work
curl -sI http://localhost:8080/admin/api/reports/export/excel?range=month | grep -i content-type
# Expected: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

### Final Checklist
- [ ] All "Must Have" features implemented
- [ ] All "Must NOT Have" guardrails respected
- [ ] Build compiles without errors
- [ ] All 7 API endpoints return valid responses
- [ ] Excel and PDF exports download correctly
- [ ] /admin/reports page shows real data
