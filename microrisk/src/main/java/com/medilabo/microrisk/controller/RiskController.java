package com.medilabo.microrisk.controller;

import com.medilabo.microrisk.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
