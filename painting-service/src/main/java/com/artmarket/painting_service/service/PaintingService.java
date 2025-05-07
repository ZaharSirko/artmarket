package com.artmarket.painting_service.service;


import com.artmarket.painting_service.DTO.PaintingRequest;
import com.artmarket.painting_service.DTO.PaintingResponse;
import com.artmarket.painting_service.model.Painting;
import com.artmarket.painting_service.model.PaintingDoc;
import com.artmarket.painting_service.repository.PaintingElasticsearchRepository;
import com.artmarket.painting_service.repository.PaintingRepository;
import com.artmarket.painting_service.service.heplers.AccessControlHelper;
import com.artmarket.painting_service.service.heplers.PaintingHelpers;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;

import static java.util.Comparator.comparingInt;

@Service
@RequiredArgsConstructor
public class PaintingService {
    private static final Logger log = LoggerFactory.getLogger(PaintingService.class);
    private final PaintingRepository paintingRepository;
    private final PaintingElasticsearchRepository paintingElasticsearchRepository;
    private final PaintingHelpers paintingHelpers;
    private final AccessControlHelper accessControlHelper;


    public Page<PaintingResponse> getPaintings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paintingRepository.findAll(pageable)
                .map(painting -> new PaintingResponse(
                                painting.getId(),
                                painting.getTitle(),
                                painting.getDescription(),
                                painting.getAuthor(),
                                painting.getReleaseDate(),
                                painting.getPrice(),
                                painting.getImageURL(),
                                painting.getUserId()
                        )
                );
    }

    @Transactional
    public void createPainting(PaintingRequest paintingRequest, MultipartFile imageFile) {
        try {
            accessControlHelper.assertCanCreatePainting();

            Long userId = accessControlHelper.getCurrentUserId();

            String imageUrl = paintingHelpers.saveImage(imageFile);
            log.info("Image saved: {}", imageUrl);

            Painting painting = Painting.builder()
                    .author(paintingRequest.author())
                    .price(paintingRequest.price())
                    .title(paintingRequest.title())
                    .description(paintingRequest.description())
                    .releaseDate(paintingRequest.releaseDate())
                    .imageURL(imageUrl)
                    .userId(userId)
                    .build();
            paintingRepository.save(painting);
            log.info("Painting created: {}", painting);
        } catch (Exception e) {
            throw new RuntimeException("Error saving painting: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deletePainting(Long id) throws AccessDeniedException {
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Painting not found"));

        accessControlHelper.checkDeletePermission(painting.getUserId());


        paintingHelpers.deleteImageFile(painting.getImageURL());
        log.info("Painting image deleted: {}", painting.getImageURL());


        paintingRepository.deleteById(id);
        log.info("Painting deleted: {}", painting.getId());
    }


    @Transactional
    public void updatePainting(Long id, PaintingRequest paintingRequest, MultipartFile imageFile) throws IOException {
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Painting not found"));

        accessControlHelper.checkUpdatePermission(painting.getUserId());

        if (imageFile != null && !imageFile.isEmpty()) {
            paintingHelpers.deleteImageFile(painting.getImageURL());
            String imageUrl = paintingHelpers.saveImage(imageFile);
            painting.setImageURL(imageUrl);
            log.info("Painting image updated: {}", imageUrl);
        }

        painting.setTitle(paintingRequest.title());
        painting.setDescription(paintingRequest.description());
        painting.setPrice(paintingRequest.price());
        painting.setAuthor(paintingRequest.author());
        painting.setReleaseDate(paintingRequest.releaseDate());

        paintingRepository.save(painting);
        log.info("Painting updated: {}", painting.getId());
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
                        painting.getImageURL(),
                        painting.getUserId()
                ))
                .sorted(comparingInt(painting -> idsMap.get(painting.id())))
                .toList();

        return new PageImpl<>(paintingsFromDb, pageable, searchResults.getTotalElements());
    }


    public Page<PaintingResponse> getUserPaintings(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<PaintingResponse> userPaintingsFromDb = paintingRepository.findAllByUserId(userId)
                .stream()
                .map(painting -> new PaintingResponse(
                                painting.getId(),
                                painting.getTitle(),
                                painting.getDescription(),
                                painting.getAuthor(),
                                painting.getReleaseDate(),
                                painting.getPrice(),
                                painting.getImageURL(),
                                painting.getUserId()
                        )
                ).toList();
        return new PageImpl<>(userPaintingsFromDb, pageable, userPaintingsFromDb.size());
    }
}


