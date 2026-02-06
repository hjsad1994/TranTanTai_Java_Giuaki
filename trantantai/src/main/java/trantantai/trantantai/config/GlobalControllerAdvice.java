package trantantai.trantantai.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.services.CartService;
import trantantai.trantantai.services.WishlistService;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;
    private final WishlistService wishlistService;

    @Autowired
    public GlobalControllerAdvice(CartService cartService, WishlistService wishlistService) {
        this.cartService = cartService;
        this.wishlistService = wishlistService;
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        return cartService.getSumQuantity(session);
    }

    @ModelAttribute("cartTotal")
    public double cartTotal(HttpSession session) {
        return cartService.getSumPrice(session);
    }

    @ModelAttribute("wishlistCount")
    public long wishlistCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            return wishlistService.getWishlistCount(user.getId());
        }
        return 0;
    }
}
