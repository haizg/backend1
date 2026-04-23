package com.example.backend1.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.*;

public class PosterMultipartFile implements MultipartFile {
    private final byte[] content;

    public PosterMultipartFile(byte[] content) {
        this.content = content;
    }

    @Override public String getName()             { return "poster"; }
    @Override public String getOriginalFilename() { return "poster.png"; }
    @Override public String getContentType()      { return "image/png"; }
    @Override public boolean isEmpty()            { return content.length == 0; }
    @Override public long getSize()               { return content.length; }
    @Override public byte[] getBytes()            { return content; }
    @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
    }
}