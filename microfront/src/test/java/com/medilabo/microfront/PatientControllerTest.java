package com.medilabo.microfront;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.controller.PatientController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PatientControllerTest {

    @InjectMocks
    private PatientController patientController;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient webClient;

    @Mock(answer = Answers.RETURNS_SELF)
    private WebClient.Builder webClientBuilder;

    @Mock
    private Model model;

    private static PatientBean firstPatient;
    private static PatientBean secondPatient;

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
    public void testHome() {

        List<PatientBean> patients = List.of(firstPatient, secondPatient);

        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.get()
                .uri("http://localhost:8081/patients")
                .retrieve()
                .bodyToFlux(PatientBean.class)
                .collectList()
                .block()).thenReturn(patients);

        String result = patientController.home(model);

        assertEquals("home", result);
        verify(model).addAttribute("patients", patients);

    }

    @Test
    public void testShowUpdatePatient() {
        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block()).thenReturn(firstPatient);

        String result = patientController.showUpdatePatient(1L, model);

        assertEquals("update", result);
    }

    @Test
    public void testUpdatePatient() {

        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.put()
                .uri("http://localhost:8081/patients/{id}", "1")
                .bodyValue(anyString())
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block()).thenReturn(firstPatient);

        String result = patientController.updatePatient(1L, firstPatient, model);

        assertEquals("redirect:/patients/1", result);
    }

    @Test
    public void testShowAddPatient() {
        PatientBean patientBean = new PatientBean();
        String result = patientController.showAddPatient(model);
        assertEquals("add", result);
        verify(model, times(1)).addAttribute("patient", patientBean);
    }


    @Test
    public void testValidatePatientFail() throws URISyntaxException {

        when(webClientBuilder.build()).thenReturn(webClient);

        Throwable cause = new RuntimeException("Simulated request failure");
        WebClientRequestException exception = new WebClientRequestException(
                cause,
                HttpMethod.POST,
                new URI("http://localhost:8081/patients/validate"),
                new HttpHeaders()
        );


        when(webClient.post()
                .uri("http://localhost:8081/patients/validate")
                .bodyValue(any(PatientBean.class))
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block()).thenThrow(exception);


        String result = patientController.validatePatient(firstPatient, model);

        verify(model).addAttribute(eq("error"), eq("Failed to validate patient: Simulated request failure"));

        assertEquals("error", result);
    }

    @Test
    public void testValidatePatient() {

        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.post()
                .uri("http://localhost:8081/patients/validate")
                .bodyValue(anyString())
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block()).thenReturn(firstPatient);

        String result = patientController.validatePatient(firstPatient, model);

        verify(model).addAttribute(eq("patients"), anyList());

        assertEquals("home", result);
    }

    @Test
    public void testDeletePatient() throws URISyntaxException {

        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.delete()
                .uri("http://localhost:8081/patients/{id}", "1")
                .retrieve()
                .toBodilessEntity()
                .block())
                .thenReturn(ResponseEntity.ok().build());

        String result = patientController.deletePatient(1L, model);

        verify(model).addAttribute(eq("patients"), anyList());
        assertEquals("home", result);
    }

    @Test
    public void testGetPatient() {

        Long patientId = 1L;

        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.get()
                .uri("http://localhost:8081/patients/{id}", patientId)
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block())
                .thenReturn(firstPatient);

        when(webClient.get()
                .uri("http://localhost:8084/risk/{patientId}", patientId)
                .retrieve()
                .bodyToMono(String.class)
                .block())
                .thenReturn("None");

        String result = patientController.getPatient(patientId, model);

        verify(model, times(1)).addAttribute("patient", firstPatient);

        String formattedDate = firstPatient.getBirthdate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        verify(model, times(1)).addAttribute("formattedBirthdate", formattedDate);

        verify(model, times(1)).addAttribute("risk", "None");
        assertEquals("patient", result);
    }
}
