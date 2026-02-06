package trantantai.trantantai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.constants.OrderStatus;
import trantantai.trantantai.constants.PaymentMethod;
import trantantai.trantantai.constants.PaymentStatus;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Category;
import trantantai.trantantai.entities.Invoice;
import trantantai.trantantai.entities.ItemInvoice;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.repositories.IBookRepository;
import trantantai.trantantai.repositories.ICategoryRepository;
import trantantai.trantantai.repositories.IInvoiceRepository;
import trantantai.trantantai.repositories.IUserRepository;
import trantantai.trantantai.services.ExcelExportService;
import trantantai.trantantai.services.PdfExportService;
import trantantai.trantantai.services.ReportService;
import trantantai.trantantai.viewmodels.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * REST API controller for admin reports.
 * All endpoints are protected by Spring Security (admin role required).
 */
@Tag(name = "Reports", description = "Admin report APIs - Revenue statistics, charts, and exports")
@RestController
@RequestMapping("/admin/api/reports")
@CrossOrigin(origins = "*")
public class ReportApiController {

    private final ReportService reportService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
    private final ICategoryRepository categoryRepository;
    private final IBookRepository bookRepository;
    private final IUserRepository userRepository;
    private final IInvoiceRepository invoiceRepository;

    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    public ReportApiController(ReportService reportService,
                               ExcelExportService excelExportService,
                               PdfExportService pdfExportService,
                               ICategoryRepository categoryRepository,
                               IBookRepository bookRepository,
                               IUserRepository userRepository,
                               IInvoiceRepository invoiceRepository) {
        this.reportService = reportService;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Operation(summary = "Seed mock data for reports", description = "Creates test data for reports testing")
    @ApiResponse(responseCode = "200", description = "Mock data created successfully")
    @PostMapping("/seed-data")
    public ResponseEntity<Map<String, Object>> seedMockData() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Create categories if needed
            List<Category> categories = categoryRepository.findAll();
            if (categories.size() < 6) {
                String[] categoryNames = {
                    "Công nghệ thông tin", "Kinh tế - Kinh doanh", "Văn học",
                    "Kỹ năng sống", "Thiếu nhi", "Khoa học"
                };
                categories = new ArrayList<>();
                for (String name : categoryNames) {
                    Category cat = new Category();
                    cat.setName(name);
                    categories.add(categoryRepository.save(cat));
                }
            }

            // Create books if needed
            List<Book> books = bookRepository.findAll();
            if (books.size() < 10) {
                Object[][] bookData = {
                    {"Clean Code", "Robert C. Martin", 350000.0, 0},
                    {"Design Patterns", "Gang of Four", 420000.0, 0},
                    {"Đắc Nhân Tâm", "Dale Carnegie", 150000.0, 1},
                    {"Cha Giàu Cha Nghèo", "Robert Kiyosaki", 180000.0, 1},
                    {"Nhà Giả Kim", "Paulo Coelho", 120000.0, 2},
                    {"Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", 98000.0, 2},
                    {"7 Thói Quen Hiệu Quả", "Stephen Covey", 195000.0, 3},
                    {"Dế Mèn Phiêu Lưu Ký", "Tô Hoài", 65000.0, 4},
                    {"Harry Potter Tập 1", "J.K. Rowling", 185000.0, 4},
                    {"Sapiens: Lược Sử Loài Người", "Yuval Noah Harari", 235000.0, 5}
                };
                books = new ArrayList<>();
                for (Object[] data : bookData) {
                    Book book = new Book();
                    book.setTitle((String) data[0]);
                    book.setAuthor((String) data[1]);
                    book.setPrice((Double) data[2]);
                    book.setQuantity(100);
                    int catIndex = (int) data[3];
                    if (catIndex < categories.size()) {
                        book.setCategoryId(categories.get(catIndex).getId());
                    }
                    books.add(bookRepository.save(book));
                }
            }

            // Get users
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                result.put("error", "No users found. Please create an admin user first.");
                return ResponseEntity.badRequest().body(result);
            }

            // Create invoices for last 12 months
            Random random = new Random(System.currentTimeMillis());
            Calendar cal = Calendar.getInstance();
            int invoicesCreated = 0;

