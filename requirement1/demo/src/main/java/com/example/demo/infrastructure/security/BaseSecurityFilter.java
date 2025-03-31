package com.example.demo.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Base filter that provides common functionality for security filters.
 * Helps to reduce code duplication between different security filters.
 */
public abstract class BaseSecurityFilter extends OncePerRequestFilter {

    protected JwtService jwtService;

    public BaseSecurityFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // If path is excluded, skip filter processing
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Process the filter logic
        processFilter(request, response, filterChain);
    }

    /**
     * Determine if the filter should be skipped for this request
     * @param request The HTTP request
     * @return true if the filter should be skipped
     */
    protected abstract boolean shouldSkip(HttpServletRequest request);

    /**
     * Process the filter logic
     * @param request The HTTP request
     * @param response The HTTP response
     * @param filterChain The filter chain
     * @throws ServletException if an error occurs
     * @throws IOException if an I/O error occurs
     */
    protected abstract void processFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException;

    /**
     * Extract JWT token from the Authorization header
     * @param request The HTTP request
     * @return The JWT token, or null if not present
     */
    protected String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }

    /**
     * Extract username from JWT token or security context
     * @param request The HTTP request
     * @return The username, or null if not available
     */
    protected String extractUsername(HttpServletRequest request) {
        // First try to get username from authentication context
        if (SecurityUtils.isAuthenticated()) {
            return SecurityUtils.getCurrentUsername();
        }
        
        // Fall back to extracting from token
        String token = extractToken(request);
        if (token != null) {
            try {
                return jwtService.extractUsername(token);
            } catch (Exception e) {
                // Token validation failed, return null
                return null;
            }
        }
        return null;
    }
}
