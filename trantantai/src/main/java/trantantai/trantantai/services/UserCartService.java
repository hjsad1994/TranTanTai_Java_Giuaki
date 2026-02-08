package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trantantai.trantantai.daos.Item;
import trantantai.trantantai.entities.UserCart;
import trantantai.trantantai.repositories.IUserCartRepository;

import java.util.List;
import java.util.logging.Logger;

@Service
public class UserCartService {

    private static final Logger logger = Logger.getLogger(UserCartService.class.getName());

    private final IUserCartRepository userCartRepository;

    @Autowired
    public UserCartService(IUserCartRepository userCartRepository) {
        this.userCartRepository = userCartRepository;
    }

    /**
     * Remove a specific book from all user carts.
     * Called when a book is deleted to clean up orphaned cart items.
     * @param bookId the book ID to remove
     * @return number of carts that were updated
     */
    public int removeBookFromAllCarts(String bookId) {
        List<UserCart> affectedCarts = userCartRepository.findByCartItems_BookId(bookId);
        int updatedCount = 0;

        for (UserCart cart : affectedCarts) {
            List<Item> items = cart.getCartItems();
            boolean removed = items.removeIf(item -> bookId.equals(item.getBookId()));

            if (removed) {
                userCartRepository.save(cart);
                updatedCount++;
                logger.info("Removed book " + bookId + " from cart of user: " + cart.getUserId());
            }
        }

        if (updatedCount > 0) {
            logger.info("Removed book " + bookId + " from " + updatedCount + " user carts");
        }

        return updatedCount;
    }

    /**
     * Remove multiple books from all user carts.
     * Called when a category is deleted (cascade delete).
     * @param bookIds list of book IDs to remove
     * @return number of carts that were updated
     */
    public int removeBooksFromAllCarts(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return 0;
        }

        int totalUpdated = 0;
        for (String bookId : bookIds) {
            totalUpdated += removeBookFromAllCarts(bookId);
        }

        return totalUpdated;
    }
}
