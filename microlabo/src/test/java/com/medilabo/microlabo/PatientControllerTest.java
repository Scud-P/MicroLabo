package com.medilabo.microlabo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medilabo.microlabo.controller.PatientController;
import com.medilabo.microlabo.domain.Patient;
import com.medilabo.microlabo.exception.PatientNotFoundException;
import com.medilabo.microlabo.service.PatientService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PatientControllerTest {

    @Autowired
    private PatientController patientController;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    private static Patient firstPatient;
    private static Patient secondPatient;
    private static List<Patient> patients;

    @BeforeAll
    public static void setup() {
        LocalDate rafaelBirthdate = LocalDate.of(2017, 5, 8);
        LocalDate thaliaBirthDate = LocalDate.of(2012, 9, 7);
        firstPatient = new Patient
                (1L, "Rafael", "Doe", rafaelBirthdate, "M", "666 Devil Drive", "111-111-111");
        secondPatient = new Patient
                (2L, "Thalia", "Smith", thaliaBirthDate, "F", "777 Jackpot Road", "222-222-222");
        patients = List.of(firstPatient, secondPatient);
    }

    @Test
    public void getAllPatients_shouldReturnAllPatients() throws Exception {

        when(patientService.getAllPatients()).thenReturn(patients);

        mockMvc.perform(get("/patients/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(firstPatient.getId().intValue())))
                .andExpect(jsonPath("$[0].firstName", is(firstPatient.getFirstName())))
                .andExpect(jsonPath("$[0].lastName", is(firstPatient.getLastName())))
                .andExpect(jsonPath("$[0].birthdate", is(firstPatient.getBirthdate().toString())))
                .andExpect(jsonPath("$[0].gender", is(firstPatient.getGender())))
                .andExpect(jsonPath("$[0].address", is(firstPatient.getAddress())))
                .andExpect(jsonPath("$[0].phoneNumber", is(firstPatient.getPhoneNumber())))
                .andExpect(jsonPath("$[1].id", is(secondPatient.getId().intValue())))
                .andExpect(jsonPath("$[1].firstName", is(secondPatient.getFirstName())))
                .andExpect(jsonPath("$[1].lastName", is(secondPatient.getLastName())))
                .andExpect(jsonPath("$[1].birthdate", is(secondPatient.getBirthdate().toString())))
                .andExpect(jsonPath("$[1].gender", is(secondPatient.getGender())))
                .andExpect(jsonPath("$[1].address", is(secondPatient.getAddress())))
                .andExpect(jsonPath("$[1].phoneNumber", is(secondPatient.getPhoneNumber())));
    }

    @Test
    public void getPatientById_shouldReturnASpecificPatient_whenTheIdExists() throws Exception {

        when(patientService.getPatientById(firstPatient.getId())).thenReturn(firstPatient);

        mockMvc.perform(get("/patients/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(firstPatient.getId()))
                .andExpect(jsonPath("$.firstName").value(firstPatient.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(firstPatient.getLastName()))
                .andExpect(jsonPath("$.birthdate").value(firstPatient.getBirthdate().toString()))
                .andExpect(jsonPath("$.gender").value(firstPatient.getGender()))
                .andExpect(jsonPath("$.address").value(firstPatient.getAddress()))
                .andExpect(jsonPath("$.phoneNumber").value(firstPatient.getPhoneNumber()));

        verify(patientService, times(1)).getPatientById(1);
    }

    @Test
    public void getPatientById_shouldThrowPatientNotFoundException_whenIdDoesNotExist() {
        when(patientService.getPatientById(anyLong())).thenThrow(new PatientNotFoundException("That patient definitely does not exist"));
        // Only two users with ID 1 and 2 in our testing environment
        assertThrows(PatientNotFoundException.class, () -> patientController.getPatientById(0L));
    }


    @Test
    public void postRequestToValidatePatient_shouldReturnCreatedCode_whenPatientIsCreated() throws Exception {

        when(patientService.addPatient(any(Patient.class))).thenReturn(firstPatient);

        mockMvc.perform(post("/patients/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(firstPatient)))
                .andExpect(status().isCreated());
    }

    @Test
    public void putRequestToUpdatePatient_shouldReturnOK_whenPatientIsSuccessfullyUpdated() throws Exception {

        when(patientService.updatePatient(any(Patient.class))).thenReturn(firstPatient);

        mockMvc.perform(put("/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(firstPatient)))
                .andExpect(status().isOk());
    }

    @Test
    public void putRequestToUpdatePatient_shouldReturnBadRequest_ifIdsDoNotMatch() throws Exception {

        when(patientService.updatePatient(any(Patient.class))).thenReturn(firstPatient);

        mockMvc.perform(put("/patients/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(firstPatient)))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void deleteRequestToDeletePatient_shouldDeletePatient_whenPatientIsFound() throws Exception {
        when(patientService.getPatientById(anyLong())).thenReturn(firstPatient);
        mockMvc.perform(delete("/patients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteRequestToDeletePatient_shouldThrowPatientNotFoundException_whenPatientIsNotFound() {
        assertThrows(PatientNotFoundException.class, () -> patientController.deletePatient(0L));
    }

    @Test
    public void getBirthdate_shouldReturnTheCorrectBirthdate() throws Exception {

        String expectedBirthdate = "\"" + firstPatient.getBirthdate().toString() + "\"";
        when(patientService.getBirthdateById(anyLong())).thenReturn(firstPatient.getBirthdate());

        mockMvc.perform(get("/patients/1/birthdate"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedBirthdate));
    }

    @Test
    public void getGender_shouldReturnTheCorrectGender() throws Exception {
        String expectedGender = firstPatient.getGender();
        when(patientService.getGenderById(anyLong())).thenReturn(firstPatient.getGender());

        mockMvc.perform(get("/patients/1/gender"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedGender));
    }

    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
