package com.example.hrm.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {
    String store(MultipartFile file);
    Resource loadAsResource(String storedPath);
    void delete(String storedPath);
}
