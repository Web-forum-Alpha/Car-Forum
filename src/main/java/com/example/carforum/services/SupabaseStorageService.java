package com.example.carforum.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SupabaseStorageService {
    String uploadFile(MultipartFile file, int userId) throws IOException, IOException;
}
