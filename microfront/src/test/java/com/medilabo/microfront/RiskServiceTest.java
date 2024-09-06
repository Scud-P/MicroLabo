package com.medilabo.microfront;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabo.microfront.service.RiskService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RiskServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @Autowired
    private RiskService riskService;

    @Mock
    private Model model;

    private static MockWebServer gatewayMockServer;
    private static MockWebServer authMockServer;


    @BeforeAll
    public static void startMockServers() throws Exception {
        gatewayMockServer = new MockWebServer();
        gatewayMockServer.start(8080);
        authMockServer = new MockWebServer();
        authMockServer.start(8085);
    }

    @AfterAll
    public static void shutDown() throws Exception {
        gatewayMockServer.shutdown();
        authMockServer.shutdown();
    }

    @Test
    public void testFetchRiskById() {
        String risk = "This has to be the hugest risk of the history of risks";

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(risk)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        String foundRisk = riskService.fetchRiskById(1L, "someValidToken");
        assertEquals(risk, foundRisk);
    }

}
