package com.example.hrm.service;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {
    String storeAvatar(MultipartFile file, Integer userId);
    void deleteAvatarIfExists(String avatarPath);
}