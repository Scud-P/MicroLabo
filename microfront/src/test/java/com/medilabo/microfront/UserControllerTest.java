package com.medilabo.microfront;

import com.medilabo.microfront.controller.UserController;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient.Builder webClientBuilder;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetLogin() throws Exception {
        mockMvc.perform(get("/api/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void testValidateLoginSuccess() throws Exception {
        when(webClientBuilder.build()).thenReturn(WebClient.builder().build());
        mockMvc.perform(post("/api/login")
                        .param("username", "bob")
                        .param("password", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/api/home"))
                .andExpect(cookie().exists("token"));
    }
}

