package com.medilabo.experiment.microlabo;

import com.medilabo.experiment.microlabo.domain.Patient;
import com.medilabo.experiment.microlabo.repository.PatientRepository;
import com.medilabo.experiment.microlabo.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
public class PatientServiceTest {

    @Autowired
    private PatientService patientService;

    @MockBean
    private PatientRepository patientRepository;

    private static Patient firstPatient;
    private static Patient secondPatient;
    private static List<Patient> patients;

    @BeforeEach
    public void setup() {
        LocalDate rafaelBirthdate = LocalDate.of(2017, 5, 8);
        LocalDate thaliaBirthDate = LocalDate.of(2012, 9, 7);
        firstPatient = new Patient
                (1L, "Rafael", "Doe", rafaelBirthdate, "M", "666 Devil Drive", "111-111-111");
        secondPatient = new Patient
                (2L, "Thalia", "Smith", thaliaBirthDate, "F", "777 Jackpot Road", "222-222-222");
        patients = List.of(firstPatient, secondPatient);
    }

    @Test
    public void getAllPatients_shouldReturnTheContentOfOurPatientRepository() {
        when(patientRepository.findAll()).thenReturn(patients);

        List<Patient> foundPatients = patientService.getAllPatients();

        assertEquals(patients, foundPatients);
    }

    @Test
    public void getPatientById_shouldReturnTheCorrectPatient_whenItIsFound() {
        when(patientRepository.findById(firstPatient.getId())).thenReturn(Optional.ofNullable(firstPatient));

        Patient foundPatient = patientService.getPatientById(firstPatient.getId());

        assertEquals(firstPatient, foundPatient);
    }

    @Test
    public void getPatientById_shouldReturnNull_whenThePatientIsNotFound() {
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());
        Object result = patientService.getPatientById(0);
        assertNull(result);
    }

    @Test
    public void addPatient_shouldSaveTheCorrectPatient() {
        patientService.addPatient(firstPatient);
        assertNull(firstPatient.getId());
        verify(patientRepository, times(1)).save(firstPatient);
    }

    @Test
    public void deletePatientById_shouldDeleteTheCorrectPatient() {
        patientService.addPatient(firstPatient);
        patientService.deletePatientById(1L);
        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    public void updatePatient_shouldUpdateTheCorrectPatient() {

        LocalDate updatedBirthDate = LocalDate.of(2018, 5, 8);

        Patient updatedPatient = new Patient
                (0L, "Falafel", "Smith", updatedBirthDate, "F", "555 Devil Drive", "222-111-111");

        when(patientRepository.findById(anyLong())).thenReturn(Optional.ofNullable(firstPatient));

        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient resultingPatient = patientService.updatePatient(updatedPatient);

        assertEquals(1L, resultingPatient.getId());
        assertEquals(updatedPatient.getFirstName(), resultingPatient.getFirstName());
        assertEquals(updatedPatient.getLastName(), resultingPatient.getLastName());
        assertEquals(updatedPatient.getBirthdate(), resultingPatient.getBirthdate());
        assertEquals(updatedPatient.getAddress(), resultingPatient.getAddress());
        assertEquals(updatedPatient.getGender(), resultingPatient.getGender());
        assertEquals(updatedPatient.getPhoneNumber(), resultingPatient.getPhoneNumber());

        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    public void getAgeById_shouldCalculateAgeAdequately() {
        when(patientRepository.findById(anyLong())).thenReturn(Optional.ofNullable(firstPatient));
        int age = patientService.getAgeById(1L);
        assertEquals(7, age);
    }

    @Test
    public void getBirthDateById_shouldReturnTheCorrectBirthdate() {
        when(patientRepository.findBirthdateById(anyLong())).thenReturn(firstPatient.getBirthdate());
        LocalDate foundBirthdate = patientService.getBirthdateById(1L);
        assertEquals(firstPatient.getBirthdate(), foundBirthdate);
    }

    @Test
    public void getGenderById_shouldReturnTheCorrectGender() {
        when(patientRepository.findGenderById(anyLong())).thenReturn(firstPatient.getGender());
        String foundGender = patientService.getGenderById(1L);
        assertEquals(firstPatient.getGender(), foundGender);
    }
}

