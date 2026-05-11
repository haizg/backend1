package com.example.backend1.controller;
import com.example.backend1.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:4200")
public class FileUploadController {
    @Autowired
    private MinioService minioService;
    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Only image files are allowed"));
            }
            String fileUrl = minioService.uploadFile(file);
            Map<String, Object> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("message", "File uploaded successfully");
            response.put("filename", file.getOriginalFilename());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            System.err.println(" Upload error: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }
}


