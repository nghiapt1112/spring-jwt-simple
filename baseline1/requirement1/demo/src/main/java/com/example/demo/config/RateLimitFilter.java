package com.example.demo.config;

import com.example.demo.security.JwtService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    // Use ConcurrentHashMap for thread safety
    private final Map<String, Bucket> userRequestBuckets = new ConcurrentHashMap<>();

    // Default rate limit: 20 requests per minute
    private static final int DEFAULT_CAPACITY = 20;
    private static final int DEFAULT_REFILL_TOKENS = 20;
    private static final int DEFAULT_REFILL_MINUTES = 1;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Skip rate limiting for non-API paths (e.g., /login)
        String path = request.getRequestURI();
        if (!path.startsWith("/rewards/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract username from JWT token
        String username = extractUsernameFromToken(request);

        if (username != null) {
            Bucket bucket = userRequestBuckets.computeIfAbsent(username, this::createNewBucket);
            
            if (bucket.tryConsume(1)) {
                // Request allowed, continue with filter chain
                filterChain.doFilter(request, response);
            } else {
                // Rate limit exceeded
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Rate limit exceeded. Please try again later.\"}");
            }
        } else {
            // No username found in token, potentially handle as anonymous request
            // For now, just proceed with filter chain
            filterChain.doFilter(request, response);
        }
    }

    private Bucket createNewBucket(String username) {
        long overdraft = 0;
        Bandwidth limit = Bandwidth.classic(DEFAULT_CAPACITY, 
                Refill.intervally(DEFAULT_REFILL_TOKENS, Duration.ofMinutes(DEFAULT_REFILL_MINUTES)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String extractUsernameFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
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
