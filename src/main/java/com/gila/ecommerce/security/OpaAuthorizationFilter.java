package com.gila.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter requesting permission checks to Open Policy Agent (OPA) server.
 * Operates on a fail-safe fallback check using standard authorities if OPA server is down.
 */
@Component
public class OpaAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(OpaAuthorizationFilter.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${opa.url:http://localhost:8181}")
    private String opaUrl;

    @Value("${opa.enabled:true}")
    private boolean opaEnabled;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!opaEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // Parse path into segments
        String cleanPath = requestUri.startsWith("/") ? requestUri.substring(1) : requestUri;
        String[] pathSegments = cleanPath.isEmpty() ? new String[0] : cleanPath.split("/");

        // Extract roles from current Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = Collections.emptyList();
        if (authentication != null && authentication.isAuthenticated()) {
            roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }

        // Build input payload for OPA
        Map<String, Object> input = new HashMap<>();
        input.put("path", pathSegments);
        input.put("method", method);
        input.put("roles", roles);

        Map<String, Object> payload = new HashMap<>();
        payload.put("input", input);

        boolean allowed = false;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            String url = opaUrl + "/v1/data/app/authz/allow";
            ResponseEntity<OpaResponse> opaResponse = restTemplate.postForEntity(url, entity, OpaResponse.class);

            if (opaResponse.getStatusCode().is2xxSuccessful() && opaResponse.getBody() != null) {
                allowed = opaResponse.getBody().getResult();
            }
        } catch (Exception e) {
            // Fallback Resilience Strategy: Fail safe/open and log a warning if OPA is down.
            logger.warn("OPA authorization service at {} is unreachable. Falling back to Spring Security roles.", opaUrl, e);
            filterChain.doFilter(request, response);
            return;
        }

        if (!allowed) {
            logger.warn("OPA policy denied access for roles: {} to {} [{}]", roles, requestUri, method);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Denied: Enforced by OPA policies.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * OPA JSON response mapping structure.
     */
    public static class OpaResponse {
        private Boolean result;

        public Boolean getResult() {
            return result != null && result;
        }

        public void setResult(Boolean result) {
            this.result = result;
        }
    }
}
