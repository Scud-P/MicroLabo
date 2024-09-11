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

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found for id: " + id));
    }

    @Transactional
    public Patient addPatient(Patient patient) {
        if (isSamePatient(patient)) {
            throw new PatientAlreadyExistsException("Patient can't be added because a patient with the same first name, last name and birthdate combination already exists");
        }
        patient.setId(null);
        return patientRepository.save(patient);
    }

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

    @Transactional
    public void deletePatientById(long id) {
        patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found for id: " + id));
        patientRepository.deleteById(id);
    }

    public Integer getAgeById(long id) {
        LocalDate birthdate = getPatientById(id).getBirthdate();
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    public LocalDate getBirthdateById(Long id) {
        return patientRepository.findBirthdateById(id);
    }

    public String getGenderById(Long id) {
        return patientRepository.findGenderById(id);
    }

    public boolean isSamePatient(Patient patientToAdd) {
        return patientRepository.existsPatientByFirstNameAndLastNameAndBirthdate(patientToAdd);
    }

    public boolean isSamePatientExcludingCurrent(Patient patient) {
        return patientRepository.existsByFirstNameAndLastNameAndBirthdateAndIdNot(
                patient.getFirstName(),
                patient.getLastName(),
                patient.getBirthdate(),
                patient.getId());
    }

    public boolean existsPatient(Long id) {
        return patientRepository.existsById(id);
    }
}
