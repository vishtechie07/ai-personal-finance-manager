package com.finance.manager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finance.manager.config.OpenAiProperties;
import com.finance.manager.model.Transaction;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OpenAiCategorySuggestionService {

    private static final URI OPENAI_CHAT = URI.create("https://api.openai.com/v1/chat/completions");

    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    public OpenAiCategorySuggestionService(OpenAiProperties openAiProperties, ObjectMapper objectMapper) {
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
    }

    public Optional<Transaction.Category> suggestCategory(String apiKey, String description) {
        if (apiKey == null || apiKey.isBlank() || description == null || description.isBlank()) {
            return Optional.empty();
        }

        String allowed = Arrays.stream(Transaction.Category.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        String system = """
                You classify personal finance transaction descriptions into exactly one category.
                Respond with JSON only: {"category":"<ENUM>"}
                Where <ENUM> must be exactly one of: %s
                If unclear, use OTHER.""".formatted(allowed);

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", openAiProperties.getModel());

            ArrayNode messages = body.putArray("messages");
            ObjectNode sys = messages.addObject();
            sys.put("role", "system");
            sys.put("content", system);
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", description.trim());

            body.put("temperature", 0.2);
            body.put("max_tokens", 80);
            ObjectNode rf = body.putObject("response_format");
            rf.put("type", "json_object");

            String json = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(OPENAI_CHAT)
                    .timeout(Duration.ofSeconds(45))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                return Optional.empty();
            }

            JsonNode parsed = objectMapper.readTree(content);
            String raw = parsed.path("category").asText(null);
            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }
            try {
                return Optional.of(Transaction.Category.valueOf(raw.trim()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
