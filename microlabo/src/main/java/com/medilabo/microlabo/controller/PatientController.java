package com.medilabo.microlabo.controller;

import com.medilabo.microlabo.domain.Patient;
import com.medilabo.microlabo.exception.PatientNotFoundException;
import com.medilabo.microlabo.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing patients.
 * Provides endpoints for retrieving, creating, updating, and deleting patients.
 */
@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * Retrieves the list of all patients.
     *
     * @return a list of all Patient
     */
    @GetMapping("/list")
    public List<Patient> patients() {
        return patientService.getAllPatients();
    }

    /**
     * Retrieves a patient by their ID.
     *
     * @param id the ID of the patient to retrieve
     * @return the Patient with the given ID
     */
    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable("id") Long id) {
        return patientService.getPatientById(id);
    }

    /**
     * Updates an existing patient.
     *
     * @param id the ID of the patient to update
     * @param patient the Patient object containing updated information
     * @return a ResponseEntity with the updated patient or a bad request status if IDs do not match
     */
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable("id") Long id, @RequestBody Patient patient) {
        if (!id.equals(patient.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Patient updatedPatient = patientService.updatePatient(patient);
        return ResponseEntity.ok(updatedPatient);
    }

    /**
     * Validates the creation of a new patient.
     *
     * @param patient the Patient to create
     * @return a ResponseEntity with the location of the created patient
     */
    @PostMapping("/validate")
    public ResponseEntity<Patient> validatePatient(@RequestBody Patient patient) {
        Patient addedPatient = patientService.addPatient(patient);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(addedPatient.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    /**
     * Deletes a patient by their ID.
     *
     * @param id the ID of the patient to delete
     * @return a ResponseEntity with no content if deletion is successful
     * @throws PatientNotFoundException if the patient with the given ID is not found
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable("id") Long id) {
        Patient patient = patientService.getPatientById(id);
        if (patient == null) throw new PatientNotFoundException("Patient with id " + id + " not found.");
        patientService.deletePatientById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves the birthdate of a patient by their ID.
     *
     * @param id the ID of the patient
     * @return a ResponseEntity containing the birthdate of the patient
     */
    @GetMapping(value = "/{id}/birthdate")
    public ResponseEntity<LocalDate> getBirthdate(@PathVariable("id") Long id) {
        LocalDate birthdate = patientService.getBirthdateById(id);
        return ResponseEntity.ok(birthdate);
    }

    /**
     * Retrieves the gender of a patient by their ID.
     *
     * @param id the ID of the patient
     * @return a ResponseEntity containing the gender of the patient
     */
    @GetMapping(value = "/{id}/gender")
    public ResponseEntity<String> getGender(@PathVariable("id") Long id) {
        String gender = patientService.getGenderById(id);
        return ResponseEntity.ok(gender);
    }

    /**
     * Checks if a patient exists by their ID.
     *
     * @param id the ID of the patient
     * @return a ResponseEntity containing true if the patient exists, otherwise false
     */
    @GetMapping(value = "/{id}/exists")
    public ResponseEntity<Boolean> checkExists(@PathVariable("id") Long id) {
        boolean exists = patientService.existsPatient(id);
        return ResponseEntity.ok(exists);
    }
}