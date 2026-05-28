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
                Rewrite the following rough description into a clear, engaging and professional event description
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

            if (improvedText == null || improvedText.isBlank()) {
                return ResponseEntity.status(500).body(Map.of("error", "Empty AI response"));
            }

            return ResponseEntity.ok(Map.of("improved", improvedText.trim()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed: " + e.getMessage()));
        }
    }
    @Autowired
    private AiPosterService aiPosterService;

    @PostMapping("/generate-poster")
    public ResponseEntity<Map<String, String>> generatePoster(@RequestBody Map<String, String> body) {
        String title= body.getOrDefault("title", "").trim();
        String description = body.getOrDefault("description", "").trim();
        String category= body.getOrDefault("category", "");
        String style= body.getOrDefault("style", "");

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

            content = content.trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            }

            List<?> recommendations;

            try {
                recommendations = mapper.readValue(content, List.class);
            } catch (Exception ex) {
                System.out.println("Invalid JSON from AI:");
                System.out.println(content);

                return ResponseEntity.status(500).body(
                        Map.of("error", "AI returned invalid JSON", "raw", content)
                );
            }

            return ResponseEntity.ok(
                    Map.of("recommendations", recommendations)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed: " + e.getMessage()));
        }
    }

    @PostMapping("/analyze-event-risk")
    public ResponseEntity<Map<String, Object>> analyzeRisk(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String description = body.get("description");
        String location = body.get("location");

        String prompt = """
        You are an AI system that detects suspicious or fake events.
    
        Analyze the event below and assign a risk score from 0 to 100:
        - 0 = completely safe and normal
        - 100 = highly suspicious / fake / scam
    
        Also give a short reason (max 15 words).
    
        Event:
        Title: %s
        Description: %s
        Location: %s
    
        Respond ONLY in JSON format:
        {
          "riskScore": number,
          "reason": "short explanation in FRENSH"
        }
        """.formatted(title, description, location);

        Map<String, Object> requestBody = Map.of(
                "model", "openrouter/free",
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openRouterApiKey);

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
            String content = ((String) message.get("content")).trim();

            if (content.startsWith("```")) {
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            }

            Map<String, Object> result = mapper.readValue(content, Map.class);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Risk analysis failed"));
        }
    }

    @PostMapping("/predict-participation")
    public ResponseEntity<Map<String, Object>> predictParticipation(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String description = body.get("description");
        String category = body.get("category");
        String location = body.get("location");
        String date = body.get("date");
        String maxParticipants = body.get("maxParticipants");

        String prompt = """
            You are an AI system that predicts event participation rates for a Tunisian event platform.
            
            Analyze the event below and predict participation level:
            - LOW: less than 40%% of capacity likely to register
            - MEDIUM: 40-70%% of capacity likely to register
            - HIGH: more than 70%% of capacity likely to register
            
            Consider: category appeal, location, date, description quality, capacity size.
            
            Event:
            Title: %s
            Category: %s
            Description: %s
            Location: %s
            Date: %s
            Max Participants: %s
            
            Respond ONLY in JSON format:
            {
              "level": "LOW" or "MEDIUM" or "HIGH",
              "reason": "short explanation max 15 words IN FRENSH"
            }
            """.formatted(title, category, description, location, date, maxParticipants);

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
            String content = ((String) message.get("content")).trim();

            if (content.startsWith("```")) {
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            }

            Map<String, Object> result = mapper.readValue(content, Map.class);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Prediction failed"));
        }
    }

}