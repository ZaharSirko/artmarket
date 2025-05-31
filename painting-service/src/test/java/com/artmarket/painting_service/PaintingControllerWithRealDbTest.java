package com.artmarket.painting_service;


import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

 class PaintingControllerWithRealDbTest extends BaseIntegrationTest  {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Test
    void shouldGetAllPaintings_withValidToken() throws Exception {
        mockMvc.perform(get("/paintings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
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
        String userId = "5c2df8b1-1217-4d8d-aaee-8ddd454049c4";

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
    void shouldSearchPaintings_withQueryAndToken() throws Exception {
        mockMvc.perform(get("/paintings/search")
                        .param("query", "Van Gogh")
                        .param("page", "0")
                        .param("size", "5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
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





}
