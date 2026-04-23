package com.example.backend1.controller;

import com.example.backend1.service.AiPosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    @PostMapping("/enhance-description")
    public ResponseEntity<Map<String, String>> enhanceDescription(@RequestBody Map<String, String> body) {
        String roughDescription = body.get("description");

        if (roughDescription == null || roughDescription.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Description is empty"));
        }

        String prompt = """
                You are helping an event organizer in Tunisia write a professional event description.
                Rewrite the following rough description into a clear, engaging, and professional event description
                in the same language it was written in (French or English).
                Keep it under 200 words. Return ONLY the improved description, nothing else.
                
                Rough description: "%s"
                """.formatted(roughDescription);

        Map<String, Object> requestBody = Map.of(
                "model", "openrouter/free",
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openRouterApiKey);
        headers.set("HTTP-Referer", "http://localhost:4200");
        headers.set("X-Title", "Invitini");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://openrouter.ai/api/v1/chat/completions",
                    request,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> responseBody = mapper.readValue(response.getBody(), Map.class);
            List<?> choices = (List<?>) responseBody.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            String improvedText = (String) message.get("content");

            return ResponseEntity.ok(Map.of("improved", improvedText));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed: " + e.getMessage()));
        }
    }
    @Autowired
    private AiPosterService aiPosterService;

    @PostMapping("/generate-poster")
    public ResponseEntity<Map<String, String>> generatePoster(@RequestBody Map<String, String> body) {
        String title       = body.getOrDefault("title", "").trim();
        String description = body.getOrDefault("description", "").trim();
        String category    = body.getOrDefault("category", "");
        String style       = body.getOrDefault("style", "");

        if (title.isEmpty() || description.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "title and description are required"));
        }

        try {
            String posterUrl = aiPosterService.generateAndUploadPoster(title, description, category, style);
            return ResponseEntity.ok(Map.of("url", posterUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Poster generation failed: " + e.getMessage()));
        }
    }
}