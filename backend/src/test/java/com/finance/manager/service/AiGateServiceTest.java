package com.finance.manager.service;

import com.finance.manager.config.AiProperties;
import com.finance.manager.model.User;
import com.finance.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiGateServiceTest {

    @Mock
    private OpenAiKeyResolver openAiKeyResolver;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SlidingWindowRateLimiter rateLimiter;

    private AiProperties aiProperties;
    private AiGateService aiGateService;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        aiProperties.setPlatformEnabled(true);
        aiProperties.setPlatformTrialMinutes(5);
        aiProperties.setPlatformMinAccountAgeHours(0);
        aiGateService = new AiGateService(aiProperties, openAiKeyResolver, userRepository, rateLimiter);
    }

    @Test
    void platformTrialActiveWithinWindow() {
        User user = new User();
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(openAiKeyResolver.isPlatformKeyConfigured()).thenReturn(true);

        AiGateService.PlatformTrialStatus status = aiGateService.getPlatformTrialStatus(1L, false);

        assertTrue(status.trialActive());
        assertFalse(status.trialExpired());
        assertTrue(aiGateService.isAiAvailableForUser(1L, false));
    }

    @Test
    void platformTrialExpiredAfterWindow() {
        User user = new User();
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(openAiKeyResolver.isPlatformKeyConfigured()).thenReturn(true);
        when(openAiKeyResolver.resolveForUser(1L))
                .thenReturn(Optional.of(new OpenAiKeyResolver.ResolvedKey("sk-test", OpenAiKeyResolver.ResolvedKey.Source.PLATFORM)));

        AiGateService.PlatformTrialStatus status = aiGateService.getPlatformTrialStatus(1L, false);

        assertFalse(status.trialActive());
        assertTrue(status.trialExpired());
        assertFalse(aiGateService.isAiAvailableForUser(1L, false));

        AiGateService.AccessResult result =
                aiGateService.evaluate(1L, "newuser", AiGateService.Operation.CATEGORY, "coffee shop");
        assertInstanceOf(AiGateService.AccessResult.Denied.class, result);
        assertEquals("platform_trial_expired", ((AiGateService.AccessResult.Denied) result).source());
    }

    @Test
    void userKeyBypassesTrialExpiry() {
        assertTrue(aiGateService.isAiAvailableForUser(1L, true));
        AiGateService.PlatformTrialStatus status = aiGateService.getPlatformTrialStatus(1L, true);
        assertFalse(status.trialActive());
        assertFalse(status.trialExpired());
    }
}
