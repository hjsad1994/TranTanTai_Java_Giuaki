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
import trantantai.trantantai.repositories.IInvoiceRepository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "cart";

    private final IInvoiceRepository invoiceRepository;
    private final BookService bookService;

    @Autowired
    public CartService(IInvoiceRepository invoiceRepository, BookService bookService) {
        this.invoiceRepository = invoiceRepository;
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
     * Save cart to database as Invoice with embedded ItemInvoices.
     * Validates stock availability and decrements stock atomically.
     * Clears cart after successful save.
     */
    public void saveCart(@NotNull HttpSession session) {
        Cart cart = getCart(session);
        
        // Guard: do not save empty cart
        if (cart.getCartItems().isEmpty()) {
            return;
        }
        
        // Step 1: Validate stock availability for all items
        for (Item item : cart.getCartItems()) {
            Book book = bookService.getBookById(item.getBookId())
                    .orElseThrow(() -> new RuntimeException("Sách không tồn tại"));
            if (book.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Không đủ hàng trong kho cho sách: " + book.getTitle());
            }
        }
        
        // Step 2: Atomically decrement stock for all items
        for (Item item : cart.getCartItems()) {
            boolean success = bookService.decrementStock(item.getBookId(), item.getQuantity());
            if (!success) {
                throw new RuntimeException("Không thể giảm số lượng tồn kho");
            }
        }
        
        // Create new Invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(new Date());
        invoice.setPrice(getSumPrice(session));
        
        // Get current authenticated user's ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            invoice.setUserId(user.getId());
        }
        
        // Convert cart items to ItemInvoices
        for (Item item : cart.getCartItems()) {
            ItemInvoice itemInvoice = new ItemInvoice();
            itemInvoice.setId(UUID.randomUUID().toString());
            itemInvoice.setBookId(item.getBookId());
            itemInvoice.setQuantity(item.getQuantity());
            invoice.addItemInvoice(itemInvoice);
        }
        
        // Save invoice to MongoDB
        invoiceRepository.save(invoice);
        
        // Clear cart from session
        removeCart(session);
    }
}
