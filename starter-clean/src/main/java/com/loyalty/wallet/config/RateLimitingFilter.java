package com.loyalty.wallet.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Cache of rate limiters, one per user
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Get user identifier (username if authenticated, IP address otherwise)
        String user = request.getUserPrincipal() != null ? 
                      request.getUserPrincipal().getName() : 
                      request.getRemoteAddr();
        
        // Get or create bucket for this user
        Bucket bucket = buckets.computeIfAbsent(user, this::createNewBucket);
        
        // Try to consume a token
        if (bucket.tryConsume(1)) {
            // Allow the request
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Please try again later.\"}");
        }
    }
    
    private Bucket createNewBucket(String user) {
        // Define bandwidth limit: 10 requests per second
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofSeconds(1)));
        
        // Create a new bucket with the specified limit
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
