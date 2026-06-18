package com.finance.manager.service;

import com.finance.manager.config.EmailProperties;
import com.finance.manager.model.Bill;
import com.finance.manager.model.User;
import com.finance.manager.repository.BillRepository;
import com.finance.manager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillReminderEmailService {

    private static final Logger log = LoggerFactory.getLogger(BillReminderEmailService.class);

    private final UserRepository userRepository;
    private final BillRepository billRepository;
    private final EmailProperties emailProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public BillReminderEmailService(
            UserRepository userRepository,
            BillRepository billRepository,
            EmailProperties emailProperties) {
        this.userRepository = userRepository;
        this.billRepository = billRepository;
        this.emailProperties = emailProperties;
    }

    @Scheduled(cron = "${app.email.reminder-cron:0 0 9 * * *}")
    @Transactional
    public void sendDueBillReminders() {
        if (!emailProperties.isEnabled()) {
            return;
        }
        LocalDate today = LocalDate.now();
        for (User user : userRepository.findAll()) {
            if (!user.isBillRemindersEnabled() || user.getEmail() == null || user.getEmail().isBlank()) {
                continue;
            }
            if (user.getLastBillReminderSentAt() != null
                    && ChronoUnit.HOURS.between(user.getLastBillReminderSentAt(), LocalDateTime.now()) < 20) {
                continue;
            }
            int daysBefore = user.getBillReminderDaysBefore();
            LocalDate horizon = today.plusDays(daysBefore);
            List<Bill> dueSoon = billRepository.findByOwner_IdAndPaidFalseAndDueDateBetweenOrderByDueDateAsc(
                    user.getId(), today, horizon);
            if (dueSoon.isEmpty()) {
                continue;
            }
            String body = buildBody(dueSoon);
            if (sendEmail(user.getEmail(), "SpendSense: bills due soon", body)) {
                user.setLastBillReminderSentAt(LocalDateTime.now());
                userRepository.save(user);
            }
        }
    }

    private String buildBody(List<Bill> bills) {
        StringBuilder sb = new StringBuilder("Hi,\n\nYou have upcoming bills:\n\n");
        for (Bill bill : bills) {
            sb.append("- ").append(bill.getPayeeName())
                    .append(" $").append(bill.getAmount())
                    .append(" due ").append(bill.getDueDate())
                    .append('\n');
        }
        sb.append("\nSign in to SpendSense to review and mark them paid.\n");
        return sb.toString();
    }

    private boolean sendEmail(String to, String subject, String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(emailProperties.getResendApiKey());

            Map<String, Object> payload = new HashMap<>();
            payload.put("from", emailProperties.getFromAddress());
            payload.put("to", List.of(to));
            payload.put("subject", subject);
            payload.put("text", text);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.resend.com/emails",
                    new HttpEntity<>(payload, headers),
                    String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Bill reminder email failed for {}: {}", to, e.getMessage());
            return false;
        }
    }
}
