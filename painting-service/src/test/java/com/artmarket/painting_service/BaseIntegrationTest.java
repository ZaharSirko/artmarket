package com.artmarket.painting_service;

import com.artmarket.dto.UserResponse;
import com.artmarket.painting_service.client.UserClient;
import com.artmarket.painting_service.model.PaintingDoc;
import com.artmarket.painting_service.repository.PaintingElasticsearchRepository;
import com.artmarket.painting_service.repository.PaintingRepository;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected UserClient userClient;

    protected String token;

    @LocalServerPort
    protected Integer port;

    protected String userId;

    @Autowired
    protected PaintingRepository paintingRepository;
    @Autowired
    protected ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    protected PaintingElasticsearchRepository paintingElasticsearchRepository;


    @Container
    static final ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.12.0"))
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");


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


        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @BeforeEach
    void setUp() throws IOException {
        RestAssured.port = port;
        token = getAccessTokenFromKeycloak();

        String settingsJson = new String(Files.readAllBytes(
                Paths.get("src/test/resources/elasticsearch-settings.json")));

        IndexOperations indexOps = elasticsearchTemplate.indexOps(PaintingDoc.class);

        if (indexOps.exists()) {
            indexOps.delete();
        }

        indexOps.createWithMapping();


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
