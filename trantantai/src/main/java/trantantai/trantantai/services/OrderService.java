package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import trantantai.trantantai.constants.OrderStatus;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Invoice;
import trantantai.trantantai.entities.ItemInvoice;
import trantantai.trantantai.repositories.IInvoiceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    private final IInvoiceRepository invoiceRepository;
    private final BookService bookService;

    @Autowired
    public OrderService(IInvoiceRepository invoiceRepository, BookService bookService) {
        this.invoiceRepository = invoiceRepository;
        this.bookService = bookService;
    }

    /**
     * Get all orders for a specific user, sorted by date descending.
     * Populates book details in each ItemInvoice (batch optimized).
     */
    public List<Invoice> getOrdersByUserId(String userId) {
        List<Invoice> orders = invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId);
        populateBookDetailsForList(orders);
        return orders;
    }

    /**
     * Get all orders with pagination and sorting (for admin).
     * Populates book details in each ItemInvoice (batch optimized).
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @param sortBy Sort field (invoiceDate, price)
     * @param sortDir Sort direction (asc, desc)
     * @param status Optional status filter
     */
    public Page<Invoice> getAllOrders(int page, int size, String sortBy, String sortDir, OrderStatus status) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        logger.info("=== Fetching orders ===");
        logger.info("Page: " + page + ", Size: " + size + ", SortBy: " + sortBy + ", SortDir: " + sortDir + ", Status: " + status);

        Page<Invoice> orders;
        if (status != null) {
            orders = invoiceRepository.findByOrderStatus(status, pageable);
        } else {
            orders = invoiceRepository.findAll(pageable);
        }

        logger.info("Found " + orders.getTotalElements() + " total orders, " + orders.getContent().size() + " on this page");

        // Batch populate book details
        populateBookDetailsForList(orders.getContent());
        return orders;
    }

    /**
     * Get all orders with pagination (for admin) - default sorted by newest first.
     * Populates book details in each ItemInvoice.
     */
    public Page<Invoice> getAllOrders(int page, int size) {
        return getAllOrders(page, size, "invoiceDate", "desc", null);
    }

    /**
     * Get a single order by ID.
     * Populates book details.
     */
    public Optional<Invoice> getOrderById(String id) {
        Optional<Invoice> orderOpt = invoiceRepository.findById(id);
        orderOpt.ifPresent(this::populateBookDetails);
        return orderOpt;
    }

    /**
     * Update order status.
     */
    public Invoice updateOrderStatus(String orderId, OrderStatus newStatus) {
        Invoice invoice = invoiceRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + orderId));
        
        invoice.setOrderStatus(newStatus);
        Invoice saved = invoiceRepository.save(invoice);
        
        logger.info("Updated order " + orderId + " status to " + newStatus);
        return saved;
    }

    /**
     * Cancel order and restore stock.
     */
    public Invoice cancelOrder(String orderId) {
        Invoice invoice = invoiceRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + orderId));
        
        if (invoice.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Đơn hàng đã bị hủy trước đó");
        }
        
        if (invoice.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Không thể hủy đơn hàng đã giao");
        }
        
        // Restore stock for each item
        for (ItemInvoice item : invoice.getItemInvoices()) {
            boolean success = bookService.incrementStock(item.getBookId(), item.getQuantity());
            if (success) {
                logger.info("Restored " + item.getQuantity() + " units for book: " + item.getBookId());
            } else {
                logger.warning("Failed to restore stock for book: " + item.getBookId());
            }
        }
        
        invoice.setOrderStatus(OrderStatus.CANCELLED);
        Invoice saved = invoiceRepository.save(invoice);
        
        logger.info("Cancelled order: " + orderId);
        return saved;
    }

    /**
     * Get order statistics by status.
     */
    public Map<String, Long> getOrderStatistics() {
        Map<String, Long> stats = new HashMap<>();

        long total = invoiceRepository.count();
        long processing = invoiceRepository.countByOrderStatus(OrderStatus.PROCESSING);
        long shipped = invoiceRepository.countByOrderStatus(OrderStatus.SHIPPED);
        long delivered = invoiceRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long cancelled = invoiceRepository.countByOrderStatus(OrderStatus.CANCELLED);

        logger.info("=== Order Statistics ===");
        logger.info("Total: " + total);
        logger.info("Processing: " + processing);
        logger.info("Shipped: " + shipped);
        logger.info("Delivered: " + delivered);
        logger.info("Cancelled: " + cancelled);

        stats.put("total", total);
        stats.put("processing", processing);
        stats.put("shipped", shipped);
        stats.put("delivered", delivered);
        stats.put("cancelled", cancelled);

        return stats;
    }

    /**
     * Populate book details in ItemInvoice @Transient field.
     * Optimized: fetches all books in one query.
     */
    private void populateBookDetails(Invoice invoice) {
        if (invoice == null || invoice.getItemInvoices() == null || invoice.getItemInvoices().isEmpty()) {
            return;
        }

        // Collect all book IDs
        List<String> bookIds = invoice.getItemInvoices().stream()
                .map(ItemInvoice::getBookId)
                .distinct()
                .collect(Collectors.toList());

        // Fetch all books at once
        Map<String, Book> bookMap = bookService.getBooksByIds(bookIds);

        // Assign books to items
        for (ItemInvoice item : invoice.getItemInvoices()) {
            Book book = bookMap.get(item.getBookId());
            if (book != null) {
                item.setBook(book);
            }
        }
    }

    /**
     * Populate book details for multiple invoices (batch optimized).
     */
    private void populateBookDetailsForList(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return;
        }

        // Collect all book IDs from all invoices
        List<String> allBookIds = invoices.stream()
                .filter(inv -> inv.getItemInvoices() != null)
                .flatMap(inv -> inv.getItemInvoices().stream())
                .map(ItemInvoice::getBookId)
                .distinct()
                .collect(Collectors.toList());

        if (allBookIds.isEmpty()) {
            return;
        }

        // Fetch all books at once
        Map<String, Book> bookMap = bookService.getBooksByIds(allBookIds);

        // Assign books to items in all invoices
        for (Invoice invoice : invoices) {
            if (invoice.getItemInvoices() != null) {
                for (ItemInvoice item : invoice.getItemInvoices()) {
                    Book book = bookMap.get(item.getBookId());
                    if (book != null) {
                        item.setBook(book);
                    }
                }
            }
        }
    }
}
