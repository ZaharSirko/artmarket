package com.artmarket.order_service;


import com.artmarket.DTO.OrderStatus;
import com.artmarket.DTO.PaintingResponse;
import com.artmarket.order_service.model.Order;
import com.artmarket.order_service.model.OrderItem;
import com.artmarket.order_service.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;


import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

class OrderServiceApplicationTests extends BaseIntegrationTest {

	@Autowired
	private OrderRepository orderRepository;

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
	@DisplayName("Create order - success")
	void createOrder_shouldCreateOrderSuccessfully() throws Exception {
		// Given
		String validOrderJson = """
            {
                "items": [
                    {"paintingId": 1},
                    {"paintingId": 2}
                ]
            }
            """;

		// When & Then
		mockMvc.perform(post("/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validOrderJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.userId").value(userId))
				.andExpect(jsonPath("$.status").value("NEW"))
				.andExpect(jsonPath("$.itemsPrice").value(25000.00))
				.andExpect(jsonPath("$.paintings.length()").value(2))
				.andExpect(jsonPath("$.paintings[0].id").value(1))
				.andExpect(jsonPath("$.paintings[1].id").value(2));
	}

	@Test
	@DisplayName("Create order with empty items - should fail")
	void createOrder_withEmptyItems_shouldFail() throws Exception {
		// Given
		String emptyOrderJson = """
            {
                "items": []
            }
            """;

		// When & Then
		mockMvc.perform(post("/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(emptyOrderJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@DisplayName("Get order by ID - success")
	void getOrderById_shouldReturnOrder() throws Exception {
		// Given
		Order order = createTestOrder();
		Long orderId = order.getId();

		// When & Then
		mockMvc.perform(get("/orders/{orderId}", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(orderId))
				.andExpect(jsonPath("$.userId").value(userId))
				.andExpect(jsonPath("$.status").value("NEW"))
				.andExpect(jsonPath("$.itemsPrice").value(25000.00))
				.andExpect(jsonPath("$.paintings.length()").value(2));
	}

	@Test
	@DisplayName("Get order by ID - not found")
	void getOrderById_whenOrderNotFound_shouldReturnNotFound() throws Exception {
		// When & Then
		mockMvc.perform(get("/orders/{orderId}", 999L)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@DisplayName("Get my orders - success")
	void getMyOrders_shouldReturnUserOrders() throws Exception {
		// Given
		Order order1 = createTestOrder();
		Order order2 = createTestOrder();

		// When & Then
		mockMvc.perform(get("/orders/my")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
				.andExpect(jsonPath("$[?(@.id == " + order1.getId() + ")]").exists())
				.andExpect(jsonPath("$[?(@.id == " + order2.getId() + ")]").exists());
	}

	@Test
	@DisplayName("Add painting to order - success")
	void addPaintingToOrder_shouldAddItemSuccessfully() throws Exception {
		// Given
		Order order = createTestOrder();
		Long orderId = order.getId();


		String addItemJson = """
            {
                "paintingId": 3
            }
            """;

		PaintingResponse newPainting = new PaintingResponse(
				3L,
				"Mona Lisa",
				"Famous portrait by Leonardo da Vinci",
				"Leonardo da Vinci",
				new SimpleDateFormat("yyyy-MM-dd").parse("1503-01-01"),
				new BigDecimal("20000.00"),
				new BigDecimal("3.0"),
				new BigDecimal("77.0"),
				new BigDecimal("53.0"),
				new BigDecimal("5.0"),
				"https://example.com/mona-lisa.jpg",
				userId
		);

		when(paintingClient.getPaintingById(3L)).thenReturn(newPainting);

		// When & Then
		mockMvc.perform(put("/orders/{orderId}/items", orderId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(addItemJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(orderId))
				.andExpect(jsonPath("$.itemsPrice").value(45000.00));
	}

	@Test
	@DisplayName("Remove painting from order - success")
	void removePaintingFromOrder_shouldRemoveItemSuccessfully() throws Exception {
		// Given
		Order order = createTestOrder();
		Long orderId = order.getId();

		String removeItemJson = """
            {
                "paintingId": 1
            }
            """;

		// When & Then
		mockMvc.perform(delete("/orders/{orderId}/items", orderId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(removeItemJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(orderId))
				.andExpect(jsonPath("$.itemsPrice").value(15000.00));
	}

	@Test
	@DisplayName("Remove non-existent painting from order - should fail")
	void removePaintingFromOrder_whenPaintingNotInOrder_shouldFail() throws Exception {
		// Given
		Order order = createTestOrder();
		Long orderId = order.getId();

		String removeItemJson = """
            {
                "paintingId": 999
            }
            """;

		// When & Then
		mockMvc.perform(delete("/orders/{orderId}/items", orderId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(removeItemJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	private Order createTestOrder() {
		Order order = Order.builder()
				.userId(userId)
				.status(OrderStatus.NEW)
				.itemsPrice(new BigDecimal("25000.00"))
				.build();

		List<OrderItem> items = List.of(
				OrderItem.builder()
						.order(order)
						.paintingId(1L)
						.price(new BigDecimal("10000.00"))
						.build(),
				OrderItem.builder()
						.order(order)
						.paintingId(2L)
						.price(new BigDecimal("15000.00"))
						.build()
		);

		order.setItems(items);
		return orderRepository.save(order);
	}
}