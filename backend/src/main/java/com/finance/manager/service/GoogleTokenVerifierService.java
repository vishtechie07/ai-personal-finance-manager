package com.finance.manager.service;

import com.finance.manager.config.GoogleProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Service
public class GoogleTokenVerifierService {

    private final GoogleProperties googleProperties;

    public GoogleTokenVerifierService(GoogleProperties googleProperties) {
        this.googleProperties = googleProperties;
    }

    private GoogleIdTokenVerifier verifier() {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleProperties.getClientId()))
                .build();
    }

    public record GoogleProfile(String sub, String email, boolean emailVerified, String givenName, String familyName) {}

    public GoogleProfile verify(String idTokenString) {
        if (!googleProperties.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google sign-in is not configured");
        }
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google credential is required");
        }
        try {
            GoogleIdToken idToken = verifier().verify(idTokenString);
            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google credential");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            if (!payload.getEmailVerified()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google email must be verified");
            }
            String email = payload.getEmail();
            String sub = payload.getSubject();
            if (email == null || email.isBlank() || sub == null || sub.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google profile incomplete");
            }
            return new GoogleProfile(
                    sub,
                    email.toLowerCase(),
                    Boolean.TRUE.equals(payload.getEmailVerified()),
                    (String) payload.get("given_name"),
                    (String) payload.get("family_name"));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google credential");
        }
    }
}
