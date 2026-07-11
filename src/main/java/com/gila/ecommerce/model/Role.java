package com.gila.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * JPA entity representing user role records.
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Retrieve role UUID.
     * @return unique role identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Set role UUID.
     * @param id unique role identifier
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Retrieve role name.
     * @return name of role
     */
    public String getName() {
        return name;
    }

    /**
     * Set role name.
     * @param name name of role
     */
    public void setName(String name) {
        this.name = name;
    }
}
