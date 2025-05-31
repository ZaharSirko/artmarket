package com.artmarket.painting_service;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;


class PaintingControllerWithTestDbTest extends BaseIntegrationTest  {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void registerTestDbProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
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
    void shouldDeletePainting_withValidIdAndToken() throws Exception {
        Long paintingId = 1L;

        mockMvc.perform(delete("/paintings/{id}/delete", paintingId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Painting deleted successfully"));
    }


}
