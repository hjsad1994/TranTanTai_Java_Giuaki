package trantantai.trantantai.services;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import trantantai.trantantai.daos.Cart;
import trantantai.trantantai.daos.Item;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Invoice;
import trantantai.trantantai.entities.ItemInvoice;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.entities.UserCart;
import trantantai.trantantai.repositories.IInvoiceRepository;
import trantantai.trantantai.repositories.IUserCartRepository;
import trantantai.trantantai.constants.PaymentStatus;
import trantantai.trantantai.constants.PaymentMethod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "cart";
    private static final Logger logger = Logger.getLogger(CartService.class.getName());

    private final IInvoiceRepository invoiceRepository;
    private final IUserCartRepository userCartRepository;
    private final BookService bookService;

    @Autowired
    public CartService(IInvoiceRepository invoiceRepository, 
                       IUserCartRepository userCartRepository,
                       BookService bookService) {
        this.invoiceRepository = invoiceRepository;
        this.userCartRepository = userCartRepository;
        this.bookService = bookService;
    }

    public Cart getCart(@NotNull HttpSession session) {
        return Optional.ofNullable((Cart) session.getAttribute(CART_SESSION_KEY))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    session.setAttribute(CART_SESSION_KEY, cart);
                    return cart;
                });
    }

    public void updateCart(@NotNull HttpSession session, Cart cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void removeCart(@NotNull HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public int getSumQuantity(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToInt(Item::getQuantity)
                .sum();
    }

    public double getSumPrice(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    /**
     * Save cart with specified payment method
     */
    public Invoice saveCart(@NotNull HttpSession session, PaymentMethod paymentMethod) {
        Cart cart = getCart(session);
        
        if (cart.getCartItems().isEmpty()) {
            return null;
        }
        
        // Validate and decrement stock (existing logic)
        for (Item item : cart.getCartItems()) {
            Book book = bookService.getBookById(item.getBookId())
                    .orElseThrow(() -> new RuntimeException("Sách không tồn tại"));
            if (book.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Không đủ hàng trong kho cho sách: " + book.getTitle());
            }
        }
        
        for (Item item : cart.getCartItems()) {
            boolean success = bookService.decrementStock(item.getBookId(), item.getQuantity());
            if (!success) {
                throw new RuntimeException("Không thể giảm số lượng tồn kho");
            }
        }
        
        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(new Date());
        invoice.setPrice(getSumPrice(session));
        invoice.setPaymentMethod(paymentMethod);
        
        // Set payment status based on method
        if (paymentMethod == PaymentMethod.COD) {
            invoice.setPaymentStatus(PaymentStatus.COD_PENDING);
        } else {
            invoice.setPaymentStatus(PaymentStatus.PENDING_PAYMENT);
        }
        
        // Get user ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            invoice.setUserId(user.getId());
        }
        
        // Convert items
        for (Item item : cart.getCartItems()) {
            ItemInvoice itemInvoice = new ItemInvoice();
            itemInvoice.setId(UUID.randomUUID().toString());
            itemInvoice.setBookId(item.getBookId());
            itemInvoice.setQuantity(item.getQuantity());
            invoice.addItemInvoice(itemInvoice);
        }
        
        // Save and clear
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Only clear cart for COD (MOMO clears after payment confirmed)
        if (paymentMethod == PaymentMethod.COD) {
            removeCart(session);
        }
        
        return savedInvoice;
    }
    
    /**
     * Save cart to database as Invoice with embedded ItemInvoices.
     * Validates stock availability and decrements stock atomically.
     * Clears cart after successful save.
     */
    public void saveCart(@NotNull HttpSession session) {
        saveCart(session, PaymentMethod.COD);
    }
    
    /**
     * Update payment status after MoMo callback
     */
    public void updatePaymentStatus(String invoiceId, PaymentStatus status, String transactionId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice != null) {
            invoice.setPaymentStatus(status);
            if (transactionId != null) {
                invoice.setMomoTransactionId(transactionId);
            }
            invoiceRepository.save(invoice);
        }
    }
    
    /**
     * Find invoice by ID
     */
    public Optional<Invoice> findInvoiceById(String id) {
        return invoiceRepository.findById(id);
    }
    
    /**
     * Set MoMo request ID on invoice (for querying status later)
     */
    public void setMomoRequestId(String invoiceId, String requestId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice != null) {
            invoice.setMomoRequestId(requestId);
            invoiceRepository.save(invoice);
        }
    }
    
    // ==================== CART PERSISTENCE METHODS ====================
    
    /**
     * Save cart to database for a user.
     * Called before logout to preserve cart across sessions.
     */
    public void saveCartToDatabase(HttpSession session, String userId) {
        if (userId == null) {
            logger.warning("Cannot save cart: userId is null");
            return;
        }
        
        Cart cart = getCart(session);
        if (cart.getCartItems().isEmpty()) {
            // Delete existing cart if session cart is empty
            userCartRepository.deleteByUserId(userId);
            logger.info("Deleted empty cart for user: " + userId);
            return;
        }
        
        // Find existing or create new
        UserCart userCart = userCartRepository.findByUserId(userId)
                .orElse(new UserCart(userId));
        
        // Copy items from session cart to persistent cart
        userCart.setCartItems(new ArrayList<>(cart.getCartItems()));
        userCart.setLastUpdated(new Date());
        
        userCartRepository.save(userCart);
        logger.info("Saved cart with " + cart.getCartItems().size() + " items for user: " + userId);
    }
    
    /**
     * Restore cart from database after login.
     * Merges with existing session cart if any.
     */
    public void restoreCartFromDatabase(HttpSession session, String userId) {
        if (userId == null) {
            logger.warning("Cannot restore cart: userId is null");
            return;
        }
        
        Optional<UserCart> savedCartOpt = userCartRepository.findByUserId(userId);
        if (savedCartOpt.isEmpty()) {
            logger.info("No saved cart found for user: " + userId);
            return;
        }
        
        UserCart savedCart = savedCartOpt.get();
        Cart sessionCart = getCart(session);
        
        // Merge saved cart into session cart
        List<Item> mergedItems = mergeCartItems(sessionCart.getCartItems(), savedCart.getCartItems());
        sessionCart.setCartItems(mergedItems);
        updateCart(session, sessionCart);
        
        // Delete saved cart from DB after restoration
        userCartRepository.deleteByUserId(userId);
        
        logger.info("Restored and merged cart with " + mergedItems.size() + " items for user: " + userId);
    }
    
    /**
     * Merge two lists of cart items.
     * For duplicate items (same bookId), quantities are summed.
     * Invalid items (deleted books, out of stock) are skipped.
     */
    private List<Item> mergeCartItems(List<Item> sessionItems, List<Item> savedItems) {
        List<Item> merged = new ArrayList<>(sessionItems);
        
        for (Item savedItem : savedItems) {
            // Validate item - check if book still exists and has stock
            Optional<Book> bookOpt = bookService.getBookById(savedItem.getBookId());
            if (bookOpt.isEmpty()) {
                logger.warning("Skipping cart item - book no longer exists: " + savedItem.getBookId());
                continue;
            }
            
            Book book = bookOpt.get();
            if (book.getQuantity() == null || book.getQuantity() <= 0) {
                logger.warning("Skipping cart item - book out of stock: " + book.getTitle());
                continue;
            }
            
            // Find existing item in session cart
            Optional<Item> existingItem = merged.stream()
                    .filter(item -> Objects.equals(item.getBookId(), savedItem.getBookId()))
                    .findFirst();
            
            if (existingItem.isPresent()) {
                // Sum quantities, cap at available stock
                int newQuantity = existingItem.get().getQuantity() + savedItem.getQuantity();
                int cappedQuantity = Math.min(newQuantity, book.getQuantity());
                existingItem.get().setQuantity(cappedQuantity);
                
                // Update price in case it changed
                existingItem.get().setPrice(book.getPrice());
                existingItem.get().setBookName(book.getTitle());
            } else {
                // Add new item, cap at available stock
                int cappedQuantity = Math.min(savedItem.getQuantity(), book.getQuantity());
                Item newItem = new Item(
                        savedItem.getBookId(),
                        book.getTitle(),
                        book.getPrice(),
                        cappedQuantity
                );
                merged.add(newItem);
            }
        }
        
        return merged;
    }
}
