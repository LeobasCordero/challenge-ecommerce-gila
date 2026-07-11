package com.gila.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity representing user records.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Retrieve user UUID.
     * @return unique user identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Set user UUID.
     * @param id unique user identifier
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Retrieve username.
     * @return unique username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username.
     * @param username unique username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieve encoded password.
     * @return encoded password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set encoded password.
     * @param password encoded password string
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Check if user is enabled.
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set enabled flag.
     * @param enabled enabled flag value
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Retrieve set of roles assigned to user.
     * @return set of user roles
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * Set roles assigned to user.
     * @param roles set of user roles
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
