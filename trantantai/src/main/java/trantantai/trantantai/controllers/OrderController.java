package trantantai.trantantai.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import trantantai.trantantai.entities.Invoice;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.repositories.IUserRepository;
import trantantai.trantantai.services.OrderService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final IUserRepository userRepository;

    @Autowired
    public OrderController(OrderService orderService, IUserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    /**
     * User order history page.
     */
    @GetMapping
    public String listOrders(Authentication authentication, Model model) {
        String userId = extractUserId(authentication);
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        List<Invoice> orders = orderService.getOrdersByUserId(userId);
        
        model.addAttribute("orders", orders);
        model.addAttribute("pageTitle", "Đơn hàng của tôi");
        
        return "user/orders";
    }

    /**
     * User order detail page.
     */
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable String id, Authentication authentication, Model model) {
        String userId = extractUserId(authentication);
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        Optional<Invoice> orderOpt = orderService.getOrderById(id);
        
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }
        
        Invoice order = orderOpt.get();
        
        // Verify the order belongs to the current user
        if (!userId.equals(order.getUserId())) {
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Chi tiết đơn hàng #" + id.substring(0, 8));
        
        return "user/order-detail";
    }

    private String extractUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return ((User) principal).getId();
        }
        
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return userRepository.findByEmail(email)
                        .map(User::getId)
                        .orElse(null);
            }
        }
        
        return null;
    }
}
