package com.example.demo.service;

import com.example.demo.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private Map<String, User> users = new HashMap<>();

    public CustomUserDetailsService() {
        // In-memory users with password encoded
        users.put("user123", new User("user123", "$2a$10$O2i4nHWGDQ9tPqznQXDa5OsHAB.tBIHPM2C0vV2qtsR5ACWC22qgO")); // password: password
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return users.get(username);
    }
}
