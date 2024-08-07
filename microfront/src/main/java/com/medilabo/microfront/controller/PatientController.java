package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.PatientBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class PatientController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("")
    public String home(Model model) {
        return updateModelWithPatients(model);
    }

    @GetMapping("/patients/{id}")
    public String getPatient(@PathVariable("id") long id, Model model) {
        PatientBean patient = webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/patients/{id}", id)
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = patient.getBirthdate().format(formatter);

        String risk = webClientBuilder.build()
                .get()
                .uri("http://localhost:8084/risk/{patientId}", id)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        model.addAttribute("patient", patient);
        model.addAttribute("formattedBirthdate", formattedDate);
        model.addAttribute("risk", risk);
        return "patient";
    }

    @GetMapping("/patients/update/{id}")
    public String showUpdatePatient(@PathVariable("id") long id, Model model) {
        PatientBean patient = webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/patients/{id}", id)
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block();

        model.addAttribute("patient", patient);
        return "update";
    }

    @PutMapping("/patients/{id}")
    public String updatePatient(@PathVariable("id") Long id, @ModelAttribute PatientBean patient, Model model) {
        WebClient webClient = webClientBuilder.build();

        PatientBean updatedPatient = webClient.put()
                .uri("http://localhost:8081/patients/{id}", id)
                .bodyValue(patient)
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block();

        model.addAttribute("patient", updatedPatient);

        return "redirect:/patients/" + id;
    }

    @GetMapping("/patients/add")
    public String showAddPatient(Model model) {
        PatientBean patient = new PatientBean();
        model.addAttribute("patient", patient);
        return "add";
    }

    @PostMapping("/patients/validate")
    public String validatePatient(@ModelAttribute PatientBean patient, Model model) {

        WebClient webClient = webClientBuilder.build();

        try {
            webClient.post()
                    .uri("http://localhost:8081/patients/validate")
                    .bodyValue(patient)
                    .retrieve()
                    .bodyToMono(PatientBean.class)
                    .block();
        } catch (WebClientException e) {
            model.addAttribute("error", "Failed to validate patient: " + e.getMessage());
            return "error";
        }
        return updateModelWithPatients(model);
    }

    @DeleteMapping("/patients/{id}")
    public String deletePatient(@PathVariable("id") Long id, Model model) {
        WebClient webClient = webClientBuilder.build();

        try {
            webClient.delete()
                    .uri("http://localhost:8081/patients/{id}", id)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientException e) {
            model.addAttribute("error", "Failed to delete patient: " + e.getMessage());
            return "error";
        }
        return updateModelWithPatients(model);
    }

    private List<PatientBean> fetchPatients() {
        WebClient webClient = webClientBuilder.build();
        return webClient.get()
                .uri("http://localhost:8081/patients")
                .retrieve()
                .bodyToFlux(PatientBean.class)
                .collectList()
                .block();
    }

    private String updateModelWithPatients(Model model) {
        List<PatientBean> patients = fetchPatients();
        model.addAttribute("patients", patients);
        return "home";
    }

}
