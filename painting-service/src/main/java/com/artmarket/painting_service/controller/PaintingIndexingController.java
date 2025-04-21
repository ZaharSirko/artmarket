package com.artmarket.painting_service.controller;

import com.artmarket.painting_service.service.PaintingIndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaintingIndexingController {
    private  static final String REINDEX = "/reindex";
    private final PaintingIndexingService paintingIndexingService;

    @GetMapping(REINDEX)
    public ResponseEntity<String> reindex() {
        paintingIndexingService.reindexAllMovies();
        return ResponseEntity.ok().body("Reindex successful");
    }
}
