package com.example.demo.infrastructure;

import com.example.demo.infrastructure.security.JwtAuthFilter;
import com.example.demo.infrastructure.security.UserIdAuthorizationFilter;
// Uncomment this to use the combined filter approach
// import com.example.demo.security.CombinedSecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ===== For separate filters approach =====
    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    
    @Autowired
    private RateLimitFilter rateLimitFilter;
    
    @Autowired
    private UserIdAuthorizationFilter userIdAuthorizationFilter;

    // ===== For combined filter approach =====
    // Uncomment this and comment out the three filters above to use the combined approach
    // @Autowired
    // private CombinedSecurityFilter combinedSecurityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Use the new non-deprecated method for CSRF configuration
            .csrf(csrf -> csrf.disable())
            
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints
                .requestMatchers("/login").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Protected endpoints - require authentication
                .requestMatchers("/rewards/**").authenticated()
                
                // Default fallback rule
                .anyRequest().authenticated()
            )
            // Configure session management to be stateless (for JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        // ===== For separate filters approach =====
        http
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtAuthFilter, RateLimitFilter.class)
            .addFilterAfter(userIdAuthorizationFilter, JwtAuthFilter.class);
            
        // ===== For combined filter approach =====
        // Uncomment this and comment out the three filters above to use the combined approach
        // http.addFilterBefore(combinedSecurityFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
