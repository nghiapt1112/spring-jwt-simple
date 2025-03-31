package com.example.demo.infrastructure;

import com.example.demo.infrastructure.security.BaseSecurityFilter;
import com.example.demo.infrastructure.security.JwtService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1) // Ensure this runs before JwtAuthFilter
public class RateLimitFilter extends BaseSecurityFilter {

    // Use ConcurrentHashMap for thread safety
    private final Map<String, Bucket> userRequestBuckets = new ConcurrentHashMap<>();

    // Rate limit configuration
    @Value("${rate.limit.capacity:20}")
    private int capacity;
    
    @Value("${rate.limit.refill-tokens:20}")
    private int refillTokens;
    
    @Value("${rate.limit.refill-minutes:1}")
    private int refillMinutes;

    @Autowired
    public RateLimitFilter(JwtService jwtService) {
        super(jwtService);
    }

    @Override
    protected boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/rewards/");
    }

    @Override
    protected void processFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Extract username from JWT token or auth context
        String username = extractUsername(request);

        if (username != null) {
            Bucket bucket = userRequestBuckets.computeIfAbsent(username, this::createNewBucket);
            
            if (bucket.tryConsume(1)) {
                // Request allowed, continue with filter chain
                filterChain.doFilter(request, response);
            } else {
                // Rate limit exceeded
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            }
        } else {
            // No username found in token, potentially handle as anonymous request
            filterChain.doFilter(request, response);
        }
    }

    private Bucket createNewBucket(String username) {
        Bandwidth limit = Bandwidth.classic(capacity, 
                Refill.intervally(refillTokens, Duration.ofMinutes(refillMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
