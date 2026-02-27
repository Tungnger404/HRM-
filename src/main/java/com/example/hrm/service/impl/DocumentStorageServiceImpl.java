package com.example.hrm.service.impl;

import com.example.hrm.service.DocumentStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class DocumentStorageServiceImpl implements DocumentStorageService {

    private final Path root;

    public DocumentStorageServiceImpl(@Value("${hrm.upload-dir:uploads/hrm-docs}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload dir: " + this.root, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is required");

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = "";

        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot);

        String storedName = UUID.randomUUID() + ext;
        Path target = this.root.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return storedName; // lưu relative name
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + original, e);
        }
    }

    @Override
    public Resource loadAsResource(String storedPath) {
        if (!StringUtils.hasText(storedPath)) {
            throw new IllegalArgumentException("Stored path is empty");
        }
        // Normalize: use only filename to handle paths like "uploads/hrm-docs/uuid.xlsx" or "uuid.xlsx"
        String safePath = storedPath.trim();
        int lastSlash = Math.max(safePath.lastIndexOf('/'), safePath.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            safePath = safePath.substring(lastSlash + 1);
        }
        try {
            Path file = this.root.resolve(safePath).normalize();
            if (!file.startsWith(this.root)) {
                throw new IllegalArgumentException("Path traversal not allowed: " + storedPath);
            }
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) return resource;
            throw new IllegalArgumentException("File not found: " + storedPath + " (resolved: " + file + ")");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad file path: " + storedPath, e);
        }
    }

    @Override
    public void delete(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) return;
        try {
            Path file = this.root.resolve(storedPath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // không throw để tránh fail luồng xóa record DB
        }
    }
}
