package com.medilabo.microlabo;

import com.medilabo.microlabo.domain.Patient;
import com.medilabo.microlabo.exception.PatientAlreadyExistsException;
import com.medilabo.microlabo.exception.PatientNotFoundException;
import com.medilabo.microlabo.repository.PatientRepository;
import com.medilabo.microlabo.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ActiveProfiles("test")
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
    public void getPatientById_shouldThrowPatientNotFoundException_whenPatientIsNotFound() {
        long someIdThatDoesNotExist = 5L;
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(PatientNotFoundException.class, () -> patientService.getPatientById(someIdThatDoesNotExist));
    }

    @Test
    public void addPatient_shouldSaveTheCorrectPatient() {
        when(patientRepository.existsPatientByFirstNameAndLastNameAndBirthdate(firstPatient)).thenReturn(false);
        patientService.addPatient(firstPatient);
        assertNull(firstPatient.getId());
        verify(patientRepository, times(1)).save(firstPatient);
    }

    @Test
    public void addPatient_shouldThrowException_WhenPatientAlreadyExists() {
        when(patientRepository.existsPatientByFirstNameAndLastNameAndBirthdate(firstPatient)).thenReturn(true);
        assertThrows(PatientAlreadyExistsException.class, () -> patientService.addPatient(firstPatient));
        verify(patientRepository, times(0)).save(firstPatient);
    }

    @Test
    public void deletePatientById_shouldDeleteTheCorrectPatient() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(firstPatient));
        patientService.deletePatientById(1L);
        verify(patientRepository, times(1)).deleteById(firstPatient.getId());
    }

    @Test
    public void updatePatient_shouldUpdateTheCorrectPatient() {

        LocalDate updatedBirthDate = LocalDate.of(2018, 5, 8);

        Patient updatedPatient = new Patient
                (0L, "Falafel", "Smith", updatedBirthDate, "F", "555 Devil Drive", "222-111-111");

        when(patientRepository.findById(anyLong())).thenReturn(Optional.ofNullable(firstPatient));
        when(patientRepository.existsByFirstNameAndLastNameAndBirthdateAndIdNot(anyString(), anyString(), any(LocalDate.class), anyLong())).thenReturn(false);

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
    public void updatePatient_shouldThrowPatientAlreadyExistsException_whenPatientAlreadyExistsReturnsTrue() {
        LocalDate updatedBirthDate = LocalDate.of(2018, 5, 8);
        Patient updatedPatient = new Patient
                (0L, "Falafel", "Smith", updatedBirthDate, "F", "555 Devil Drive", "222-111-111");

        when(patientRepository.findById(anyLong())).thenReturn(Optional.ofNullable(firstPatient));
        when(patientRepository.existsByFirstNameAndLastNameAndBirthdateAndIdNot(anyString(), anyString(), any(LocalDate.class), anyLong())).thenReturn(true);

        assertThrows(PatientAlreadyExistsException.class, () -> patientService.updatePatient(updatedPatient));
        verify(patientRepository, never()).save(any(Patient.class));
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

    @Test
    public void testIsSamePatient() {
        when(patientRepository.existsPatientByFirstNameAndLastNameAndBirthdate(firstPatient)).thenReturn(true);
        boolean result = patientService.isSamePatient(firstPatient);
        assertTrue(result);
    }

    @Test
    public void testIsSamePatientExcludingCurrent() {
        when(patientRepository.existsByFirstNameAndLastNameAndBirthdateAndIdNot
                (anyString(), anyString(), any(LocalDate.class), anyLong())).thenReturn(true);
        boolean result = patientService.isSamePatientExcludingCurrent(firstPatient);
        assertTrue(result);
    }
}

