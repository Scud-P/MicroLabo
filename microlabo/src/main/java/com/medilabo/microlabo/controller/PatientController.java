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

@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/list")
    public List<Patient> patients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable("id") Long id) {
        return patientService.getPatientById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable("id") Long id, @RequestBody Patient patient) {
        if (!id.equals(patient.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Patient updatedPatient = patientService.updatePatient(patient);
        return ResponseEntity.ok(updatedPatient);
    }

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

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable("id") Long id) {
        Patient patient = patientService.getPatientById(id);
        if (patient == null) throw new PatientNotFoundException("Patient with id " + id + " not found.");
        patientService.deletePatientById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/birthdate")
    public ResponseEntity<LocalDate> getBirthdate(@PathVariable("id") Long id) {
        LocalDate birthdate = patientService.getBirthdateById(id);
        return ResponseEntity.ok(birthdate);
    }

    @GetMapping(value = "/{id}/gender")
    public ResponseEntity<String> getGender(@PathVariable("id") Long id) {
        String gender = patientService.getGenderById(id);
        return ResponseEntity.ok(gender);
    }

    @GetMapping(value = "/{id}/exists")
    public ResponseEntity<Boolean> checkExists(@PathVariable("id") Long id) {
        boolean exists = patientService.existsPatient(id);
        return ResponseEntity.ok(exists);
    }

}
