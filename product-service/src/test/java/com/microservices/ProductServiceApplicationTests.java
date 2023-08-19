package com.microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.dto.ProductRequest;
import com.microservices.dto.ProductResponse;
import com.microservices.model.Product;
import com.microservices.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
        dymDynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    public void clearData() {
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequest productRequest = getProductRequest();
        String productRequestString = objectMapper.writeValueAsString(productRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequestString))
                .andExpect(status().isCreated());
        Assertions.assertEquals(1, productRepository.findAll().size());
    }

    @Test
    void shouldReturnAllProducts() throws Exception {

        productRepository.save(Product.builder().name("test1").description("testDesc1").price(BigDecimal.valueOf(120000)).build());
        productRepository.save(Product.builder().name("test2").description("testDesc2").price(BigDecimal.valueOf(220000)).build());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product").contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().is2xxSuccessful()).
                andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("test1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("testDesc1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].price").value(120000))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("test2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("testDesc2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].price").value(220000))
        ;

        Assertions.assertEquals(2, productRepository.findAll().size());
    }

    private ProductRequest getProductRequest() {
        return ProductRequest.builder()
                .name("iPhone 13")
                .description("iPhone 13")
                .price(BigDecimal.valueOf(1200))
                .build();
    }

}
