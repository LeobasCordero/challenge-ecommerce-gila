package com.gila.ecommerce.security;

import com.gila.ecommerce.model.User;
import com.gila.ecommerce.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service adapting UserRepository database queries to Spring Security UserDetailsService requirements.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor injecting UserRepository.
     * @param userRepository user repository database interface
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieve user authentication details by username string.
     * @param username username lookup key
     * @return populated UserPrincipal containing user records
     * @throws UsernameNotFoundException if user records not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new UserPrincipal(user);
    }
}
