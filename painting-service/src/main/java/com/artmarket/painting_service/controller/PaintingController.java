package com.artmarket.painting_service.controller;

import com.artmarket.painting_service.DTO.PaintingRequest;
import com.artmarket.painting_service.DTO.PaintingResponse;
import com.artmarket.painting_service.service.PaintingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaintingController {
    private static  final String HOMEPAGE = "/";
    private  static final String SEARCH = "/search";
    @Value("${upload.directory}")
    private String uploadDirectory;

    private final PaintingService paintingService;


    @GetMapping(HOMEPAGE)
    public ResponseEntity<Page<PaintingResponse>> getAllPaintings(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        var allPainting = paintingService.getPaintings(page, size);
        return new ResponseEntity<>(allPainting, HttpStatus.OK);
    }


    @GetMapping(SEARCH)
    public ResponseEntity<Page<PaintingResponse>> searchPainting(
            @RequestParam String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        var foundPaintings = paintingService.searchPaintingsViaElastic(query, page, size);
        return new ResponseEntity<>(foundPaintings, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPainting(
            @ModelAttribute PaintingRequest paintingRequest,
            @RequestParam("image") MultipartFile imageFile) {

        paintingService.createPainting(paintingRequest, imageFile);
        return ResponseEntity.ok("Painting created successfully");
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDirectory).resolve(filename);
            Resource file = new UrlResource(filePath.toUri());

            if (file.exists() || file.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath))
                        .body(file);
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePainting(@PathVariable Long id) {
        paintingService.deletePainting(id);
        return ResponseEntity.ok("Painting deleted successfully");
    }


}
