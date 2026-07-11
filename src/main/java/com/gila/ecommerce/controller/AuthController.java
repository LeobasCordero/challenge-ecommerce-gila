package com.gila.ecommerce.controller;

import com.gila.ecommerce.api.AuthApi;
import com.gila.ecommerce.aspect.Auditable;
import com.gila.ecommerce.dto.LoginRequestDto;
import com.gila.ecommerce.dto.LoginResponseDto;
import com.gila.ecommerce.security.JwtTokenProvider;
import com.gila.ecommerce.util.AuditAction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementing authentication endpoints.
 */
@RestController
public class AuthController implements AuthApi {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Constructor injecting dependencies.
     * @param authenticationManager authentication manager bean
     * @param tokenProvider JWT token utility helper
     */
    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Authenticate credentials and return signed JWT token.
     * @param loginRequestDto user credentials details
     * @return response containing authentication token
     */
    @Override
    @Auditable(action = AuditAction.LOGIN)
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(loginRequestDto.getUsername());
        LoginResponseDto response = new LoginResponseDto();
        response.setToken(jwt);

        return ResponseEntity.ok(response);
    }
}
