package com.finance.manager.controller;

import com.finance.manager.config.AuthProperties;
import com.finance.manager.config.GoogleProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/config")
public class PublicConfigController {

    private final GoogleProperties googleProperties;
    private final AuthProperties authProperties;

    public PublicConfigController(GoogleProperties googleProperties, AuthProperties authProperties) {
        this.googleProperties = googleProperties;
        this.authProperties = authProperties;
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicConfig() {
        return ResponseEntity.ok(Map.of(
                "googleSignInEnabled", googleProperties.isEnabled(),
                "googleClientId", googleProperties.isEnabled() ? googleProperties.getClientId() : "",
                "registrationEnabled", authProperties.isRegistrationEnabled()));
    }
}
