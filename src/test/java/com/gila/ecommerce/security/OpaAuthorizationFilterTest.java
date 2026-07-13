package com.gila.ecommerce.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class OpaAuthorizationFilterTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private OpaAuthorizationFilter opaAuthorizationFilter;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(opaAuthorizationFilter, "opaUrl", "http://localhost:8181");
        ReflectionTestUtils.setField(opaAuthorizationFilter, "opaEnabled", true);
        ReflectionTestUtils.setField(opaAuthorizationFilter, "restTemplate", restTemplate);
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testFilter_Disabled() throws Exception {
        ReflectionTestUtils.setField(opaAuthorizationFilter, "opaEnabled", false);

        opaAuthorizationFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(restTemplate);
    }

    @Test
    public void testFilter_AllowedByOpa() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getMethod()).thenReturn("GET");

        OpaAuthorizationFilter.OpaResponse opaResponse = new OpaAuthorizationFilter.OpaResponse();
        opaResponse.setResult(true);

        when(restTemplate.postForEntity(
                eq("http://localhost:8181/v1/data/app/authz/allow"),
                any(HttpEntity.class),
                eq(OpaAuthorizationFilter.OpaResponse.class)
        )).thenReturn(new ResponseEntity<>(opaResponse, HttpStatus.OK));

        opaAuthorizationFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testFilter_DeniedByOpa() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getMethod()).thenReturn("POST");

        OpaAuthorizationFilter.OpaResponse opaResponse = new OpaAuthorizationFilter.OpaResponse();
        opaResponse.setResult(false);

        when(restTemplate.postForEntity(
                eq("http://localhost:8181/v1/data/app/authz/allow"),
                any(HttpEntity.class),
                eq(OpaAuthorizationFilter.OpaResponse.class)
        )).thenReturn(new ResponseEntity<>(opaResponse, HttpStatus.OK));

        StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        opaAuthorizationFilter.doFilter(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    public void testFilter_OpaUnreachableFallback() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getMethod()).thenReturn("GET");

        // Simulate connection refusal exception
        when(restTemplate.postForEntity(
                eq("http://localhost:8181/v1/data/app/authz/allow"),
                any(HttpEntity.class),
                eq(OpaAuthorizationFilter.OpaResponse.class)
        )).thenThrow(new RuntimeException("Connection refused"));

        opaAuthorizationFilter.doFilter(request, response, filterChain);

        // Fail-safe should fall back and allow request to proceed to the next filter
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
