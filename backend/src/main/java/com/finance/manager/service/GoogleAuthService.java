package com.finance.manager.service;

import com.finance.manager.model.User;
import com.finance.manager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final UserRepository userRepository;
    private final DemoSeedService demoSeedService;
    private final RegistrationAbuseService registrationAbuseService;

    public GoogleAuthService(
            GoogleTokenVerifierService googleTokenVerifierService,
            UserRepository userRepository,
            DemoSeedService demoSeedService,
            RegistrationAbuseService registrationAbuseService) {
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.userRepository = userRepository;
        this.demoSeedService = demoSeedService;
        this.registrationAbuseService = registrationAbuseService;
    }

    @Transactional
    public User authenticate(String idToken, String clientIp) {
        GoogleTokenVerifierService.GoogleProfile profile = googleTokenVerifierService.verify(idToken);

        Optional<User> byGoogle = userRepository.findByGoogleSub(profile.sub());
        if (byGoogle.isPresent()) {
            return byGoogle.get();
        }

        Optional<User> byEmail = userRepository.findByEmail(profile.email());
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            if (existing.getGoogleSub() != null && !existing.getGoogleSub().equals(profile.sub())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already linked to another account");
            }
            existing.setGoogleSub(profile.sub());
            existing.setAuthProvider(User.AuthProvider.GOOGLE);
            if (profile.givenName() != null && !profile.givenName().isBlank()) {
                existing.setFirstName(profile.givenName());
            }
            if (profile.familyName() != null && !profile.familyName().isBlank()) {
                existing.setLastName(profile.familyName());
            }
            return userRepository.save(existing);
        }

        Optional<String> ipLimit = registrationAbuseService.validateNewAccountFromIp(clientIp);
        if (ipLimit.isPresent()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, ipLimit.get());
        }

        User user = new User();
        user.setGoogleSub(profile.sub());
        user.setAuthProvider(User.AuthProvider.GOOGLE);
        user.setEmail(profile.email());
        user.setUsername(uniqueUsername(profile.email(), profile.sub()));
        user.setPassword(null);
        user.setFirstName(profile.givenName());
        user.setLastName(profile.familyName());
        user.setRole(User.Role.USER);

        User saved = userRepository.save(user);
        demoSeedService.seedDemoFinancialsIfEmpty(saved);
        return saved;
    }

    private String uniqueUsername(String email, String googleSub) {
        String base = email.contains("@") ? email.substring(0, email.indexOf('@')) : "user";
        base = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
        if (base.length() < 3) {
            base = "user";
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String candidate = base;
        int attempt = 0;
        while (userRepository.existsByUsername(candidate)) {
            attempt++;
            String suffix = attempt < 10 ? String.valueOf(attempt) : UUID.randomUUID().toString().substring(0, 6);
            candidate = base + suffix;
            if (candidate.length() > 50) {
                candidate = "g" + googleSub.substring(0, Math.min(8, googleSub.length())) + suffix;
            }
        }
        return candidate;
    }
}
