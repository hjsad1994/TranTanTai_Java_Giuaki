package trantantai.trantantai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.services.BookService;
import trantantai.trantantai.services.CategoryService;
import trantantai.trantantai.services.PdfExportService;
import trantantai.trantantai.services.PdfExportService.InventoryItemVm;
import trantantai.trantantai.services.PdfExportService.InventorySummaryVm;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API controller for inventory management.
 * Provides endpoints for inventory export and statistics.
 */
@Tag(name = "Inventory", description = "Inventory management APIs - Stock tracking and export")
@RestController
@RequestMapping("/admin/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryApiController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final PdfExportService pdfExportService;

    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    @Autowired
    public InventoryApiController(BookService bookService,
                                  CategoryService categoryService,
                                  PdfExportService pdfExportService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.pdfExportService = pdfExportService;
    }

    @Operation(summary = "Get inventory summary", description = "Retrieves overview statistics for inventory")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory summary")
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getInventorySummary() {
        List<Book> allBooks = bookService.getAllBooks(0, Integer.MAX_VALUE, "title");

        long totalProducts = allBooks.size();
        long totalStock = allBooks.stream()
                .mapToLong(b -> b.getQuantity() != null ? b.getQuantity() : 0)
                .sum();
        long inStockCount = allBooks.stream()
                .filter(b -> b.getQuantity() != null && b.getQuantity() > 5)
                .count();
        long lowStockCount = allBooks.stream()
                .filter(b -> b.getQuantity() != null && b.getQuantity() > 0 && b.getQuantity() <= 5)
                .count();
        long outOfStockCount = allBooks.stream()
                .filter(b -> b.getQuantity() == null || b.getQuantity() == 0)
                .count();

        int inStockPercent = totalProducts > 0 ? (int) ((inStockCount * 100) / totalProducts) : 0;

        Double totalValue = allBooks.stream()
                .mapToDouble(b -> (b.getPrice() != null ? b.getPrice() : 0) *
                                  (b.getQuantity() != null ? b.getQuantity() : 0))
                .sum();

        Double avgPrice = totalProducts > 0 ?
                allBooks.stream()
                        .mapToDouble(b -> b.getPrice() != null ? b.getPrice() : 0)
                        .average()
                        .orElse(0) : 0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", totalProducts);
        summary.put("totalStock", totalStock);
        summary.put("inStockCount", inStockCount);
        summary.put("lowStockCount", lowStockCount);
        summary.put("outOfStockCount", outOfStockCount);
        summary.put("inStockPercent", inStockPercent);
        summary.put("totalValue", totalValue);
        summary.put("avgPrice", avgPrice);

        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get low stock items", description = "Retrieves list of items with low or zero stock")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved low stock items")
    @GetMapping("/low-stock")
    public ResponseEntity<List<Map<String, Object>>> getLowStockItems() {
        List<Book> allBooks = bookService.getAllBooks(0, Integer.MAX_VALUE, "quantity");

        List<Map<String, Object>> lowStockItems = allBooks.stream()
                .filter(b -> b.getQuantity() == null || b.getQuantity() <= 5)
                .sorted(Comparator.comparingInt(b -> b.getQuantity() != null ? b.getQuantity() : 0))
                .map(book -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", book.getId());
                    item.put("title", book.getTitle());
                    item.put("author", book.getAuthor());
                    item.put("quantity", book.getQuantity());
                    item.put("price", book.getPrice());
                    item.put("categoryName", book.getCategory() != null ? book.getCategory().getName() : "Chưa phân loại");

                    // Priority level
                    int qty = book.getQuantity() != null ? book.getQuantity() : 0;
                    if (qty == 0) {
                        item.put("priority", "critical");
                        item.put("priorityLabel", "Khẩn cấp");
                    } else if (qty <= 2) {
                        item.put("priority", "high");
                        item.put("priorityLabel", "Cao");
                    } else {
                        item.put("priority", "medium");
                        item.put("priorityLabel", "Trung bình");
                    }

                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(lowStockItems);
    }

    @Operation(summary = "Export inventory report to PDF", description = "Downloads the inventory report as a PDF file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF file generated successfully"),
            @ApiResponse(responseCode = "500", description = "Error generating PDF file")
    })
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportInventoryPdf(
            @RequestParam(defaultValue = "all") String filter) {

        try {
            List<Book> allBooks = bookService.getAllBooks(0, Integer.MAX_VALUE, "title");

            // Apply filter if needed
            List<Book> filteredBooks = switch (filter.toLowerCase()) {
                case "instock" -> allBooks.stream()
                        .filter(b -> b.getQuantity() != null && b.getQuantity() > 5)
                        .collect(Collectors.toList());
                case "lowstock" -> allBooks.stream()
                        .filter(b -> b.getQuantity() != null && b.getQuantity() > 0 && b.getQuantity() <= 5)
                        .collect(Collectors.toList());
                case "outofstock" -> allBooks.stream()
                        .filter(b -> b.getQuantity() == null || b.getQuantity() == 0)
                        .collect(Collectors.toList());
                default -> allBooks;
            };

            // Convert to InventoryItemVm
            List<InventoryItemVm> items = filteredBooks.stream()
                    .map(book -> new InventoryItemVm(
                            book.getId(),
                            book.getTitle(),
                            book.getAuthor(),
                            book.getCategory() != null ? book.getCategory().getName() : null,
                            book.getPrice(),
                            book.getQuantity() != null ? book.getQuantity() : 0
                    ))
                    .collect(Collectors.toList());

            // Calculate summary from all books (not filtered)
            long totalProducts = allBooks.size();
            long totalStock = allBooks.stream()
                    .mapToLong(b -> b.getQuantity() != null ? b.getQuantity() : 0)
                    .sum();
            long inStockCount = allBooks.stream()
                    .filter(b -> b.getQuantity() != null && b.getQuantity() > 5)
                    .count();
            long lowStockCount = allBooks.stream()
                    .filter(b -> b.getQuantity() != null && b.getQuantity() > 0 && b.getQuantity() <= 5)
                    .count();
            long outOfStockCount = allBooks.stream()
                    .filter(b -> b.getQuantity() == null || b.getQuantity() == 0)
                    .count();
            int inStockPercent = totalProducts > 0 ? (int) ((inStockCount * 100) / totalProducts) : 0;

            Double totalValue = allBooks.stream()
                    .mapToDouble(b -> (b.getPrice() != null ? b.getPrice() : 0) *
                                      (b.getQuantity() != null ? b.getQuantity() : 0))
                    .sum();

            Double avgPrice = totalProducts > 0 ?
                    allBooks.stream()
                            .mapToDouble(b -> b.getPrice() != null ? b.getPrice() : 0)
                            .average()
                            .orElse(0) : 0;

            InventorySummaryVm summary = new InventorySummaryVm(
                    totalProducts,
                    totalStock,
                    inStockCount,
                    lowStockCount,
                    outOfStockCount,
                    inStockPercent,
                    totalValue,
                    avgPrice
            );

            // Generate PDF
            byte[] pdfBytes = pdfExportService.exportInventoryReport(items, summary);

            // Build response
            String filename = "BookHaven_Inventory_" + FILE_DATE_FORMAT.format(new Date()) + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
