package com.artmarket.user_service;

import com.artmarket.DTO.PaintingResponse;
import com.artmarket.user_service.DTO.client.PageResponse;
import com.artmarket.user_service.client.PaintingClient;
import com.artmarket.user_service.service.KeycloakUserService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

import static io.restassured.RestAssured.given;


import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceApplicationTests {

	@LocalServerPort
	protected Integer port;

	@Autowired
	protected MockMvc mockMvc;

	@MockBean
	protected PaintingClient paintingClient;


	private KeycloakUserService keycloakUserService;

	protected String token;

	protected String user_Id;

	@Container
	static final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.1")
			.withRealmImportFile("keycloak-test-realm.json")
			.withAdminUsername("admin")
			.withAdminPassword("admin")
			.waitingFor(new HttpWaitStrategy()
					.forPath("/admin/master/console")
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

	@BeforeAll
	static void startContainer() {
		keycloak.start();
	}

	@BeforeEach
	void setUp() throws IOException {
		RestAssured.port = port;
		token = getAccessTokenFromKeycloak();

		String[] chunks = token.split("\\.");
		String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> claims = mapper.readValue(payload, Map.class);

		user_Id = claims.get("sub").toString();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
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

	@Test
	void shouldReturnUser() throws Exception {
		mockMvc.perform(get("/users/me")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("artist@artmarket.com"))
				.andExpect(jsonPath("$.roles").isArray())
				.andExpect(jsonPath("$.roles").value(hasItem("ARTIST")));
	}

	@Test
	void shouldReturnUserPaintings() throws Exception {
		String userId = user_Id;

		PaintingResponse painting = new PaintingResponse(
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
		);

		PageResponse pageResponse = new PageResponse(
				List.of(painting),
				1,   // totalPages
				1,   // totalElements
				0,   // number (current page)
				10,  // size
				true, // first
				true  // last
		);


		when(paintingClient.getUserPaintings(eq(userId))).thenReturn(pageResponse);

		mockMvc.perform(get("/users/" + userId + "/paintings")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content[0].title").value("Starry Night"))
				.andExpect(jsonPath("$.content[0].author").value("Vincent van Gogh"))
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.first").value(true));
	}


	@Test
	void shouldUpdatePassword() throws Exception {
		String password = "artist";

		mockMvc.perform(put("/users/updatePassword")
						.header("Authorization", "Bearer " + token)
						.content(password))
				.andExpect(status().isOk())
				.andExpect(content().string("Password updated successfully"));
	}


//	@Test
//	void shouldAssignRoleToUser() throws Exception {
//		String roleName = "COLLECTOR";
//
//		mockMvc.perform(put("/users/assign-role/" + user_Id)
//						.param("roleName", roleName)
//						.header("Authorization", "Bearer " + token))
//				.andExpect(status().isOk())
//				.andExpect(content().string("Role '" + roleName + "' assigned to user " + user_Id));
//	}

	@Test
	void shouldCreateUser() throws Exception {
		String requestJson = """
        {
            "username": "newuser@art.com",
            "email": "newuser@art.com",
            "firstName": "New",
            "lastName": "User",
            "password": "password123"
        }
        """;

		mockMvc.perform(post("/users/registration")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("newuser@art.com"));
	}

	@Test
	@Order(Ordered.LOWEST_PRECEDENCE)
	void shouldDeleteUserById() throws Exception {
		String userId = user_Id;

		mockMvc.perform(delete("/users/" + userId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string("User deleted successfully"));
	}
}
