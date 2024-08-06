package com.medilabo.microfront;

import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.controller.PatientController;
import com.medilabo.microfront.proxies.MicroLaboProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientController patientController;

    @MockBean
    private MicroLaboProxy microLaboProxy;

    private static PatientBean firstPatient;
    private static PatientBean secondPatient;
    private static List<PatientBean> patients;

    @BeforeEach
    public void setUp() {
        LocalDate rafaelBirthdate = LocalDate.of(2017, 5, 8);
        LocalDate thaliaBirthDate = LocalDate.of(2012, 9, 7);
        firstPatient = new PatientBean
                (1L, "Rafael", "Doe", rafaelBirthdate, "M", "666 Devil Drive", "111-111-111");
        secondPatient = new PatientBean
                (2L, "Thalia", "Smith", thaliaBirthDate, "F", "777 Jackpot Road", "222-222-222");
        patients = List.of(firstPatient, secondPatient);
    }

    @Test
    public void homeTest() throws Exception {

        when(microLaboProxy.getAllPatients()).thenReturn(patients);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("patients"));
    }

    @Test
    public void getRequest_toShowUpdateForm_shouldDisplayThePatientUpdateForm() throws Exception {

        when(microLaboProxy.getPatientById(anyLong())).thenReturn(firstPatient);

        mockMvc.perform(get("/patients/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update"))
                .andExpect(model().attributeExists("patient"));
    }

    //TODO CHECK IF THE METHOD UPDATES THE PATIENT IN UI & DB

    @Test
    public void putRequest_toUpdatePatient_shouldUpdateTheCorrectPatient() throws Exception {

        mockMvc.perform(put("/patients/update")
                        .param("birthdate", "2017-09-07")
                        .flashAttr("patient", secondPatient))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(microLaboProxy, times(1)).updatePatient(secondPatient);
    }

    @Test
    public void getRequest_toAddPatient_ShouldCreateANewPatientBean_AndAddItToTheModel() throws Exception {

        mockMvc.perform(get("/patients/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attribute("patient", hasProperty("id", is(0L))))
                .andExpect(model().attribute("patient", hasProperty("firstName", is(emptyOrNullString()))))
                .andExpect(model().attribute("patient", hasProperty("lastName", is(emptyOrNullString()))))
                .andExpect(model().attribute("patient", hasProperty("birthdate", nullValue())))
                .andExpect(model().attribute("patient", hasProperty("gender", is(emptyOrNullString()))))
                .andExpect(model().attribute("patient", hasProperty("address", is(emptyOrNullString()))))
                .andExpect(model().attribute("patient", hasProperty("phoneNumber", is(emptyOrNullString()))));
    }

    @Test
    public void postRequest_toValidatePatient_shouldValidateTheNewPatient_AndAddItToThePatientList() throws Exception {

        when(microLaboProxy.validatePatient(any(PatientBean.class))).thenReturn(firstPatient);

        List<PatientBean> testPatients = new ArrayList<>();
        testPatients.add(firstPatient);

        when(microLaboProxy.getAllPatients()).thenReturn(testPatients);

        mockMvc.perform(post("/patients/validate")
                        .flashAttr("patient", firstPatient))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));


        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("patients"))
                .andExpect(model().attribute("patients", hasSize(1)));
    }

    @Test
    public void deleteRequest_toDeletePatient_shouldDeleteTheCorrectPatient() throws Exception {

        mockMvc.perform(delete("/patients/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(microLaboProxy, times(1)).deletePatient(1L);
    }
}
