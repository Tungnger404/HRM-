package com.example.hrm.service.impl;

import com.example.hrm.service.AvatarStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class AvatarStorageServiceImpl implements AvatarStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path avatarRoot;

    public AvatarStorageServiceImpl(@Value("${hrm.avatar-upload-dir:uploads/avatars}") String uploadDir) {
        this.avatarRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.avatarRoot);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create avatar upload folder: " + this.avatarRoot, e);
        }
    }

    @Override
    public String storeAvatar(MultipartFile file, Integer userId) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please choose an avatar image");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = getExtension(originalName).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new RuntimeException("Avatar must be jpg, jpeg, png, or webp");
        }

        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Avatar size must be <= 5MB");
        }

        String newFileName = "user_" + userId + "_" + UUID.randomUUID() + "." + ext;
        Path target = avatarRoot.resolve(newFileName);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save avatar file", e);
        }

        return "/uploads/avatars/" + newFileName;
    }

    @Override
    public void deleteAvatarIfExists(String avatarPath) {
        if (avatarPath == null || avatarPath.isBlank()) {
            return;
        }

        try {
            String fileName = Paths.get(avatarPath).getFileName().toString();
            Path file = avatarRoot.resolve(fileName).normalize();

            if (Files.exists(file) && file.startsWith(avatarRoot)) {
                Files.delete(file);
            }
        } catch (Exception ignored) {
        }
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }
}