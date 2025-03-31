package com.example.demo.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for security operations.
 * Centralizes all interactions with SecurityContextHolder and provides
 * common security-related helper methods.
 */
public class SecurityUtils {
    
    /**
     * Get the current authentication object
     * @return The authentication object or null if not authenticated
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * Check if the current user is authenticated
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * Get the current authenticated username
     * @return Username or null if not authenticated
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
    
    /**
     * Check if the current user has the admin role
     * @return true if the user has admin role, false otherwise
     */
    public static boolean isAdmin() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
    
    /**
     * Check if the current user has a specific role
     * @param role The role to check for (without the "ROLE_" prefix)
     * @return true if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }
    
    /**
     * Check if the current user has any of the specified authorities
     * @param authorities The authorities to check for
     * @return true if the user has any of the authorities, false otherwise
     */
    public static boolean hasAnyAuthority(String... authorities) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        for (String authority : authorities) {
            for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
                if (authority.equals(grantedAuthority.getAuthority())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if the authenticated user is the same as the requested user
     * @param userId The user ID to check against
     * @return true if it's the same user or if the current user is an admin, false otherwise
     */
    public static boolean isUserOrAdmin(String userId) {
        if (!isAuthenticated()) {
            return false;
        }
        
        String currentUsername = getCurrentUsername();
        return isAdmin() || (currentUsername != null && currentUsername.equals(userId));
    }
}
