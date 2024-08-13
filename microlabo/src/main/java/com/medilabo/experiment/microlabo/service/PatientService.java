package com.medilabo.experiment.microlabo.service;

import com.medilabo.experiment.microlabo.domain.Patient;
import com.medilabo.experiment.microlabo.exception.PatientAlreadyExistsException;
import com.medilabo.experiment.microlabo.exception.PatientNotFoundException;
import com.medilabo.experiment.microlabo.repository.PatientRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medilabo.experiment.microlabo.util.SimpleDateUtil;

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
        if(isSamePatient(patient)) {
            throw new PatientAlreadyExistsException("Patient can't be added because a patient with the same first name, last name and birthdate combination already exists");
        }
        patient.setId(null);
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient updatePatient(Patient patient) {

        Patient patientToUpdate = getPatientById(patient.getId());

        if(isSamePatientExcludingCurrent(patient)) {
            throw new PatientAlreadyExistsException("Patient can't be added because a patient with the same first name, last name and birthdate combination already exists");
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



    /**
     * For testing purposes, we need to create Patients
     */

    @PostConstruct
    public void populateMockPatientTable() {

        List<Patient> presentPatients = patientRepository.findAll();

        if (presentPatients.isEmpty()) {

            SimpleDateUtil simpleDateUtil = new SimpleDateUtil();

            Patient firstPatient = new Patient();
            firstPatient.setLastName("TestNone");
            firstPatient.setFirstName("Test");
            firstPatient.setBirthdate(simpleDateUtil.parseDate("1966-12-31"));
            firstPatient.setGender("F");
            firstPatient.setAddress("1 Brookside St");
            firstPatient.setPhoneNumber("100-222-3333");

            Patient secondPatient = new Patient();
            secondPatient.setLastName("TestBorderline");
            secondPatient.setFirstName("Test");
            secondPatient.setBirthdate(simpleDateUtil.parseDate("1945-06-24"));
            secondPatient.setGender("M");
            secondPatient.setAddress("2 High St");
            secondPatient.setPhoneNumber("200-333-4444");

            Patient thirdPatient = new Patient();
            thirdPatient.setLastName("TestInDanger");
            thirdPatient.setFirstName("Test");
            thirdPatient.setBirthdate(simpleDateUtil.parseDate("2004-06-18"));
            thirdPatient.setGender("M");
            thirdPatient.setAddress("3 Club Road");
            thirdPatient.setPhoneNumber("300-444-5555");

            Patient fourthPatient = new Patient();
            fourthPatient.setLastName("TestEarlyOnset");
            fourthPatient.setFirstName("Test");
            fourthPatient.setBirthdate(simpleDateUtil.parseDate("2002-06-28"));
            fourthPatient.setGender("F");
            fourthPatient.setAddress("4 Valley Dr");
            fourthPatient.setPhoneNumber("400-555-6666");

            List<Patient> mockPatients = List.of(firstPatient, secondPatient, thirdPatient, fourthPatient);
            patientRepository.saveAll(mockPatients);
            System.out.println("DB populated");
        }
    }

    @PreDestroy
    private void clearDB() {
        patientRepository.deleteAll();
        patientRepository.resetAutoIncrement();
        System.out.println("DB emptied");
    }
}
