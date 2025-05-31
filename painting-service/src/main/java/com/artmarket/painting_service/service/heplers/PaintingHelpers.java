package com.artmarket.painting_service.service.heplers;

import com.artmarket.DTO.PaintingResponse;
import com.artmarket.painting_service.DTO.PaintingRequest;
import com.artmarket.painting_service.model.Painting;
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
        return baseUrl + "/images/" + fileName;
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

    public PaintingResponse convertToResponse(Painting painting) {
        return new PaintingResponse(
                painting.getId(),
                painting.getTitle(),
                painting.getDescription(),
                painting.getAuthor(),
                painting.getReleaseDate(),
                painting.getPrice(),
                painting.getWeight(),
                painting.getWidth(),
                painting.getHeight(),
                painting.getDepth(),
                painting.getImageURL(),
                painting.getUserId()
        );
    }

    public Painting buildPaintingFromRequest(PaintingRequest request, String imageUrl, String userId) {
        return Painting.builder()
                .author(request.author())
                .price(request.price())
                .title(request.title())
                .description(request.description())
                .releaseDate(request.releaseDate())
                .imageURL(imageUrl)
                .userId(userId)
                .weight(request.weight())
                .width(request.width())
                .height(request.height())
                .depth(request.depth())
                .build();
    }

    public void updatePaintingImage(Painting painting, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            deleteImageFile(painting.getImageURL());
            String imageUrl = saveImage(imageFile);
            painting.setImageURL(imageUrl);
        }
    }

    public void updatePaintingFromRequest(Painting painting, PaintingRequest request) {
        painting.setTitle(request.title());
        painting.setDescription(request.description());
        painting.setPrice(request.price());
        painting.setAuthor(request.author());
        painting.setReleaseDate(request.releaseDate());
    }

}
