package com.gila.ecommerce.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration setup declaring endpoints filters and access rule policies.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OpaAuthorizationFilter opaAuthorizationFilter;

    /**
     * Constructor injecting filter dependencies.
     * @param jwtAuthenticationFilter JWT extraction and session mapping filter
     * @param opaAuthorizationFilter OPA authorization request filter
     */
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            OpaAuthorizationFilter opaAuthorizationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.opaAuthorizationFilter = opaAuthorizationFilter;
    }

    /**
     * Set up endpoint security authorization rules and session lifecycle properties.
     * @param http security building builder
     * @return configured filter chain
     * @throws Exception on policy building errors
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/products/import/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/products/import/status/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/cart/**").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/v1/orders/checkout").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/orders/clear").hasRole("ADMIN")
                .requestMatchers("/api/v1/audit-logs").hasRole("ADMIN")
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(opaAuthorizationFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Password encoder bean definition.
     * @return BCrypt utility encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager bean definition.
     * @param authenticationConfiguration authentication configuration reference
     * @return initialized authentication manager
     * @throws Exception on configuration fetch errors
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
