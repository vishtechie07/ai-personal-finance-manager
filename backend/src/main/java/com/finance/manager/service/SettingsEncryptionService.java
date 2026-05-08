package com.finance.manager.service;

import com.finance.manager.config.SettingsProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class SettingsEncryptionService {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LENGTH = 12;

    private final SettingsProperties settingsProperties;
    private SecretKey aesKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public SettingsEncryptionService(SettingsProperties settingsProperties) {
        this.settingsProperties = settingsProperties;
    }

    @PostConstruct
    void init() throws Exception {
        String raw = settingsProperties.getEncryptionKey();
        if (raw.length() < 32) {
            throw new IllegalStateException(
                    "app.settings.encryption-key must be at least 32 characters (use SETTINGS_ENCRYPTION_KEY in production).");
        }
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
        byte[] keyBytes = Arrays.copyOf(digest, 32);
        aesKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String combined) {
        if (combined == null || combined.isEmpty()) {
            return null;
        }
        try {
            int sep = combined.indexOf(':');
            if (sep < 0) {
                return null;
            }
            byte[] iv = Base64.getDecoder().decode(combined.substring(0, sep));
            byte[] cipherBytes = Base64.getDecoder().decode(combined.substring(sep + 1));
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
