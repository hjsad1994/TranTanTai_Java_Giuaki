package trantantai.trantantai.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import trantantai.trantantai.entities.Category;

import java.util.List;

@Repository
public interface ICategoryRepository extends MongoRepository<Category, String> {
    
    // Search categories by name containing keyword (case-insensitive)
    List<Category> findByNameContainingIgnoreCase(String keyword);
}
