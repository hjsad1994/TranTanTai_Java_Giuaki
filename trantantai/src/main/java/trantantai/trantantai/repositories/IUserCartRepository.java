package trantantai.trantantai.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import trantantai.trantantai.entities.UserCart;

import java.util.Optional;

@Repository
public interface IUserCartRepository extends MongoRepository<UserCart, String> {
    
    /**
     * Find cart by user ID
     */
    Optional<UserCart> findByUserId(String userId);
    
    /**
     * Delete cart by user ID
     */
    void deleteByUserId(String userId);
    
    /**
     * Check if user has a saved cart
     */
    boolean existsByUserId(String userId);
}
