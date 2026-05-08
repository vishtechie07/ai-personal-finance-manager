package com.finance.manager.controller;

import com.finance.manager.model.User;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.security.AuthPrincipal;
import com.finance.manager.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username()).orElse(null);
        if (user == null || !passwordMatches(user, loginRequest.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtService.generateToken(user);
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("user", toUserInfo(user));
        return ResponseEntity.ok(body);
    }

    private boolean passwordMatches(User user, String rawPassword) {
        String stored = user.getPassword();
        if (stored != null && stored.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        if (stored != null && stored.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal AuthPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userRepository.findById(principal.userId())
                .map(user -> ResponseEntity.ok(toUserInfo(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
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
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(User.Role.USER);

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("user", toUserInfo(saved));
        return ResponseEntity.ok(body);
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
            @Size(min = 8, max = 128, message = "Password must be at least 8 characters")
            String password,

            String firstName,
            String lastName
    ) {}
}
