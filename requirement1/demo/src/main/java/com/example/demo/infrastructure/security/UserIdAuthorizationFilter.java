package com.example.demo.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to enforce that users can only access their own reward data.
 * This replaces the @PreAuthorize annotations with filter-based security.
 */
@Component
public class UserIdAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Only apply to /rewards/* endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/rewards/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get the userId from the request parameters
        String userId = request.getParameter("userId");
        if (userId == null || userId.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check if user is authenticated
        if (!SecurityUtils.isAuthenticated()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required\"}");
            return;
        }
        
        // Either the user is accessing their own data or they're an admin
        if (SecurityUtils.isUserOrAdmin(userId)) {
            filterChain.doFilter(request, response);
        } else {
            // Access denied - user is trying to access another user's data
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Access denied: You can only access your own data\"}");
        }
    }
}
