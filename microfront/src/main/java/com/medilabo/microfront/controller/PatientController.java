package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.PatientAlreadyExistsException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import com.medilabo.microfront.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/api")
public class PatientController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private PatientService patientService;

    @GetMapping("/home")
    public String home(@CookieValue(name = "token", required = false) String token,
                       Model model) {
        if (token == null || token.isEmpty()) {
            return "redirect:/login";
        }
        return updateModelWithPatients(token, model);
    }

//    public List<PatientBean> fetchPatients(String token) {
//
//        WebClient webClient = webClientBuilder.build();
//        return webClient.get()
//                .uri("/patients/list")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .retrieve()
//                .bodyToFlux(PatientBean.class)
//                .collectList()
//                .block();
//    }

    public String updateModelWithPatients(String token, Model model) {
        List<PatientBean> patients = patientService.fetchPatients(token);
        model.addAttribute("patients", patients);
        return "home";
    }

    @GetMapping("/patients/{id}")
    public String getPatient(@PathVariable("id") long id, Model model,
                             @CookieValue(value = "token", required = false) String token) {

        try {
            PatientBean patient = webClientBuilder.build()
                    .get()
                    .uri("/patients/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> Mono.error(new PatientNotFoundException("Patient not found for id: " + id)))
                    .bodyToMono(PatientBean.class)
                    .block();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = patient.getBirthdate().format(formatter);

            String risk = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8080/risk/{patientId}", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            model.addAttribute("patient", patient);
            model.addAttribute("formattedBirthdate", formattedDate);
            model.addAttribute("risk", risk);
            return "patient";

        } catch (PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }



    @GetMapping("/patients/update/{id}")
    public String showUpdatePatient(@PathVariable("id") long id,
                                    @CookieValue(value = "token", required = false) String token,
                                    Model model) {
        try {
            PatientBean patient = webClientBuilder.build()
                    .get()
                    .uri("/patients/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> Mono.error(new PatientNotFoundException("Patient not found for id: " + id)))
                    .bodyToMono(PatientBean.class)
                    .block();

            model.addAttribute("patient", patient);
            return "update";

        } catch (PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @PutMapping("/patients/{id}")
    public String updatePatient(@PathVariable("id") long id,
                                @ModelAttribute PatientBean patient,
                                Model model,
                                @CookieValue(value = "token", required = false) String token) {
        try {
            WebClient webClient = webClientBuilder.build();

            PatientBean updatedPatient = webClient.put()
                    .uri("/patients/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(patient)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> {
                                if (clientResponse.statusCode() == HttpStatusCode.valueOf(409)) {
                                    return Mono.error(new PatientAlreadyExistsException(
                                            "Patient can't be updated because a patient with the same first name, last name and birthdate combination already exists"));
                                } else {
                                    return Mono.error(new PatientNotFoundException("Patient not found for id: " + id));
                                }
                            })
                    .bodyToMono(PatientBean.class)
                    .block();
            model.addAttribute("patient", updatedPatient);
            return "redirect:/api/patients/" + id;

        } catch (PatientAlreadyExistsException | PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/patients/add")
    public String showAddPatient(Model model) {
        PatientBean patient = new PatientBean();
        model.addAttribute("patient", patient);
        return "add";
    }

    @PostMapping("/patients/validate")
    public String validatePatient(@ModelAttribute PatientBean patient,
                                  @CookieValue(name = "token", required = false) String token,
                                  Model model) {
        WebClient webClient = webClientBuilder.build();
        try {
            webClient.post()
                    .uri("/patients/validate")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(patient)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> Mono.error(new PatientAlreadyExistsException(
                                    "Patient can't be added because a patient with the same first name, last name and birthdate combination already exists")))
                    .bodyToMono(PatientBean.class)
                    .block();
            return updateModelWithPatients(token, model);

        } catch (PatientAlreadyExistsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @DeleteMapping("/patients/{id}")
    public String deletePatient(@PathVariable("id") Long id,
                                @CookieValue(name = "token", required = false) String token,
                                Model model) {
        WebClient webClient = webClientBuilder.build();

        try {
            webClient.delete()
                    .uri("/patients/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> Mono.error(new PatientNotFoundException(
                                    "Patient not found for id: " + id)))
                    .toBodilessEntity()
                    .block();
            return updateModelWithPatients(token, model);

        } catch (PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
