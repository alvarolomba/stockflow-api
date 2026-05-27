package com.stockflow.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class StockflowApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("stockflow")
            .withUsername("stockflow")
            .withPassword("stockflow");

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void userCanRegisterLoginAndReadCurrentProfile() throws Exception {
        register("profile@example.com");

        String token = login("profile@example.com");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("profile@example.com"))
                .andExpect(jsonPath("$.fullName").value("Ana Backend"));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        register("duplicate@example.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "duplicate@example.com",
                                  "fullName": "Ana Backend",
                                  "password": "strong-password"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void userCanCreateOrderPayItAndReadReports() throws Exception {
        register("seller@example.com");
        String token = login("seller@example.com");

        int productId = createProduct(token, "SKU-PRO-001", 1500, 10, 8);

        MvcResult orderResult = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    { "productId": %d, "quantity": 2 }
                                  ]
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalCents").value(3000))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andReturn();

        int orderId = JsonPath.read(orderResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stockOnHand").value(10))
                .andExpect(jsonPath("$[0].stockReserved").value(2))
                .andExpect(jsonPath("$[0].availableStock").value(8));

        mockMvc.perform(get("/inventory/movements").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type").value("RESERVED"))
                .andExpect(jsonPath("$[1].type").value("ADDED"));

        mockMvc.perform(get("/reports/low-stock").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(patch("/orders/" + orderId + "/pay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stockOnHand").value(8))
                .andExpect(jsonPath("$[0].stockReserved").value(0))
                .andExpect(jsonPath("$[0].availableStock").value(8));

        mockMvc.perform(get("/reports/sales").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidOrders").value(1))
                .andExpect(jsonPath("$.totalRevenueCents").value(3000));

        mockMvc.perform(get("/reports/orders-by-status").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(0))
                .andExpect(jsonPath("$.paid").value(1))
                .andExpect(jsonPath("$.canceled").value(0));
    }

    @Test
    void pendingOrderCanBeCanceledAndReleasesReservedStock() throws Exception {
        register("cancel@example.com");
        String token = login("cancel@example.com");

        int productId = createProduct(token, "SKU-CANCEL-001", 2000, 5, 1);
        int orderId = createOrder(token, productId, 3);

        mockMvc.perform(patch("/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stockOnHand").value(5))
                .andExpect(jsonPath("$[0].stockReserved").value(0));
    }

    @Test
    void orderCannotExceedAvailableStock() throws Exception {
        register("stock-limit@example.com");
        String token = login("stock-limit@example.com");

        int productId = createProduct(token, "SKU-LIMIT-001", 1200, 1, 1);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    { "productId": %d, "quantity": 2 }
                                  ]
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usersCannotAccessEachOthersProducts() throws Exception {
        register("ana@example.com");
        register("bob@example.com");
        String anaToken = login("ana@example.com");
        String bobToken = login("bob@example.com");

        int productId = createProduct(anaToken, "SKU-PRIVATE-001", 9900, 10, 2);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    { "productId": %d, "quantity": 1 }
                                  ]
                                }
                                """.formatted(productId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticationIsRequired() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isUnauthorized());
    }

    private int createProduct(String token, String sku, int priceCents, int initialStock, int lowStockThreshold) throws Exception {
        MvcResult result = mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "%s",
                                  "name": "Mechanical Keyboard",
                                  "priceCents": %d,
                                  "initialStock": %d,
                                  "lowStockThreshold": %d
                                }
                                """.formatted(sku, priceCents, initialStock, lowStockThreshold)))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private int createOrder(String token, int productId, int quantity) throws Exception {
        MvcResult result = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    { "productId": %d, "quantity": %d }
                                  ]
                                }
                                """.formatted(productId, quantity)))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "fullName": "Ana Backend",
                                  "password": "strong-password"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "strong-password"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }
}
