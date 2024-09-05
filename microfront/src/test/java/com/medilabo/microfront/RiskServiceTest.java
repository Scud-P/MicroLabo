package com.medilabo.microfront;

import com.medilabo.microfront.service.RiskService;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;

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

}
