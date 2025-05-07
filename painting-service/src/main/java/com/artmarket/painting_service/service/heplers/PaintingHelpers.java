package com.artmarket.painting_service.service.heplers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@RequiredArgsConstructor
public class PaintingHelpers {

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Value("${app.base-url}")
    private String baseUrl;

    public String saveImage(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return baseUrl + "images/" + fileName;
    }

    public void deleteImageFile(String imageUrl) {
        try {
            Path imagePath = Paths.get(uploadDirectory, extractFilenameFromUrl(imageUrl));
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting image file: " + imageUrl, e);
        }
    }

    private String extractFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
