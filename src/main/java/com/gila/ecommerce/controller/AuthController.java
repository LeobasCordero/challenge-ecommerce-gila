package com.gila.ecommerce.controller;

import com.gila.ecommerce.api.AuthApi;
import com.gila.ecommerce.dto.LoginRequestDto;
import com.gila.ecommerce.dto.LoginResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller stub implementing authentication endpoint.
 */
@RestController
public class AuthController implements AuthApi {

    /**
     * Authenticate login credentials and return a mock JWT response.
     * @param loginRequestDto the login request body
     * @return login response containing mock JWT token
     */
    @Override
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto loginRequestDto) {
        LoginResponseDto response = new LoginResponseDto();
        response.setToken("mock-jwt-token-for-testing");
        return ResponseEntity.ok(response);
    }
}
