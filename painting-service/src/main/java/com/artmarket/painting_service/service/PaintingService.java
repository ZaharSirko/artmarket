package com.artmarket.painting_service.service;


import com.artmarket.DTO.PaintingResponse;
import com.artmarket.painting_service.DTO.PaintingRequest;
import com.artmarket.painting_service.model.Painting;
import com.artmarket.painting_service.model.PaintingDoc;
import com.artmarket.painting_service.repository.PaintingElasticsearchRepository;
import com.artmarket.painting_service.repository.PaintingRepository;
import com.artmarket.painting_service.service.heplers.AccessControlHelper;
import com.artmarket.painting_service.service.heplers.PaintingHelpers;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class PaintingService {
    private final PaintingRepository paintingRepository;
    private final PaintingElasticsearchRepository paintingElasticsearchRepository;
    private final PaintingHelpers paintingHelpers;
    private final AccessControlHelper accessControlHelper;


    public Page<PaintingResponse> getPaintings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paintingRepository.findAll(pageable)
                .map(paintingHelpers::convertToResponse);
    }

    @Transactional
    public void createPainting(PaintingRequest paintingRequest, MultipartFile imageFile) {
        try {
            accessControlHelper.assertCanCreatePainting();
            String userId = accessControlHelper.getCurrentUserId();

            String imageUrl = paintingHelpers.saveImage(imageFile);
            log.info("Image saved: {}", imageUrl);

            Painting painting = paintingHelpers.buildPaintingFromRequest(paintingRequest, imageUrl, userId);
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

        paintingHelpers.updatePaintingImage(painting, imageFile);
        paintingHelpers.updatePaintingFromRequest(painting, paintingRequest);

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
                .map(paintingHelpers::convertToResponse)
                .sorted(comparingInt(painting -> idsMap.get(painting.id())))
                .toList();

        return new PageImpl<>(paintingsFromDb, pageable, searchResults.getTotalElements());
    }


    public Page<PaintingResponse> getUserPaintings(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<PaintingResponse> userPaintings = paintingRepository.findAllByUserId(userId)
                .stream()
                .map(paintingHelpers::convertToResponse)
                .toList();

        return new PageImpl<>(userPaintings, pageable, userPaintings.size());
    }

    public List<PaintingResponse> getAllPaintingsById(List<Long> paintingIds) {
        return paintingRepository.findByIdIn(paintingIds)
                .stream()
                .map(paintingHelpers::convertToResponse)
                .toList();
    }

    public PaintingResponse getPaintingById(Long paintingId) {
        return paintingRepository.findById(paintingId)
                .map(paintingHelpers::convertToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Painting not found: " + paintingId));
    }
}


