package com.finance.manager.controller;

import com.finance.manager.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private Map<String, User> defaultUsers;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }
        
        User user = defaultUsers.get(username);
        if (user != null && user.getPassword().equals(password)) {
            // In a real app, you'd hash the password and use JWT tokens
            Map<String, Object> response = Map.of(
                "token", "demo-token-" + username,
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole()
                )
            );
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
    }
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid authorization header"));
        }
        
        String token = authHeader.substring(7);
        // Simple token validation for demo purposes
        if (token.startsWith("demo-token-")) {
            String username = token.substring(11);
            User user = defaultUsers.get(username);
            if (user != null) {
                Map<String, Object> userInfo = Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole()
                );
                return ResponseEntity.ok(userInfo);
            }
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
    }
}
