package com.artmarket.logistics_service.config;


import com.artmarket.logistics_service.client.NovaPoshtaClient;
import com.artmarket.logistics_service.client.OrderClient;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    @Value("${order-service.url}")
    private String orderServiceUrl;

    @Value("${novaposhta.api.url}")
    private String novaPoshtaUrl;

    @Value("${novaposhta.api.key}")
    private String novaposhtaApiKey;

    private final ObservationRegistry observationRegistry;

    @Bean
    public OrderClient orderClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(orderServiceUrl)
                .requestFactory(getClientHttpRequestFactory())
                .observationRegistry(observationRegistry)
                .build();

        var adapter = RestClientAdapter.create(restClient);
        var proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(OrderClient.class);
    }


    @Bean
    public NovaPoshtaClient novaPoshtaClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(novaPoshtaUrl)
                .requestFactory(getClientHttpRequestFactory())
                .observationRegistry(observationRegistry)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        var adapter = RestClientAdapter.create(restClient);
        var proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(NovaPoshtaClient.class);
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));
        return factory;
    }
}
