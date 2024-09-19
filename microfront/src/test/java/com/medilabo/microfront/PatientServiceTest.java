package com.medilabo.microfront;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.PatientAlreadyExistsException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import com.medilabo.microfront.service.PatientService;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PatientServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @Autowired
    private PatientService patientService;

    @Mock
    private Model model;

    private ObjectMapper mapper;

    private static MockWebServer gatewayMockServer;
    private static MockWebServer authMockServer;

    private static PatientBean firstPatient;
    private static PatientBean secondPatient;
    private static PatientBean updatedPatient;

    private String validToken;

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
        updatedPatient = new PatientBean
                (1L, "Rafael", "Doe", rafaelBirthdate, "M", "666 Devil Drive", "333-333-333");
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        validToken = "someValidToken";
    }

    @Test
    public void testFetchPatients() throws Exception {
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

        List<PatientBean> patients = patientService.fetchPatients(validToken);

        assertNotNull(patients);
        assertEquals(2, patients.size());
        assertEquals("Rafael", patients.get(0).getFirstName());
        assertEquals("Thalia", patients.get(1).getFirstName());
    }

    @Test
    public void testFetchPatientById() throws JsonProcessingException {

        String firstPatientJson = mapper.writeValueAsString(firstPatient);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(firstPatientJson)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        PatientBean patient = patientService.fetchPatientById(firstPatient.getId(), validToken);

        assertEquals(firstPatient, patient);
    }

    @Test
    public void testFetchPatientByIdNotFound() throws JsonProcessingException {
        String firstPatientJson = mapper.writeValueAsString(firstPatient);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(firstPatientJson)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(PatientNotFoundException.class, () -> patientService.fetchPatientById(firstPatient.getId(), validToken));
    }

    @Test
    public void testUpdatePatient() throws JsonProcessingException {

        String updatedPatientJson = mapper.writeValueAsString(updatedPatient);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(updatedPatientJson)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        PatientBean patient = patientService.updatePatient(1, firstPatient, validToken);

        assertEquals(updatedPatient, patient);
    }

    @Test
    public void testUpdatePatientAlreadyExists() throws JsonProcessingException {
        String updatedPatientJson = mapper.writeValueAsString(updatedPatient);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(409)
                .setBody(updatedPatientJson)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(PatientAlreadyExistsException.class, () -> patientService.updatePatient(firstPatient.getId(), firstPatient, validToken));
    }

    @Test
    public void testUpdatePatientNotFound() throws JsonProcessingException {
        String updatedPatientJson = mapper.writeValueAsString(updatedPatient);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(updatedPatientJson)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(PatientNotFoundException.class, () -> patientService.updatePatient(firstPatient.getId(), firstPatient, validToken));
    }


    @Test
    public void testValidatePatient() throws JsonProcessingException {
        String addedPatient = mapper.writeValueAsString(firstPatient);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(addedPatient)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        PatientBean patient = patientService.validatePatient(firstPatient, validToken);

        assertEquals(firstPatient, patient);
    }

    @Test
    public void testValidatePatientAlreadyExists() throws JsonProcessingException {
        String addedPatient = mapper.writeValueAsString(firstPatient);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(409)
                .setBody(addedPatient)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(PatientAlreadyExistsException.class, () -> patientService.validatePatient(firstPatient, validToken));
    }

    @Test
    public void testDeletePatient() {
        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        patientService.deletePatientById(firstPatient.getId(), validToken);
    }

    @Test
    public void testDeletePatientNotFound() {
        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(PatientNotFoundException.class, () -> patientService.deletePatientById(firstPatient.getId(), validToken));
    }
}
