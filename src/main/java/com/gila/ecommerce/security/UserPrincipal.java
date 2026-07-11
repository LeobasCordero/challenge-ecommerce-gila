package com.gila.ecommerce.security;

import com.gila.ecommerce.model.User;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Custom UserDetails implementation wrapping User database entities.
 */
public class UserPrincipal implements UserDetails {

    private final User user;

    /**
     * Constructor setting wrapped User entity.
     * @param user target User database entity
     */
    public UserPrincipal(User user) {
        this.user = user;
    }

    /**
     * Retrieve the wrapped User entity.
     * @return User database entity
     */
    public User getUser() {
        return user;
    }

    /**
     * Map user roles to collection of granted authority values.
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve the user password.
     * @return encoded password string
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Retrieve the user username.
     * @return username lookup string
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Check if account is not expired.
     * @return true always
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Check if account is not locked.
     * @return true always
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Check if credentials are not expired.
     * @return true always
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Check if account is enabled.
     * @return true if user enabled flag is set, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
