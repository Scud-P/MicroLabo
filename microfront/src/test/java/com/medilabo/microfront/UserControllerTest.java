package com.medilabo.microfront;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient.Builder webClientBuilder;

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
    public void testGetLogin() throws Exception {
        mockMvc.perform(get("/api/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void testValidateLoginSuccess() throws Exception {
        String mockToken = "definitelyAValidToken";
        authMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockToken)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl("http://localhost:8085")
                .build());

        mockMvc.perform(post("/api/login")
                        .param("username", "bob")
                        .param("password", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/api/home"))
                .andExpect(cookie().value("token", mockToken));
    }
}

