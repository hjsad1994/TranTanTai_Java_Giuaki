package trantantai.trantantai.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import trantantai.trantantai.constants.OrderStatus;
import trantantai.trantantai.entities.Invoice;

import java.util.List;

@Repository
public interface IInvoiceRepository extends MongoRepository<Invoice, String> {
    
    /**
     * Find all orders by user ID, sorted by invoice date descending
     */
    List<Invoice> findByUserIdOrderByInvoiceDateDesc(String userId);
    
    /**
     * Find all orders with pagination, sorted by invoice date descending
     */
    Page<Invoice> findAllByOrderByInvoiceDateDesc(Pageable pageable);
    
    /**
     * Count orders by order status
     */
    long countByOrderStatus(OrderStatus orderStatus);
    
    /**
     * Find orders by order status with pagination
     */
    Page<Invoice> findByOrderStatusOrderByInvoiceDateDesc(OrderStatus orderStatus, Pageable pageable);

    /**
     * Check if user has an order (not cancelled) containing the specified bookId.
     * Uses @Query because Spring Data MongoDB doesn't support derived queries
     * for fields inside embedded document lists (itemInvoices.bookId).
     * 
     * @param userId the user ID
     * @param bookId the book ID to check
     * @return true if user has purchased the book (order not cancelled)
     */
    @Query(value = "{ 'userId': ?0, 'orderStatus': { $ne: 'CANCELLED' }, 'itemInvoices.bookId': ?1 }", exists = true)
    boolean existsByUserIdAndDeliveredOrderContainingBook(String userId, String bookId);
}
