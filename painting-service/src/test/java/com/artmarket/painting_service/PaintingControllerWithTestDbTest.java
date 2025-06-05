package com.artmarket.painting_service;

import com.artmarket.painting_service.model.PaintingDoc;


import org.hamcrest.Matchers;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;

import java.nio.charset.StandardCharsets;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaintingControllerWithTestDbTest extends BaseIntegrationTest  {

    @Test
    @Order(1)
    void shouldCreatePainting_withValidDataAndToken() throws Exception {
        File testImage = new File("src/test/resources/van_gog_sunflower_test.jpg");

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "van_gog_sunflower_test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new FileInputStream(testImage)
        );

        MultiValueMap<String, String> jsonData = new LinkedMultiValueMap<>();
        jsonData.add("title", "Van Gogh Sunflower");
        jsonData.add("description", "Iconic work by Van Gogh");
        jsonData.add("author", "Vincent van Gogh");
        jsonData.add("releaseDate", "1888-08-01");
        jsonData.add("price", "2500.00");
        jsonData.add("weight", "1.00");
        jsonData.add("width", "1.00");
        jsonData.add("height", "1.00");
        jsonData.add("depth", "1.00");


        mockMvc.perform(multipart("/paintings/create")
                        .file(imageFile)
                        .params(jsonData)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Painting created successfully"));
    }

    @Test
    void shouldUpdatePainting_withValidDataAndToken() throws Exception {
        Long paintingId = 1L;

        MockMultipartFile jsonPart = new MockMultipartFile(
                "data", "", "application/json",
                """
                {
                    "title": "Updated Title",
                    "description": "Updated Description",
                    "author": "New Author",
                    "releaseDate": "1990-01-01",
                    "price": 3000,
                    "weight": 1.2,
                    "width": 1.2,
                    "height": 1.2,
                    "depth": 1.2
                }
                """.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/paintings/{id}/update", paintingId)
                        .file(jsonPart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Painting updated successfully"));
    }



    @Test
    void shouldGetPaintingsByIds_withValidToken() throws Exception {
        mockMvc.perform(get("/paintings/by-ids")
                        .param("ids", "1", "2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldGetPaintingsByUserId_withValidToken() throws Exception {
        mockMvc.perform(get("/paintings/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnSecureInfo_withValidToken() throws Exception {
        mockMvc.perform(get("/paintings/secure")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Hello")))
                .andExpect(content().string(Matchers.containsString("roles")));
    }

    @Test
    void shouldServeImage_withExistingFile() throws Exception {
        String filename = "van_gog_sunflower_test.jpg"; // переконайся, що файл існує в uploadDirectory

        mockMvc.perform(get("/paintings/images/{filename}", filename)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, Matchers.containsString("image/")));
    }

    @Test
    @Order(2)
    void shouldSearchPaintings_withQueryAndToken() throws Exception {
        PaintingDoc painting1 = new PaintingDoc(1L, "Van Gogh Sunflower", "Iconic work by Van Gogh", "Vincent van Gogh");
        paintingElasticsearchRepository.save(painting1);

        elasticsearchTemplate.indexOps(PaintingDoc.class).refresh();

        mockMvc.perform(get("/paintings/search")
                        .param("query", "Van Gogh")
                        .param("page", "0")
                        .param("size", "5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].title").value(Matchers.oneOf("Starry Night", "Van Gogh Sunflower")));
    }


    @Test
    void shouldGetPaintingById_withValidIdAndToken() throws Exception {
        Long paintingId = 1L;
        mockMvc.perform(get("/paintings/{id}", paintingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(paintingId));
    }

    @Test
    @Order(Ordered.LOWEST_PRECEDENCE)
    void shouldDeletePainting_withValidIdAndToken() throws Exception {
        Long paintingId = 1L;

        mockMvc.perform(delete("/paintings/{id}/delete", paintingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Painting deleted successfully"));
    }



}
