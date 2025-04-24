package com.artmarket.painting_service.service;


import com.artmarket.painting_service.DTO.PaintingRequest;
import com.artmarket.painting_service.DTO.PaintingResponse;
import com.artmarket.painting_service.model.Painting;
import com.artmarket.painting_service.model.PaintingDoc;
import com.artmarket.painting_service.repository.PaintingElasticsearchRepository;
import com.artmarket.painting_service.repository.PaintingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.comparingInt;

@Service
@RequiredArgsConstructor
public class PaintingService {
    private static final Logger log = LoggerFactory.getLogger(PaintingService.class);
    private final PaintingRepository paintingRepository;

    private final PaintingElasticsearchRepository paintingElasticsearchRepository;

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Value("${app.base-url}")
    private String baseUrl;


    public Page<PaintingResponse> getPaintings(int page,int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paintingRepository.findAll(pageable)
                .map(painting -> new PaintingResponse(
                        painting.getId(),
                        painting.getTitle(),
                        painting.getDescription(),
                        painting.getAuthor(),
                        painting.getReleaseDate(),
                        painting.getPrice(),
                        painting.getImageURL()
                )
        );
    }

    @Transactional
    public void createPainting(PaintingRequest paintingRequest, MultipartFile imageFile) {
       try {
           String imageUrl = saveImage(imageFile);
           Painting painting = Painting.builder()
                   .author(paintingRequest.author())
                   .price(paintingRequest.price())
                   .title(paintingRequest.title())
                   .description(paintingRequest.description())
                   .releaseDate(paintingRequest.releaseDate())
                   .imageURL(imageUrl)
                   .build();
           paintingRepository.save(painting);
           log.info("Painting created: {}", painting);
       }
       catch (Exception e) {
           throw new RuntimeException("Error saving painting: " + e.getMessage(), e);
       }
    }

    @Transactional
    public void deletePainting(Long id) {
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Painting not found"));

        if (painting.getImageURL() != null) {
            deleteImageFile(painting.getImageURL());
            log.info("Painting images deleted: {}", painting);
        }

        paintingRepository.deleteById(id);
        log.info("Painting deleted: {}", painting);
    }



    public Page<PaintingResponse> searchPaintingsViaElastic(String searchText, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaintingDoc> searchResults = paintingElasticsearchRepository.searchByQuery(searchText, pageable);

        Map<Long, Integer> idsMap = new HashMap<>();
        List<PaintingDoc> movieDocs = searchResults.getContent();
        for (int i = 0; i < movieDocs.size(); i++) {
            idsMap.put(movieDocs.get(i).getId(), i);
        }

        Set<Long> ids = idsMap.keySet();

        List<PaintingResponse> paintingsFromDb = paintingRepository.findAllById(ids)
                .stream()
                .map(painting -> new PaintingResponse(
                        painting.getId(),
                        painting.getTitle(),
                        painting.getDescription(),
                        painting.getAuthor(),
                        painting.getReleaseDate(),
                        painting.getPrice(),
                        painting.getImageURL()
                ))
                .sorted(comparingInt(painting -> idsMap.get(painting.id())))
                .toList();

        return new PageImpl<>(paintingsFromDb, pageable, searchResults.getTotalElements());
    }

    public String saveImage(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Directories created: {}", uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Image saved: {}", fileName);
        return baseUrl + "images/" + fileName;
    }

    private void deleteImageFile(String imageUrl) {
        try {
            Path imagePath = Paths.get(uploadDirectory, extractFilenameFromUrl(imageUrl));
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            log.error("Failed to delete image file: {}", imageUrl, e);
            throw new RuntimeException("Error deleting image file: " + imageUrl, e);
        }
    }

    private String extractFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

}