            for (int monthsAgo = 11; monthsAgo >= 0; monthsAgo--) {
                cal.setTime(new Date());
                cal.add(Calendar.MONTH, -monthsAgo);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                int numOrders = 20 + (12 - monthsAgo) * 3 + random.nextInt(10);
                int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                for (int i = 0; i < numOrders; i++) {
                    cal.set(Calendar.DAY_OF_MONTH, 1 + random.nextInt(daysInMonth));
                    cal.set(Calendar.HOUR_OF_DAY, 8 + random.nextInt(12));
                    cal.set(Calendar.MINUTE, random.nextInt(60));

                    Invoice invoice = new Invoice();
                    invoice.setInvoiceDate(cal.getTime());
                    invoice.setUserId(users.get(random.nextInt(users.size())).getId());

                    int numItems = 1 + random.nextInt(4);
                    List<ItemInvoice> items = new ArrayList<>();
                    double totalPrice = 0;

                    for (int j = 0; j < numItems && j < books.size(); j++) {
                        Book book = books.get(random.nextInt(books.size()));
                        int quantity = 1 + random.nextInt(3);
                        ItemInvoice item = new ItemInvoice();
                        item.setId(UUID.randomUUID().toString());
                        item.setBookId(book.getId());
                        item.setQuantity(quantity);
                        items.add(item);
                        totalPrice += book.getPrice() * quantity;
                    }

                    invoice.setItemInvoices(items);
                    invoice.setPrice(totalPrice);

                    int statusRoll = random.nextInt(100);
                    if (statusRoll < 85) {
                        invoice.setOrderStatus(OrderStatus.DELIVERED);
                        invoice.setPaymentStatus(PaymentStatus.PAID);
                    } else if (statusRoll < 95) {
                        invoice.setOrderStatus(OrderStatus.PROCESSING);
                        invoice.setPaymentStatus(PaymentStatus.COD_PENDING);
                    } else {
                        invoice.setOrderStatus(OrderStatus.CANCELLED);
                        invoice.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
                    }

                    invoice.setPaymentMethod(random.nextInt(100) < 70 ? PaymentMethod.COD : PaymentMethod.MOMO);
                    invoiceRepository.save(invoice);
                    invoicesCreated++;
                }
            }

