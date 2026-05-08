package com.finance.manager.service;

import com.finance.manager.model.User;
import com.finance.manager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserOpenAiSettingsService {

    private final UserRepository userRepository;
    private final SettingsEncryptionService encryptionService;

    public UserOpenAiSettingsService(UserRepository userRepository, SettingsEncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    public boolean hasApiKey(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getOpenaiApiKeyEncrypted() != null && !u.getOpenaiApiKeyEncrypted().isBlank())
                .orElse(false);
    }

    @Transactional
    public void saveApiKey(Long userId, String apiKey) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setOpenaiApiKeyEncrypted(encryptionService.encrypt(apiKey.trim()));
        userRepository.save(user);
    }

    @Transactional
    public void clearApiKey(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setOpenaiApiKeyEncrypted(null);
        userRepository.save(user);
    }

    public Optional<String> getDecryptedApiKey(Long userId) {
        return userRepository.findById(userId)
                .map(User::getOpenaiApiKeyEncrypted)
                .filter(s -> s != null && !s.isBlank())
                .map(encryptionService::decrypt)
                .filter(k -> k != null && !k.isBlank());
    }
}
