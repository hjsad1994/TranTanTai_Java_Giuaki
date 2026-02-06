package trantantai.trantantai.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import trantantai.trantantai.entities.RoleEntity;

import java.util.Optional;

@Repository
public interface IRoleRepository extends MongoRepository<RoleEntity, String> {
    
    /**
     * Find a role by its name.
     * @param name The role name (e.g., "ADMIN", "USER")
     * @return Optional containing the role if found
     */
    Optional<RoleEntity> findByName(String name);
}
