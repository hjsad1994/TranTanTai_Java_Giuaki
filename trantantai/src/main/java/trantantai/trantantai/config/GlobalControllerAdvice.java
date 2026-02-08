package trantantai.trantantai.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.repositories.IUserRepository;
import trantantai.trantantai.services.CartService;
import trantantai.trantantai.services.WishlistService;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;
    private final WishlistService wishlistService;
    private final IUserRepository userRepository;

    @Autowired
    public GlobalControllerAdvice(CartService cartService, WishlistService wishlistService, IUserRepository userRepository) {
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.userRepository = userRepository;
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
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            
            if (principal instanceof User) {
                return wishlistService.getWishlistCount(((User) principal).getId());
            }
            
            if (principal instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) principal;
                String email = oauth2User.getAttribute("email");
                if (email != null) {
                    return userRepository.findByEmail(email)
                            .map(user -> wishlistService.getWishlistCount(user.getId()))
                            .orElse(0L);
                }
            }
        }
        return 0;
    }
}
