package com.example.backend1.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;


    public String uploadFile(MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB");
        }

        ensureBucketExists();


        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = UUID.randomUUID().toString() + "_" +
                System.currentTimeMillis() + extension;

        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        try (InputStream inputStream = file.getInputStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFilename)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            System.out.println("✅ File uploaded to MinIO: " + uniqueFilename);

        } catch (Exception e) {
            System.err.println("❌ Error uploading file to MinIO: " + e.getMessage());
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }

        String fileUrl = minioUrl + "/" + bucketName + "/" + uniqueFilename;

        System.out.println("📎 Public URL: " + fileUrl);

        return fileUrl;
    }


    private void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        if (!bucketExists) {
            System.out.println("📦 Creating bucket: " + bucketName);

            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            String policy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """.formatted(bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );

            System.out.println("✅ Bucket created and set to public: " + bucketName);
        }
    }


    public void deleteFile(String fileUrl) {
        try {

            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            System.out.println("🗑️ File deleted from MinIO: " + filename);

        } catch (Exception e) {
            System.err.println("❌ Error deleting file from MinIO: " + e.getMessage());
        }
    }
}