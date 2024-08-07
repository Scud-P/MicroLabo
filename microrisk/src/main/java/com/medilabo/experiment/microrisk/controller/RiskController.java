package com.medilabo.experiment.microrisk.controller;

import com.medilabo.experiment.microrisk.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/risk")
@RestController
public class RiskController {

    @Autowired
    private RiskService riskService;

    @GetMapping("/{id}")
    public ResponseEntity<String> getRiskForPatient(@PathVariable("id") Long id) {
        String risk = riskService.calculateRiskForPatient(id);
        return ResponseEntity.ok(risk);
    }

}
