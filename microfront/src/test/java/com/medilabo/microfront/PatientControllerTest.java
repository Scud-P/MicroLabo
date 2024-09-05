package com.medilabo.microfront;

import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.controller.PatientController;
import com.medilabo.microfront.exception.PatientNotFoundException;
import com.medilabo.microfront.service.PatientService;
import com.medilabo.microfront.service.RiskService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @MockBean
    private PatientService patientService;

    @MockBean
    private RiskService riskService;

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
    public void setUp() {
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
    public void testHome() {
        List<PatientBean> patients = List.of(firstPatient, secondPatient);

        when(patientService.fetchPatients(anyString())).thenReturn(patients);
        String home = patientController.home("someValidToken", model);

        assertEquals("home", home);
        verify(model, times(1)).addAttribute("patients", patients);
    }


    @Test
    public void testUpdateModelWithPatients() {
        List<PatientBean> patients = List.of(firstPatient, secondPatient);

        when(patientService.fetchPatients(anyString())).thenReturn(patients);
        String home = patientController.updateModelWithPatients("someValidToken", model);

        assertEquals("home", home);
        verify(model, times(1)).addAttribute("patients", patients);
    }

    @Test
    public void testGetPatient() {
        String risk = "Absolutely no risk here!";

        when(patientService.fetchPatientById(anyLong(), anyString())).thenReturn(firstPatient);
        when(riskService.fetchRiskById(anyLong(), anyString())).thenReturn(risk);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedBirthdate = firstPatient.getBirthdate().format(formatter);

        String result = patientController.getPatient(firstPatient.getId(), model, "someValidToken");

        assertEquals("patient", result);
        verify(model, times(1)).addAttribute("patient", firstPatient);
        verify(model, times(1)).addAttribute("risk", risk);
        verify(model, times(1)).addAttribute("formattedBirthdate", formattedBirthdate);
    }

    @Test
    public void testShowUpdatePatient() {
        when(patientService.fetchPatientById(anyLong(), anyString())).thenReturn(firstPatient);

        String result = patientController.showUpdatePatient(firstPatient.getId(), "someValidToken", model);

        assertEquals("update", result);
        verify(model, times(1)).addAttribute("patient", firstPatient);
    }

    @Test
    public void testShowUpdatePatientNotFound() {
        doThrow(new PatientNotFoundException("Oops")).when(patientService).fetchPatientById(anyLong(), anyString());

        String result = patientController.showUpdatePatient(firstPatient.getId(), "someValidToken", model);

        assertEquals("error", result);
        verify(model).addAttribute("errorMessage", "Oops");
    }

    @Test
    public void testUpdatePatient() {
        when(patientService.updatePatient(anyLong(), any(PatientBean.class), anyString())).thenReturn(firstPatient);
        String result = patientController.updatePatient(firstPatient.getId(), firstPatient, model, "someValidToken");

        assertEquals("redirect:/api/patients/1", result);
        verify(model, times(1)).addAttribute("patient", firstPatient);
    }

    @Test
    public void testShowAddPatient() {
        String result =  patientController.showAddPatient(model);
        assertEquals("add", result);
        verify(model, times(1)).addAttribute("patient", new PatientBean());
    }


}


