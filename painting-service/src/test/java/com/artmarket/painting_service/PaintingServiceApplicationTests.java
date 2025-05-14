package com.artmarket.painting_service;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class PaintingServiceApplicationTests {

	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

	@Container
	static GenericContainer<?> keycloak = new GenericContainer<>("quay.io/keycloak/keycloak:24.0.1")
			.withExposedPorts(8080)
			.withCommand("start-dev --import-realm")
			.withEnv("KEYCLOAK_ADMIN", "admin")
			.withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
			.withCopyFileToContainer(
					MountableFile.forClasspathResource("keycloak-test-realm.json"),
					"/opt/keycloak/data/import/realm.json"
			)
			.waitingFor(Wait.forHttp("/realms/artmarket/.well-known/openid-configuration")
					.forPort(8080)
					.forStatusCode(200)
					.withStartupTimeout(Duration.ofSeconds(60)));

	@LocalServerPort
	private Integer port;

	private String token;

	static {
		postgres.start();
		keycloak.start();

		String issuerUri = "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080) + "/realms/artmarket";
		System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", issuerUri);
	}

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		token = getAccessTokenFromKeycloak();
	}

	private String getAccessTokenFromKeycloak() {
		return RestAssured.given()
				.contentType("application/x-www-form-urlencoded")
				.formParam("client_id", "painting-service")
				.formParam("username", "artist@artmarket.com")
				.formParam("password", "artist")
				.formParam("grant_type", "password")
				.formParam("client_secret", "painting-service-secret")
				.when()
				.post("http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080) + "/realms/artmarket/protocol/openid-connect/token")
				.then()
				.statusCode(200)
				.extract()
				.jsonPath()
				.getString("access_token");
	}

	@Test
	void shouldCreatePainting() {
		File testImage = new File("src/test/resources/van_gog_sunflower_test.jpg");

		String requestBody = """
            {
              "title": "Van Gogh Sunflower",
              "description": "Iconic work by Van Gogh",
              "author": "Vincent van Gogh",
              "price": 2500.00,
              "releaseDate": "1888-08-01"
            }
            """;

		RestAssured.given()
				.header("Authorization", "Bearer " + token)
				.multiPart("imageFile", testImage)
				.multiPart("data", requestBody, "application/json")
				.when()
				.post("/paintings/create")
				.then()
				.statusCode(200);
	}
}
