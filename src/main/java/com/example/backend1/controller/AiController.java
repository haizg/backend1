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

    @PostMapping("/recommend-events")
    public ResponseEntity<Map<String, Object>> recommendEvents(@RequestBody Map<String, Object> body) {
        String userHistory = (String) body.get("userHistory");
        String availableEvents = (String) body.get("availableEvents");

        if (userHistory == null || availableEvents == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing data"));
        }

        String prompt = """
            You are an event recommendation assistant for Invitini, a Tunisian cultural and educational event platform.
            
            The user has previously participated in these events:
            %s
            
            Here are the available upcoming events they haven't joined yet:
            %s
            
            Based on the user's interests shown by their history, recommend the 3 most relevant events.
            
            Respond ONLY with a valid JSON array, no explanation, no markdown, no backticks. Example format:
            [
              {"id": 1, "reason": "Short reason in the same language as the event title"},
              {"id": 2, "reason": "Short reason"},
              {"id": 3, "reason": "Short reason"}
            ]
            
            If the user has no history, recommend 3 events based on popularity and variety.
            Return exactly 3 items. Only include IDs from the provided available events list.
            """.formatted(userHistory, availableEvents);

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
            String content = (String) message.get("content");

            // clean response in case ai adds backticks
            content = content.trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            }

            List<?> recommendations = mapper.readValue(content, List.class);
            return ResponseEntity.ok(Map.of("recommendations", recommendations));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed: " + e.getMessage()));
        }
    }




}