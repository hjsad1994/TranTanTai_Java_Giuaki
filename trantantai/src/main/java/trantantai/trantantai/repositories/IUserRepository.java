package trantantai.trantantai.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import trantantai.trantantai.entities.User;

import java.util.Optional;

@Repository
public interface IUserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    Optional<User> findByEmail(String email);
}