            result.put("success", true);
            result.put("categoriesCount", categoryRepository.count());
            result.put("booksCount", bookRepository.count());
            result.put("invoicesCreated", invoicesCreated);
            result.put("totalInvoices", invoiceRepository.count());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @Operation(summary = "Get report overview", description = "Retrieves overview statistics for the specified date range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved overview data")
    @GetMapping("/overview")
    public ResponseEntity<ReportOverviewVm> getOverview(
            @Parameter(description = "Date range: today, week, month, quarter, year, custom")
            @RequestParam(defaultValue = "month") String range,
            @Parameter(description = "Custom start date (required if range is custom)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Custom end date (required if range is custom)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Date[] dateRange = reportService.getDateRange(range, startDate, endDate);
        ReportOverviewVm overview = reportService.getOverview(range, dateRange[0], dateRange[1]);
        return ResponseEntity.ok(overview);
    }

    @Operation(summary = "Get revenue chart data", description = "Retrieves revenue data for Chart.js visualization")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved chart data")
    @GetMapping("/revenue-chart")
    public ResponseEntity<RevenueChartVm> getRevenueChart(
            @Parameter(description = "Date range: today, week, month, quarter, year, custom")
            @RequestParam(defaultValue = "month") String range,
            @Parameter(description = "Custom start date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Custom end date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Date[] dateRange = reportService.getDateRange(range, startDate, endDate);
        RevenueChartVm chartData = reportService.getRevenueChart(range, dateRange[0], dateRange[1]);
        return ResponseEntity.ok(chartData);
    }

    @Operation(summary = "Get category revenue distribution", description = "Retrieves revenue breakdown by category")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved category data")
    @GetMapping("/category-revenue")
    public ResponseEntity<List<CategoryRevenueVm>> getCategoryRevenue(
            @Parameter(description = "Date range: today, week, month, quarter, year, custom")
            @RequestParam(defaultValue = "month") String range,
            @Parameter(description = "Custom start date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Custom end date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Date[] dateRange = reportService.getDateRange(range, startDate, endDate);
        List<CategoryRevenueVm> categoryRevenue = reportService.getCategoryRevenue(dateRange[0], dateRange[1]);
        return ResponseEntity.ok(categoryRevenue);
    }

    @Operation(summary = "Get sales trend comparison", description = "Retrieves current vs previous period sales trend")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved trend data")
    @GetMapping("/sales-trend")
    public ResponseEntity<SalesTrendVm> getSalesTrend(
            @Parameter(description = "Date range: today, week, month, quarter, year, custom")
            @RequestParam(defaultValue = "month") String range,
            @Parameter(description = "Custom start date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Custom end date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Date[] dateRange = reportService.getDateRange(range, startDate, endDate);
        SalesTrendVm salesTrend = reportService.getSalesTrend(range, dateRange[0], dateRange[1]);
        return ResponseEntity.ok(salesTrend);
    }

    @Operation(summary = "Get top selling books", description = "Retrieves list of top selling books by quantity")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved top books")
    @GetMapping("/top-books")
    public ResponseEntity<List<BookSalesVm>> getTopSellingBooks(
            @Parameter(description = "Date range: today, week, month, quarter, year, custom")
            @RequestParam(defaultValue = "month") String range,
            @Parameter(description = "Number of books to return")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Custom start date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Custom end date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Date[] dateRange = reportService.getDateRange(range, startDate, endDate);
        List<BookSalesVm> topBooks = reportService.getTopSellingBooks(dateRange[0], dateRange[1], limit);
        return ResponseEntity.ok(topBooks);
    }

    @Operation(summary = "Get revenue table data", description = "Retrieves detailed revenue table with period grouping")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved table data")
    @GetMapping("/revenue-table")
    public ResponseEntity<List<RevenueTableRowVm>> getRevenueTable(
            @Parameter(description = "Group by: day, week, month")
            @RequestParam(defaultValue = "month") String groupBy,
            @Parameter(description = "Date range: today, week, month, quarter, year, custom")
            @RequestParam(defaultValue = "year") String range,
            @Parameter(description = "Custom start date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Custom end date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Date[] dateRange = reportService.getDateRange(range, startDate, endDate);
        List<RevenueTableRowVm> tableData = reportService.getRevenueTable(groupBy, dateRange[0], dateRange[1]);
        return ResponseEntity.ok(tableData);
    }

    @Operation(summary = "Get order status distribution", description = "Retrieves count of orders by status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved order status distribution")
    @GetMapping("/order-status")
    public ResponseEntity<java.util.Map<String, Long>> getOrderStatusDistribution(
            @Parameter(description = "Date range: today, week, month, quarter, year, custom")
            @RequestParam(defaultValue = "month") String range,
            @Parameter(description = "Custom start date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Custom end date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Date[] dateRange = reportService.getDateRange(range, startDate, endDate);
        java.util.Map<String, Long> distribution = reportService.getOrderStatusDistribution(dateRange[0], dateRange[1]);
        return ResponseEntity.ok(distribution);
    }

    @Operation(summary = "Export report to Excel", description = "Downloads the report as an Excel file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
            @ApiResponse(responseCode = "500", description = "Error generating Excel file")
    })
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @Parameter(description = "Date range: today, week, month, quarter, year, custom")
            @RequestParam(defaultValue = "month") String range,
            @Parameter(description = "Custom start date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Custom end date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        try {
            Date[] dateRange = reportService.getDateRange(range, startDate, endDate);

            // Gather report data
            ReportOverviewVm overview = reportService.getOverview(range, dateRange[0], dateRange[1]);
            List<BookSalesVm> topBooks = reportService.getTopSellingBooks(dateRange[0], dateRange[1], 10);
            List<RevenueTableRowVm> revenueTable = reportService.getRevenueTable("month", dateRange[0], dateRange[1]);
            List<CategoryRevenueVm> categoryRevenue = reportService.getCategoryRevenue(dateRange[0], dateRange[1]);

            // Generate Excel
            byte[] excelBytes = excelExportService.exportReport(overview, topBooks, revenueTable, categoryRevenue, range);

            // Build response
            String filename = "BookHaven_Report_" + FILE_DATE_FORMAT.format(new Date()) + ".xlsx";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Export report to PDF", description = "Downloads the report as a PDF file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF file generated successfully"),
            @ApiResponse(responseCode = "500", description = "Error generating PDF file")
    })
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @Parameter(description = "Date range: today, week, month, quarter, year, custom")
            @RequestParam(defaultValue = "month") String range,
            @Parameter(description = "Custom start date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "Custom end date")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        try {
            Date[] dateRange = reportService.getDateRange(range, startDate, endDate);

            // Gather report data
            ReportOverviewVm overview = reportService.getOverview(range, dateRange[0], dateRange[1]);
            List<BookSalesVm> topBooks = reportService.getTopSellingBooks(dateRange[0], dateRange[1], 10);
            List<RevenueTableRowVm> revenueTable = reportService.getRevenueTable("month", dateRange[0], dateRange[1]);
            List<CategoryRevenueVm> categoryRevenue = reportService.getCategoryRevenue(dateRange[0], dateRange[1]);

            // Generate PDF
            byte[] pdfBytes = pdfExportService.exportReport(overview, topBooks, revenueTable, categoryRevenue, range);

            // Build response
            String filename = "BookHaven_Report_" + FILE_DATE_FORMAT.format(new Date()) + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
