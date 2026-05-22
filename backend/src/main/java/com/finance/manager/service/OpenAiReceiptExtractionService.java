package com.finance.manager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finance.manager.config.OpenAiProperties;
import com.finance.manager.config.StorageProperties;
import com.finance.manager.util.UploadFileValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OpenAiReceiptExtractionService {

    private static final URI OPENAI_CHAT = URI.create("https://api.openai.com/v1/chat/completions");

    private final OpenAiProperties openAiProperties;
    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;

    public OpenAiReceiptExtractionService(
            OpenAiProperties openAiProperties,
            StorageProperties storageProperties,
            ObjectMapper objectMapper) {
        this.openAiProperties = openAiProperties;
        this.storageProperties = storageProperties;
        this.objectMapper = objectMapper;
    }

    public Optional<Map<String, Object>> extract(String apiKey, MultipartFile file) {
        if (apiKey == null || apiKey.isBlank() || file == null || file.isEmpty()) {
            return Optional.empty();
        }
        UploadFileValidator.validateReceiptFile(file, storageProperties.getMaxFileSize());
        try {
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String dataUrl = "data:" + contentType + ";base64," + base64;

            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", openAiProperties.getModel());

            ArrayNode messages = body.putArray("messages");
            ObjectNode sys = messages.addObject();
            sys.put("role", "system");
            sys.put("content", """
                    Extract receipt fields from the image. Respond with JSON only:
                    {"description":"merchant or summary","amount":0.00,"date":"YYYY-MM-DD or null"}
                    amount must be a positive number. Use null for unknown date.""");

            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            ArrayNode content = userMsg.putArray("content");
            ObjectNode textPart = content.addObject();
            textPart.put("type", "text");
            textPart.put("text", "Extract transaction details from this receipt.");
            ObjectNode imagePart = content.addObject();
            imagePart.put("type", "image_url");
            ObjectNode imageUrl = imagePart.putObject("image_url");
            imageUrl.put("url", dataUrl);

            body.put("temperature", 0.1);
            body.put("max_tokens", 300);
            ObjectNode rf = body.putObject("response_format");
            rf.put("type", "json_object");

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(OPENAI_CHAT)
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            String raw = root.path("choices").path(0).path("message").path("content").asText(null);
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            JsonNode parsed = objectMapper.readTree(raw);
            Map<String, Object> result = new HashMap<>();
            if (parsed.hasNonNull("description")) {
                result.put("description", parsed.get("description").asText());
            }
            if (parsed.has("amount")) {
                result.put("amount", parsed.get("amount").asDouble());
            }
            if (parsed.hasNonNull("date") && !parsed.get("date").asText().equals("null")) {
                result.put("date", parsed.get("date").asText());
            }
            return result.isEmpty() ? Optional.empty() : Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
