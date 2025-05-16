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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/paintings")
public class PaintingController {
    private static  final String HOMEPAGE = "/";
    private  static final String SEARCH = "/search";
    private static final String CREATE_PAINTING = "/create";
    private static final String IMAGES = "/images/{filename:.+}";
    private static final String BY_ID = "/{id}";
    private static final String UPDATE_BY_ID = "/update/{id}";
    private static final String SECURE = "/secure";
    private static final String BY_USER_ID = "/user/{userId}";
    private static final String BY_IDS = "by-ids";
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

    @PostMapping(
            path = CREATE_PAINTING,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPainting(
            @ModelAttribute PaintingRequest paintingRequest,
            @RequestParam("image") MultipartFile imageFile) {

        paintingService.createPainting(paintingRequest, imageFile);
        return ResponseEntity.ok("Painting created successfully");
    }

    @GetMapping(IMAGES)
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

    @PutMapping(UPDATE_BY_ID)
    public ResponseEntity<String> updatePainting(
            @PathVariable Long id,
            @RequestPart("data") PaintingRequest paintingRequest,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        paintingService.updatePainting(id, paintingRequest, imageFile);
        return ResponseEntity.ok("Painting updated successfully");
    }


    @GetMapping(BY_ID)
    public ResponseEntity<PaintingResponse> getPainting(@PathVariable Long id) {
        var painting = paintingService.getPaintingById(id);
        return ResponseEntity.ok(painting);
    }


    @DeleteMapping(BY_ID)
    public ResponseEntity<String> deletePainting(@PathVariable Long id) throws AccessDeniedException {
        paintingService.deletePainting(id);
        return ResponseEntity.ok("Painting deleted successfully");
    }

    @GetMapping(SECURE)
    public ResponseEntity<String> secure(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = Collections.emptyList();

        if (realmAccess != null && realmAccess.containsKey("roles")) {
            roles = (List<String>) realmAccess.get("roles");
        }

        return ResponseEntity.ok("Hello " + userId + ", roles: " + roles);
    }


    @GetMapping(BY_USER_ID)
    public ResponseEntity<Page<PaintingResponse>> getPaintingsByUser(
            @PathVariable String userId,
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        var allUserPaintings = paintingService.getUserPaintings(userId,page, size);
        return new ResponseEntity<>(allUserPaintings, HttpStatus.OK);
    }

    @GetMapping(BY_IDS)
    public List<PaintingResponse> getByIds(@RequestParam List<Long> ids) {
        return paintingService.getAllPaintingsById(ids);
    }

}
