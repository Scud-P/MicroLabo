package com.medilabo.microlabo.service;

import com.medilabo.microlabo.exception.PatientAlreadyExistsException;
import com.medilabo.microlabo.exception.PatientNotFoundException;
import com.medilabo.microlabo.repository.PatientRepository;
import com.medilabo.microlabo.domain.Patient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Service class for managing patients.
 * Provides methods to create, update, delete, and retrieve patient information.
 */
@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    /**
     * Retrieves all patients.
     *
     * @return a list of all Patient
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Retrieves a patient by their ID.
     *
     * @param id the ID of the patient to retrieve
     * @return the Patient with the given ID
     * @throws PatientNotFoundException if no patient is found with the given ID
     */
    public Patient getPatientById(long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found for id: " + id));
    }

    /**
     * Adds a new patient.
     *
     * @param patient the Patient
     * @return the added Patient
     * @throws PatientAlreadyExistsException if a patient with the same first name, last name, and birthdate already exists
     */
    @Transactional
    public Patient addPatient(Patient patient) {
        if (isSamePatient(patient)) {
            throw new PatientAlreadyExistsException("Patient can't be added because a patient with the same first name, last name and birthdate combination already exists");
        }
        patient.setId(null);
        return patientRepository.save(patient);
    }

    /**
     * Updates an existing patient.
     *
     * @param patient the Patient object containing updated information
     * @return the updated Patient
     * @throws PatientAlreadyExistsException if a patient with the same first name, last name, and birthdate already exists (excluding the current patient)
     */
    @Transactional
    public Patient updatePatient(Patient patient) {

        Patient patientToUpdate = getPatientById(patient.getId());

        if (isSamePatientExcludingCurrent(patient)) {
            throw new PatientAlreadyExistsException("Patient can't be updated because a patient with the same first name, last name and birthdate combination already exists");
        }

        patientToUpdate.setFirstName(patient.getFirstName());
        patientToUpdate.setLastName(patient.getLastName());
        patientToUpdate.setBirthdate(patient.getBirthdate());
        patientToUpdate.setGender(patient.getGender());
        patientToUpdate.setAddress(patient.getAddress());
        patientToUpdate.setPhoneNumber(patient.getPhoneNumber());
        return patientRepository.save(patientToUpdate);
    }

    /**
     * Deletes a patient by their ID.
     *
     * @param id the ID of the patient to delete
     * @throws PatientNotFoundException if no patient is found with the given ID
     */
    @Transactional
    public void deletePatientById(long id) {
        patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found for id: " + id));
        patientRepository.deleteById(id);
    }

    /**
     * Retrieves the age of a patient by their ID.
     *
     * @param id the ID of the patient
     * @return the age of the patient
     */
    public Integer getAgeById(long id) {
        LocalDate birthdate = getPatientById(id).getBirthdate();
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    /**
     * Retrieves the birthdate of a patient by their ID.
     *
     * @param id the ID of the patient
     * @return the birthdate of the patient
     */
    public LocalDate getBirthdateById(Long id) {
        LocalDate birthdate = patientRepository.findBirthdateById(id);
        System.out.println("Birthdate found by service in repo: " + birthdate);
        return birthdate;
    }

    /**
     * Retrieves the gender of a patient by their ID.
     *
     * @param id the ID of the patient
     * @return the gender of the patient
     */
    public String getGenderById(Long id) {
        String gender = patientRepository.findGenderById(id);
        System.out.println("Gender found by service in repo: " + gender);
        return gender;
    }

    /**
     * Checks if a patient with the same first name, last name, and birthdate already exists.
     *
     * @param patientToAdd the Patient to check
     * @return true if a patient with the same first name, last name, and birthdate exists, otherwise false
     */
    public boolean isSamePatient(Patient patientToAdd) {
        return patientRepository.existsPatientByFirstNameAndLastNameAndBirthdate(patientToAdd);
    }

    /**
     * Checks if a patient with the same first name, last name, and birthdate exists, excluding the current patient.
     * it avoids throwing {@link PatientAlreadyExistsException} when changing the fields that have most chances of changing (phone, email, address).
     *
     * @param patient the {@link Patient} to check
     * @return true if a patient with the same first name, last name, and birthdate exists, excluding the current patient, otherwise false
     */
    public boolean isSamePatientExcludingCurrent(Patient patient) {
        return patientRepository.existsByFirstNameAndLastNameAndBirthdateAndIdNot(
                patient.getFirstName(),
                patient.getLastName(),
                patient.getBirthdate(),
                patient.getId());
    }

    /**
     * Checks if a patient exists by their ID.
     *
     * @param id the ID of the patient
     * @return true if the patient exists, otherwise false
     */
    public boolean existsPatient(Long id) {
        return patientRepository.existsById(id);
    }
}
