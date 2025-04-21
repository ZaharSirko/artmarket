package com.artmarket.painting_service.service;

import com.artmarket.painting_service.model.Painting;
import com.artmarket.painting_service.model.PaintingDoc;
import com.artmarket.painting_service.repository.PaintingElasticsearchRepository;
import com.artmarket.painting_service.repository.PaintingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaintingIndexingService {
    private final PaintingRepository paintingRepository;
    private final PaintingElasticsearchRepository paintingElasticsearchRepository;

    @Transactional(readOnly = true)
    public void reindexAllMovies() {
        List<Painting> paintings = paintingRepository.findAll();

        paintingElasticsearchRepository.saveAll(
                paintings.stream().map(
                        painting -> new PaintingDoc(
                                painting.getId(),
                                painting.getTitle(),
                                painting.getDescription(),
                                painting.getAuthor()
                        )
                ).toList()
        );

    }
}
