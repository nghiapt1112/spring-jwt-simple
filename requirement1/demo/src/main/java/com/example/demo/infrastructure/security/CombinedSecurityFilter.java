package com.example.demo.infrastructure.security;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Combined security filter that handles both JWT authentication and rate limiting.
 * This approach merges both security concerns into a single filter to reduce duplication.
 */
@Component
@Order(1)
public class CombinedSecurityFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    
    // Rate limiting
    private final Map<String, Bucket> userRequestBuckets = new ConcurrentHashMap<>();
    
    @Value("${rate.limit.capacity:20}")
    private int capacity;
    
    @Value("${rate.limit.refill-tokens:20}")
    private int refillTokens;
    
    @Value("${rate.limit.refill-minutes:1}")
    private int refillMinutes;

    @Autowired
    public CombinedSecurityFilter(UserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip rate limiting for non-API paths
        boolean isRewardsEndpoint = request.getRequestURI().startsWith("/rewards/");
        
        String token = extractToken(request);
        String username = null;
        
        // Extract username from token if available
        if (token != null) {
            try {
                username = jwtService.extractUsername(token);
            } catch (Exception e) {
                // Token validation failed, continue without username
            }
        }

        // Process JWT authentication if username is available and not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            processAuthentication(request, token, username);
        }
        
        // Apply rate limiting only for protected endpoints
        if (isRewardsEndpoint) {
            // Try to get authenticated username first, then fallback to token username
            String rateUsername = SecurityUtils.isAuthenticated() 
                    ? SecurityUtils.getCurrentUsername() 
                    : username;
            
            if (rateUsername != null) {
                // Apply rate limiting
                if (!processRateLimit(response, rateUsername)) {
                    // If rate limit exceeded, return early without continuing the filter chain
                    return;
                }
            }
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
    
    private void processAuthentication(HttpServletRequest request, String token, String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (jwtService.validateToken(token, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
    
    private boolean processRateLimit(HttpServletResponse response, String username) throws IOException {
        Bucket bucket = userRequestBuckets.computeIfAbsent(username, this::createNewBucket);
        
        if (bucket.tryConsume(1)) {
            return true; // Rate limit not exceeded
        } else {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            return false;
        }
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
    
    private Bucket createNewBucket(String username) {
        Bandwidth limit = Bandwidth.classic(capacity, 
                Refill.intervally(refillTokens, Duration.ofMinutes(refillMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
