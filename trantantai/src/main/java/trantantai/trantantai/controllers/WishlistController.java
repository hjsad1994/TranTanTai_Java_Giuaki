package trantantai.trantantai.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.services.WishlistService;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    /**
     * Display user's wishlist page
     */
    @GetMapping
    public String showWishlist(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        List<Book> wishlistBooks = wishlistService.getWishlistBooks(user.getId());
        model.addAttribute("wishlistBooks", wishlistBooks);
        model.addAttribute("wishlistCount", wishlistBooks.size());

        return "book/wishlist";
    }
}
