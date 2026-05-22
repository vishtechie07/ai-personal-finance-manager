package com.finance.manager.controller;

import com.finance.manager.config.AuthProperties;
import com.finance.manager.model.User;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.DemoSeedService;
import com.finance.manager.service.GoogleAuthService;
import com.finance.manager.service.RegistrationAbuseService;
import com.finance.manager.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DemoSeedService demoSeedService;
    private final GoogleAuthService googleAuthService;
    private final AuthProperties authProperties;
    private final RegistrationAbuseService registrationAbuseService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            DemoSeedService demoSeedService,
            GoogleAuthService googleAuthService,
            AuthProperties authProperties,
            RegistrationAbuseService registrationAbuseService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.demoSeedService = demoSeedService;
        this.googleAuthService = googleAuthService;
        this.authProperties = authProperties;
        this.registrationAbuseService = registrationAbuseService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username()).orElse(null);
        if (user == null || !passwordMatches(user, loginRequest.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        return ResponseEntity.ok(tokenResponse(user));
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> googleSignIn(
            @RequestBody @Valid GoogleSignInRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = ClientIpResolver.resolve(httpRequest);
        User user = googleAuthService.authenticate(request.credential(), clientIp);
        return ResponseEntity.ok(tokenResponse(user));
    }

    private boolean passwordMatches(User user, String rawPassword) {
        if (user.getAuthProvider() == User.AuthProvider.GOOGLE && user.getPassword() == null) {
            return false;
        }
        String stored = user.getPassword();
        return stored != null && stored.startsWith("$2") && passwordEncoder.matches(rawPassword, stored);
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
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest) {
        if (!authProperties.isRegistrationEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Registration is disabled. Sign in with Google instead."));
        }
        String clientIp = ClientIpResolver.resolve(httpRequest);
        Optional<String> validationError = registrationAbuseService.validateRegistration(
                request.username(), request.email());
        if (validationError.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", validationError.get()));
        }
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }
        Optional<String> ipLimitError = registrationAbuseService.validateNewAccountFromIp(clientIp);
        if (ipLimitError.isPresent()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", ipLimitError.get()));
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(User.Role.USER);
        user.setAuthProvider(User.AuthProvider.LOCAL);

        User saved = userRepository.save(user);
        demoSeedService.seedDemoFinancialsIfEmpty(saved);

        return ResponseEntity.ok(tokenResponse(saved));
    }

    private Map<String, Object> tokenResponse(User user) {
        Map<String, Object> body = new HashMap<>();
        body.put("token", jwtService.generateToken(user));
        body.put("user", toUserInfo(user));
        return body;
    }

    private Map<String, Object> toUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("firstName", user.getFirstName());
        info.put("lastName", user.getLastName());
        info.put("role", user.getRole());
        info.put("authProvider", user.getAuthProvider());
        return info;
    }

    public record LoginRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record GoogleSignInRequest(
            @NotBlank(message = "Google credential is required")
            String credential
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
