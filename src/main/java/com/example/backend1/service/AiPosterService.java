package com.example.backend1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.List;
import java.util.Map;

@Service
public class AiPosterService {

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    private final MinioService minioService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public AiPosterService(MinioService minioService) {
        this.minioService = minioService;
    }

    public String generateAndUploadPoster(String title, String description,
                                          String category, String style) throws Exception {

        String safeStyle    = (style    != null && !style.isBlank())    ? style    : "modern elegant";
        String safeCategory = (category != null && !category.isBlank()) ? category : "event";

        String prompt = String.format(
                "Abstract artistic background for a %s event, theme: %s, style: %s, " +
                        "vibrant colors, cinematic lighting, high quality, " +
                        "NO text, NO letters, NO words, NO writing, purely visual",
                safeCategory,
                description.length() > 80 ? description.substring(0, 80) : description,
                safeStyle
        );

        String encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8");
        String imageUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt
                + "?width=1024&height=1024&nologo=true";

        byte[] imageBytes = new java.net.URL(imageUrl).openStream().readAllBytes();

        byte[] finalBytes = overlayText(imageBytes, title, safeCategory);

        PosterMultipartFile multipartFile = new PosterMultipartFile(finalBytes);
        return minioService.uploadFile(multipartFile);
    }

    private byte[] overlayText(byte[] imageBytes, String title, String category) throws Exception {
        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(
                new java.io.ByteArrayInputStream(imageBytes)
        );

        int w = img.getWidth();
        int h = img.getHeight();

        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                0, h * 0.55f, new java.awt.Color(0, 0, 0, 0),
                0, h,         new java.awt.Color(0, 0, 0, 200)
        );
        g.setPaint(gradient);
        g.fillRect(0, (int)(h * 0.55), w, (int)(h * 0.45));

        g.setColor(new java.awt.Color(255, 180, 220));
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 28));
        String catLabel = category.toUpperCase();
        int catX = 60;
        int catY = (int)(h * 0.75);
        g.drawString(catLabel, catX, catY);

        g.setColor(java.awt.Color.WHITE);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 62));
        java.awt.FontMetrics fm = g.getFontMetrics();

        String[] words = title.split(" ");
        StringBuilder line = new StringBuilder();
        int titleY = catY + 75;
        int lineHeight = fm.getHeight();

        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > w - 120) {
                g.drawString(line.toString(), catX, titleY);
                titleY += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            g.drawString(line.toString(), catX, titleY);
        }

        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 22));
        g.setColor(new java.awt.Color(255, 255, 255, 160));
        String brand = "invitini.tn";
        int brandW = g.getFontMetrics().stringWidth(brand);
        g.drawString(brand, w - brandW - 40, h - 30);

        g.dispose();

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", out);
        return out.toByteArray();
    }
}