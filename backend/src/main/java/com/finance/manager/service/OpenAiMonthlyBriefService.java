package com.finance.manager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finance.manager.config.OpenAiProperties;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OpenAiMonthlyBriefService {

    private static final URI OPENAI_CHAT = URI.create("https://api.openai.com/v1/chat/completions");

    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    public OpenAiMonthlyBriefService(OpenAiProperties openAiProperties, ObjectMapper objectMapper) {
        this.openAiProperties = openAiProperties;
        this.objectMapper = objectMapper;
    }

    public Optional<BriefResult> generateBrief(String apiKey, String summaryJson) {
        if (apiKey == null || apiKey.isBlank() || summaryJson == null || summaryJson.isBlank()) {
            return Optional.empty();
        }

        String system = """
                You are a personal finance coach. Given aggregated monthly stats (no raw transactions), \
                produce a concise monthly brief.
                Respond with JSON only: {"bullets":["..."],"recommendation":"..."}
                Rules: 3 to 5 bullets, each under 120 characters; one actionable recommendation under 200 characters; \
                use dollar amounts from the data; do not invent numbers not present in the input.""";

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", openAiProperties.getModel());
            ArrayNode messages = body.putArray("messages");
            ObjectNode sys = messages.addObject();
            sys.put("role", "system");
            sys.put("content", system);
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", summaryJson);
            body.put("temperature", 0.4);
            body.put("max_tokens", 450);
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
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                return Optional.empty();
            }

            JsonNode parsed = objectMapper.readTree(content);
            List<String> bullets = new ArrayList<>();
            if (parsed.path("bullets").isArray()) {
                for (JsonNode b : parsed.path("bullets")) {
                    String text = b.asText(null);
                    if (text != null && !text.isBlank()) {
                        bullets.add(text.trim());
                    }
                }
            }
            String recommendation = parsed.path("recommendation").asText("").trim();
            if (bullets.isEmpty() && recommendation.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new BriefResult(bullets, recommendation));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public record BriefResult(List<String> bullets, String recommendation) {}
}
