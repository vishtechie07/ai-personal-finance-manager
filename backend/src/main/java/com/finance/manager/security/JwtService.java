package com.finance.manager.security;

import com.finance.manager.config.JwtProperties;
import com.finance.manager.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final String CLAIM_UID = "uid";

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void init() {
        String secret = jwtProperties.getSecret();
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least 32 characters (configure JWT_SECRET in production).");
        }
        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        long expMs = jwtProperties.getExpirationMs();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expMs);
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_UID, user.getId())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Optional<AuthPrincipal> parseToken(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(bearerToken)
                    .getPayload();
            String username = claims.getSubject();
            Long uid = claims.get(CLAIM_UID, Long.class);
            if (username == null || uid == null) {
                return Optional.empty();
            }
            return Optional.of(new AuthPrincipal(uid, username));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
