package com.medilabo.microfront;

import com.medilabo.microfront.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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
    private UserService userService;


    @Test
    public void testGetLogin() throws Exception {
        mockMvc.perform(get("/api/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void testValidateLoginSuccess() throws Exception {
        String mockToken = "definitelyAValidToken";

        when(userService.getToken(anyString(), anyString())).thenReturn(mockToken);

        mockMvc.perform(post("/api/login")
                        .param("username", "bob")
                        .param("password", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/api/home"))
                .andExpect(cookie().value("token", mockToken));
    }
}

