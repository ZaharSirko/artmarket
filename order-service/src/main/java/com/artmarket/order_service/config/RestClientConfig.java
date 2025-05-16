package com.artmarket.order_service.config;


import com.artmarket.order_service.client.PaintingClient;
import com.artmarket.order_service.client.UserClient;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${painting-service.url}")
    private String paintingServiceUrl;

    private final ObservationRegistry observationRegistry;

    @Bean
    public UserClient userClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .requestFactory(getClientHttpRequestFactory())
                .observationRegistry(observationRegistry)
                .build();

        var adapter = RestClientAdapter.create(restClient);
        var proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(UserClient.class);
    }

    @Bean
    public PaintingClient paintingClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(paintingServiceUrl)
                .requestFactory(getClientHttpRequestFactory())
                .observationRegistry(observationRegistry)
                .build();

        var adapter = RestClientAdapter.create(restClient);
        var proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(PaintingClient.class);
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));
        return factory;
    }
}
