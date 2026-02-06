package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
     * Populates book details in each ItemInvoice.
     */
    public List<Invoice> getOrdersByUserId(String userId) {
        List<Invoice> orders = invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId);
        orders.forEach(this::populateBookDetails);
        return orders;
    }

    /**
     * Get all orders with pagination (for admin).
     * Populates book details in each ItemInvoice.
     */
    public Page<Invoice> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> orders = invoiceRepository.findAllByOrderByInvoiceDateDesc(pageable);
        orders.forEach(this::populateBookDetails);
        return orders;
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
        
        stats.put("total", invoiceRepository.count());
        stats.put("processing", invoiceRepository.countByOrderStatus(OrderStatus.PROCESSING));
        stats.put("shipped", invoiceRepository.countByOrderStatus(OrderStatus.SHIPPED));
        stats.put("delivered", invoiceRepository.countByOrderStatus(OrderStatus.DELIVERED));
        stats.put("cancelled", invoiceRepository.countByOrderStatus(OrderStatus.CANCELLED));
        
        return stats;
    }

    /**
     * Populate book details in ItemInvoice @Transient field.
     */
    private void populateBookDetails(Invoice invoice) {
        if (invoice == null || invoice.getItemInvoices() == null) {
            return;
        }
        
        for (ItemInvoice item : invoice.getItemInvoices()) {
            bookService.getBookById(item.getBookId())
                    .ifPresent(item::setBook);
        }
    }
}
