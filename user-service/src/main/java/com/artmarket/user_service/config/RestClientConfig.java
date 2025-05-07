package com.artmarket.user_service.config;

import com.artmarket.user_service.client.PaintingClient;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    @Value("${painting.url}")
    private String paintingServiceUrl;
    private final ObservationRegistry observationRegistry;

    @Bean
    public PaintingClient paintingClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(paintingServiceUrl)
                .observationRegistry(observationRegistry)
                .build();
        var adapter = RestClientAdapter.create(restClient);
        var factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(PaintingClient.class);
    }
}

