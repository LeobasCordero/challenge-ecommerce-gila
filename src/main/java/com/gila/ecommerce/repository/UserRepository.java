package com.gila.ecommerce.repository;

import com.gila.ecommerce.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing User database entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Retrieve a user by username.
     * @param username username lookup key
     * @return optional containing the user if found
     */
    Optional<User> findByUsername(String username);
}
