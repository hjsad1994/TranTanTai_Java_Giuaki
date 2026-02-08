package trantantai.trantantai.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trantantai.trantantai.constants.OrderStatus;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Category;
import trantantai.trantantai.entities.Invoice;
import trantantai.trantantai.services.BookService;
import trantantai.trantantai.services.CategoryService;
import trantantai.trantantai.services.OrderService;
import trantantai.trantantai.services.ReportService;
import trantantai.trantantai.viewmodels.BookSalesVm;
import trantantai.trantantai.viewmodels.ReportOverviewVm;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final ReportService reportService;

    public AdminController(BookService bookService, CategoryService categoryService, OrderService orderService, ReportService reportService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.reportService = reportService;
    }

    // ==================== DASHBOARD ====================
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("totalBooks", bookService.getTotalBooks());
        model.addAttribute("totalCategories", categoryService.getAllCategories().size());
        model.addAttribute("recentBooks", bookService.getAllBooks(0, 5, "id"));
        model.addAttribute("categories", categoryService.getAllCategories());
        
        // Order statistics
        Map<String, Long> orderStats = orderService.getOrderStatistics();
        model.addAttribute("orderStats", orderStats);
        
        return "admin/dashboard";
    }

    // ==================== BOOKS MANAGEMENT ====================
    @GetMapping("/books")
    public String listBooks(
            @NotNull Model model,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {

        List<Book> books = bookService.getAllBooks(pageNo, pageSize, sortBy);
        long totalBooks = bookService.getTotalBooks();
        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);

        model.addAttribute("pageTitle", "Quản lý Sách");
        model.addAttribute("activeMenu", "books");
        model.addAttribute("books", books);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalCategories", categoryService.getAllCategories().size());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("totalPages", totalPages > 0 ? totalPages - 1 : 0);

        return "admin/books/list";
    }

    @GetMapping("/books/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("pageTitle", "Thêm Sách Mới");
        model.addAttribute("activeMenu", "books");
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/books/add";
    }

    @PostMapping("/books/add")
    public String addBook(
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm Sách Mới");
            model.addAttribute("activeMenu", "books");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/books/add";
        }
        bookService.addBook(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/books/edit/{id}")
    public String showEditBookForm(@PathVariable String id, Model model) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        model.addAttribute("pageTitle", "Chỉnh Sửa Sách");
        model.addAttribute("activeMenu", "books");
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/books/edit";
    }

    @PostMapping("/books/edit/{id}")
    public String updateBook(
            @PathVariable String id,
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Chỉnh Sửa Sách");
            model.addAttribute("activeMenu", "books");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/books/edit";
        }
        book.setId(id);
        bookService.updateBook(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable String id) {
        bookService.deleteBookById(id);
        return "redirect:/admin/books";
    }

    // ==================== CATEGORIES MANAGEMENT ====================
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        
        // Build a map of categoryId -> bookCount for the view
        Map<String, Long> bookCountMap = new HashMap<>();
        for (Category category : categories) {
            long count = categoryService.countBooksInCategory(category.getId());
            bookCountMap.put(category.getId(), count);
        }
        
        model.addAttribute("pageTitle", "Quản lý Danh mục");
        model.addAttribute("activeMenu", "categories");
        model.addAttribute("categories", categories);
        model.addAttribute("bookCountMap", bookCountMap);
        return "admin/categories/list";
    }

    @GetMapping("/categories/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("pageTitle", "Thêm Danh Mục");
        model.addAttribute("activeMenu", "categories");
        model.addAttribute("category", new Category());
        return "admin/categories/add";
    }

    @PostMapping("/categories/add")
    public String addCategory(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm Danh Mục");
            model.addAttribute("activeMenu", "categories");
            return "admin/categories/add";
        }
        categoryService.addCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String showEditCategoryForm(@PathVariable String id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        model.addAttribute("pageTitle", "Chỉnh Sửa Danh Mục");
        model.addAttribute("activeMenu", "categories");
        model.addAttribute("category", category);
        return "admin/categories/edit";
    }

    @PostMapping("/categories/edit/{id}")
    public String updateCategory(
            @PathVariable String id,
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Chỉnh Sửa Danh Mục");
            model.addAttribute("activeMenu", "categories");
            return "admin/categories/edit";
        }
        category.setId(id);
        categoryService.updateCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable String id) {
        categoryService.deleteCategoryWithCascade(id);
        return "redirect:/admin/categories";
    }

    // ==================== INVENTORY MANAGEMENT ====================
    @GetMapping("/inventory")
    public String inventoryManagement(
            @NotNull Model model,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "quantity") String sortBy) {

        List<Book> books = bookService.getAllBooks(pageNo, pageSize, sortBy);
        long totalBooks = bookService.getTotalBooks();
        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);

        // Calculate inventory statistics
        long totalStock = books.stream().mapToLong(book -> book.getQuantity() != null ? book.getQuantity() : 0).sum();
        long booksInStock = books.stream().filter(book -> book.getQuantity() != null && book.getQuantity() > 5).count();
        long lowStockCount = books.stream().filter(book -> book.getQuantity() != null && book.getQuantity() > 0 && book.getQuantity() <= 5).count();
        long outOfStockCount = books.stream().filter(book -> book.getQuantity() == null || book.getQuantity() == 0).count();

        int percentInStock = totalBooks > 0 ? (int) ((booksInStock * 100) / totalBooks) : 0;

        model.addAttribute("pageTitle", "Quản lý Tồn kho");
        model.addAttribute("activeMenu", "inventory");
        model.addAttribute("books", books);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", totalPages > 0 ? totalPages - 1 : 0);

        // Inventory stats
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("booksInStock", booksInStock);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("percentInStock", percentInStock);

        return "admin/inventory/list";
    }

    // ==================== ORDERS MANAGEMENT ====================
    @GetMapping("/orders")
    public String listOrders(
            @NotNull Model model,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status) {

        // Parse status filter
        OrderStatus orderStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatus = OrderStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {
                // Invalid status, ignore
            }
        }

        Page<Invoice> ordersPage = orderService.getAllOrders(pageNo, pageSize, sortBy, sortDir, orderStatus);

        model.addAttribute("pageTitle", "Quản lý Đơn hàng");
        model.addAttribute("activeMenu", "orders");
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalOrders", ordersPage.getTotalElements());
        model.addAttribute("totalPages", ordersPage.getTotalPages() > 0 ? ordersPage.getTotalPages() - 1 : 0);
        model.addAttribute("orderStatuses", OrderStatus.values());

        // Current filter/sort values for the UI
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortDir", sortDir);
        model.addAttribute("currentStatus", status);

        // Order statistics
        Map<String, Long> orderStats = orderService.getOrderStatistics();
        model.addAttribute("orderStats", orderStats);

        return "admin/orders/list";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Invoice order = orderService.getOrderById(id).orElse(null);
        
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại");
            return "redirect:/admin/orders";
        }
        
        model.addAttribute("pageTitle", "Chi tiết đơn hàng #" + id.substring(0, 8));
        model.addAttribute("activeMenu", "orders");
        model.addAttribute("order", order);
        model.addAttribute("orderStatuses", OrderStatus.values());
        
        return "admin/orders/detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng và hoàn lại số lượng vào kho");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    // ==================== REPORTS & STATISTICS ====================
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("pageTitle", "Báo cáo & Thống kê");
        model.addAttribute("activeMenu", "reports");

        // Get date range for default "month" view
        Date[] dateRange = reportService.getDateRange("month", null, null);

        // Overview data from ReportService
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
}
