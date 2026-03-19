package com.finance.manager.controller;

import com.finance.manager.model.User;
import com.finance.manager.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid LoginRequest loginRequest) {
        if (userRepository.existsByUsername(loginRequest.username())) {
            User user = userRepository.findByUsername(loginRequest.username()).orElse(null);
            if (user != null && user.getPassword().equals(loginRequest.password())) {
                return ResponseEntity.ok(Map.of(
                    "token", "demo-token-" + user.getUsername(),
                    "user", toUserInfo(user)
                ));
            }
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
    }
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid authorization header"));
        }
        
        String token = authHeader.substring(7);
        if (token.startsWith("demo-token-")) {
            String username = token.substring(11);
            return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(toUserInfo(user)))
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Invalid token")));
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(User.Role.USER);

        User saved = userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "token", "demo-token-" + saved.getUsername(),
            "user", toUserInfo(saved)
        ));
    }

    private Map<String, Object> toUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("firstName", user.getFirstName());
        info.put("lastName", user.getLastName());
        info.put("role", user.getRole());
        return info;
    }

    public record LoginRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        String password
    ) {}

    public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        String firstName,
        String lastName
    ) {}
}
