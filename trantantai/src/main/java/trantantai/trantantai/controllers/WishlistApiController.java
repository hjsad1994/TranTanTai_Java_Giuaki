package trantantai.trantantai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.services.WishlistService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Wishlist", description = "Wishlist management APIs - Add, remove, and manage user wishlist")
@RestController
@RequestMapping("/api/wishlist")
public class WishlistApiController {

    @Autowired
    private WishlistService wishlistService;

    /**
     * Get all books in user's wishlist
     */
    @Operation(summary = "Get user wishlist", description = "Retrieves all books in the authenticated user's wishlist")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved wishlist"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping
    public ResponseEntity<List<Book>> getWishlist(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<Book> books = wishlistService.getWishlistBooks(user.getId());
        return ResponseEntity.ok(books);
    }

    /**
     * Add a book to wishlist
     */
    @Operation(summary = "Add book to wishlist", description = "Adds a book to the authenticated user's wishlist")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book added to wishlist successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @PostMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> addToWishlist(
            @Parameter(description = "Book ID to add", required = true) @PathVariable String bookId,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        wishlistService.addToWishlist(user.getId(), bookId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Added to wishlist");
        response.put("inWishlist", true);
        response.put("wishlistCount", wishlistService.getWishlistCount(user.getId()));

        return ResponseEntity.ok(response);
    }

    /**
     * Remove a book from wishlist
     */
    @Operation(summary = "Remove book from wishlist", description = "Removes a book from the authenticated user's wishlist")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book removed from wishlist successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @Parameter(description = "Book ID to remove", required = true) @PathVariable String bookId,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        wishlistService.removeFromWishlist(user.getId(), bookId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Removed from wishlist");
        response.put("inWishlist", false);
        response.put("wishlistCount", wishlistService.getWishlistCount(user.getId()));

        return ResponseEntity.ok(response);
    }

    /**
     * Toggle book in wishlist (add/remove)
     */
    @Operation(summary = "Toggle book in wishlist", description = "Adds book if not in wishlist, removes if already in wishlist")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Wishlist toggled successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @PostMapping("/{bookId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @Parameter(description = "Book ID to toggle", required = true) @PathVariable String bookId,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        boolean added = wishlistService.toggleWishlist(user.getId(), bookId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", added ? "Added to wishlist" : "Removed from wishlist");
        response.put("inWishlist", added);
        response.put("wishlistCount", wishlistService.getWishlistCount(user.getId()));

        return ResponseEntity.ok(response);
    }

    /**
     * Check if a book is in wishlist
     */
    @Operation(summary = "Check book in wishlist", description = "Checks if a specific book is in the user's wishlist")
    @ApiResponse(responseCode = "200", description = "Check completed successfully")
    @GetMapping("/{bookId}/check")
    public ResponseEntity<Map<String, Object>> checkInWishlist(
            @Parameter(description = "Book ID to check", required = true) @PathVariable String bookId,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("inWishlist", false);
            return ResponseEntity.ok(response);
        }

        boolean inWishlist = wishlistService.isInWishlist(user.getId(), bookId);

        Map<String, Object> response = new HashMap<>();
        response.put("inWishlist", inWishlist);

        return ResponseEntity.ok(response);
    }

    /**
     * Get wishlist count
     */
    @Operation(summary = "Get wishlist count", description = "Returns the total number of items in the user's wishlist")
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getWishlistCount(@AuthenticationPrincipal User user) {
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        long count = wishlistService.getWishlistCount(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Clear wishlist
     */
    @Operation(summary = "Clear wishlist", description = "Removes all books from the authenticated user's wishlist")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Wishlist cleared successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearWishlist(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        wishlistService.clearWishlist(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Wishlist cleared");

        return ResponseEntity.ok(response);
    }
}
