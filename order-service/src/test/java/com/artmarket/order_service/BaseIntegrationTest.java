package com.artmarket.order_service;

import com.artmarket.DTO.PaintingResponse;
import com.artmarket.DTO.UserResponse;


import com.artmarket.order_service.DTO.OrderItemRequest;
import com.artmarket.order_service.client.PaintingClient;
import com.artmarket.order_service.client.UserClient;
import com.artmarket.order_service.service.KafkaOrderEventProducer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected UserClient userClient;

    @MockBean
    protected PaintingClient paintingClient;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private KafkaOrderEventProducer kafkaOrderEventProducer;

    protected String token;

    @LocalServerPort
    protected Integer port;

    protected String userId;



    @Container
    static final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.1")
            .withRealmImportFile("keycloak-test-realm.json")
            .withAdminUsername("admin")
            .withAdminPassword("admin")
            .waitingFor(new HttpWaitStrategy()
                    .forPath("/health/ready")
                    .forPort(8080)
                    .withStartupTimeout(Duration.ofSeconds(60)));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        String issuerUri = "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080) + "/realms/artmarket";

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> issuerUri + "/protocol/openid-connect/certs");
        registry.add("keycloak.auth-server-url", () -> keycloak.getAuthServerUrl());
        registry.add("keycloak.realm", () -> "artmarket");
        registry.add("keycloak.resource", () -> "user-service");
        registry.add("keycloak.credentials.secret", () -> "user-service-secret");

    }

    @BeforeEach
    void setUp() throws IOException, ParseException {
        RestAssured.port = port;
        token = getAccessTokenFromKeycloak();

        String[] chunks = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> claims = mapper.readValue(payload, Map.class);

        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");

        userId = claims.get("sub").toString();

        UserResponse mockUser = new UserResponse(
                userId,
                (String) claims.get("email"),
                (String) claims.get("preferred_username"),
                (String) claims.get("given_name"),
                (String) claims.get("family_name"),
                roles,
                1L
        );

        List<OrderItemRequest> itemRequests = List.of(
                new OrderItemRequest(1L),
                new OrderItemRequest(2L)
        );

        List<PaintingResponse> paintings = List.of(
                new PaintingResponse(
                        1L,
                        "Starry Night",
                        "Famous painting by Van Gogh",
                        "Vincent van Gogh",
                        new SimpleDateFormat("yyyy-MM-dd").parse("1889-06-01"),
                        new BigDecimal("10000.00"),
                        new BigDecimal("2.5"),
                        new BigDecimal("60.0"),
                        new BigDecimal("50.0"),
                        new BigDecimal("5.0"),
                        "https://example.com/starry-night.jpg",
                        userId
                ),
                new PaintingResponse(
                        2L,
                        "The Persistence of Memory",
                        "Iconic surrealist painting with melting clocks",
                        "Salvador Dalí",
                        new SimpleDateFormat("yyyy-MM-dd").parse("1931-04-01"),
                        new BigDecimal("15000.00"),
                        new BigDecimal("1.8"),  // вага
                        new BigDecimal("33.0"), // довжина
                        new BigDecimal("24.0"), // ширина
                        new BigDecimal("4.0"),  // висота
                        "https://example.com/persistence-of-memory.jpg",
                        userId
                )
        );

        List<Long> paintingIds = itemRequests.stream()
                .map(OrderItemRequest::paintingId)
                .collect(Collectors.toList());

        doNothing().when(kafkaOrderEventProducer).sendOrderCreatedEvent(any(), any());
        when(paintingClient.getPaintingsByIds(paintingIds)).thenReturn(paintings);
        when(userClient.getCurrentUser(anyString())).thenReturn(mockUser);

    }

    protected String getAccessTokenFromKeycloak() {
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("client_id", "user-service")
                .formParam("username", "artist@artmarket.com")
                .formParam("password", "artist")
                .formParam("grant_type", "password")
                .formParam("client_secret", "user-service-secret")
                .when()
                .post("http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080)
                        + "/realms/artmarket/protocol/openid-connect/token")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("access_token");
    }
}
