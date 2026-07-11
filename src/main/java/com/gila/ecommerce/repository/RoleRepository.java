package com.gila.ecommerce.repository;

import com.gila.ecommerce.model.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Role database entities.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Retrieve a role by name.
     * @param name role name lookup key
     * @return optional containing the role if found
     */
    Optional<Role> findByName(String name);
}
