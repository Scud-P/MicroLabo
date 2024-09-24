package com.medilabo.microrisk.controller;

import com.medilabo.microrisk.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


/**
 * REST controller for managing patients risk level.
 * Provides an endpoint to calculate the risk level for a specific patient.
 */
@RequestMapping("/risk")
@RestController
public class RiskController {

    @Autowired
    private RiskService riskService;

    /**
     * Retrieves the risk level for a patient by their ID.
     *
     * @param id the ID of the patient
     * @return a  ResponseEntity containing the calculated risk level as a String
     */
//    @GetMapping("/{id}")
//    public ResponseEntity<String> getRiskForPatient(@PathVariable("id") Long id) {
//        String risk = riskService.calculateRiskForPatient(id);
//        System.out.println("Id received in the RiskController" + id);
//        return ResponseEntity.ok(risk);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getRiskForPatient(@PathVariable("id") Long id) {
        LocalDate birthDate = riskService.fetchBirthDate(id);
        String gender = riskService.fetchGender(id);
        String risk = riskService.calculateRiskForPatient(id, birthDate, gender);
        System.out.println("Id received in the RiskController" + id);
        return ResponseEntity.ok(risk);
    }

    @GetMapping("/fetchBirthdate/{id}")
    public LocalDate fetchBirthDate(@PathVariable Long id) {
        return riskService.fetchBirthDate(id);
    }

    @GetMapping("/fetchGender/{id}")
    public String fetchGender(@PathVariable Long id) {
        return riskService.fetchGender(id);
    }

}
