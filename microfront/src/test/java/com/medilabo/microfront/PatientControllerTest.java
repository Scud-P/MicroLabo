package com.medilabo.microfront;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.controller.PatientController;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @Autowired
    private PatientController patientController;

    @Mock
    private Model model;

    private static MockWebServer gatewayMockServer;
    private static MockWebServer authMockServer;

    private static PatientBean firstPatient;
    private static PatientBean secondPatient;

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


    @BeforeEach
    public void setUp() throws IOException {
        LocalDate rafaelBirthdate = LocalDate.of(2017, 5, 8);
        LocalDate thaliaBirthDate = LocalDate.of(2012, 9, 7);

        firstPatient = new PatientBean
                (1L, "Rafael", "Doe", rafaelBirthdate, "M", "666 Devil Drive", "111-111-111");
        secondPatient = new PatientBean
                (2L, "Thalia", "Smith", thaliaBirthDate, "F", "777 Jackpot Road", "222-222-222");
    }

    @Test
    public void testGetHomeNoToken() throws Exception {
            mockMvc.perform(get("/api/home"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testFetchPatients() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String firstPatientJson = mapper.writeValueAsString(firstPatient);
        String secondPatientJson = mapper.writeValueAsString(secondPatient);

        String patientsJsonArray = "[" + firstPatientJson + "," + secondPatientJson + "]";

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(patientsJsonArray)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        List<PatientBean> patients = patientController.fetchPatients("someValidToken");

        assertNotNull(patients);
        assertEquals(2, patients.size());
        assertEquals("Rafael", patients.get(0).getFirstName());
        assertEquals("Thalia", patients.get(1).getFirstName());
    }


    @Test
    public void testUpdateModelWithPatients() {
        List<PatientBean> patients = List.of(firstPatient, secondPatient);

        doReturn(patients).when(patientController).fetchPatients(anyString());
        String home = patientController.updateModelWithPatients("someValidToken", model);

        assertEquals("home", home);
        verify(model, times(1)).addAttribute("patients", patients);
    }

}


