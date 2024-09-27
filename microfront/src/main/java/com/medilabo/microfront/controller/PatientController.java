package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.PatientAlreadyExistsException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import com.medilabo.microfront.service.PatientService;
import com.medilabo.microfront.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;


import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PatientController handles HTTP requests related to patient management,
 * including retrieving, updating, adding, and deleting patient information.
 */
@Controller
@RequestMapping("/api")
public class PatientController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private PatientService patientService;

    @Autowired
    private RiskService riskService;


    /**
     * Gets the home page and populates it with the list of patients.
     *
     * @param token The Bearer token for authentication.
     * @param model The model to populate with patient data.
     * @return The name of the view displaying the list of patients.
     */
    @GetMapping("/home")
    public String home(@CookieValue(name = "token", required = false) String token,
                       Model model) {
        if (token == null || token.isEmpty()) {
            return "redirect:/login";
        }
        return updateModelWithPatients(token, model);
    }

    /**
     * Updates the model with the list of patients fetched using the provided token.
     *
     * @param token The Bearer token for authentication.
     * @param model The model to populate with patient data.
     * @return The name of the view to render.
     */
    public String updateModelWithPatients(String token, Model model) {
        List<PatientBean> patients = patientService.fetchPatients(token);
        model.addAttribute("patients", patients);
        return "home";
    }

    /**
     * Gets a patient by its ID and populates the model with patient information.
     *
     * @param id    The ID of the patient we want to retrieve.
     * @param model The model to be populated with patient data.
     * @param token The Bearer token for authentication.
     * @return The name of the view displaying the patient information.
     */
    @GetMapping("/patients/{id}")
    public String getPatient(@PathVariable("id") long id, Model model,
                             @CookieValue(value = "token", required = false) String token) {

        try {
            PatientBean patient = patientService.fetchPatientById(id, token);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = patient.getBirthdate().format(formatter);

            String risk = riskService.fetchRiskById(id, token);

            model.addAttribute("patient", patient);
            model.addAttribute("formattedBirthdate", formattedDate);
            model.addAttribute("risk", risk);
            return "patient";

        } catch (PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    /**
     * Shows the form to update a patient with the specified ID.
     *
     * @param id    The ID of the patient we want to update.
     * @param token The Bearer token for authentication.
     * @param model The model to be populated with patient data.
     * @return The name of the view displaying the update form.
     */
    @GetMapping("/patients/update/{id}")
    public String showUpdatePatient(@PathVariable("id") long id,
                                    @CookieValue(value = "token", required = false) String token,
                                    Model model) {
        try {
            PatientBean patient = patientService.fetchPatientById(id, token);
            model.addAttribute("patient", patient);
            return "update";

        } catch (PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    /**
     * Updates a patient with the provided information from the form.
     *
     * @param id      The ID of the patient we are updating.
     * @param patient The PatientBean containing updated information.
     * @param model   The model to be populated with patient data.
     * @param token   The Bearer token for authentication.
     * @return A ResponseEntity with the redirect location.
     */
    @PutMapping("/patients/{id}")
    public ResponseEntity<String> updatePatient(@PathVariable("id") long id,
                                                @ModelAttribute PatientBean patient,
                                                Model model,
                                                @CookieValue(value = "token", required = false) String token) {
        try {
            PatientBean updatedPatient = patientService.updatePatient(id, patient, token);
            model.addAttribute("patient", updatedPatient);
            String redirectUrl = "/api/patients/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (PatientAlreadyExistsException | PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return new ResponseEntity<>("error", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Gets the form to add a new patient
     *
     * @param model The model to be populated with patient data.
     * @return The name of the view displaying the add patient form.
     */
    @GetMapping("/patients/add")
    public String showAddPatient(Model model) {
        PatientBean patient = new PatientBean();
        model.addAttribute("patient", patient);
        return "add";
    }

    /**
     * Adds a new patient with the provided information from the form.
     *
     * @param patient The PatientBean containing the new patient information.
     * @param model   The model to be populated with patient data.
     * @param token   The Bearer token for authentication.
     * @return A ResponseEntity with the redirect location.
     */
    @PostMapping("/patients/validate")
    public String validatePatient(@ModelAttribute PatientBean patient,
                                  @CookieValue(name = "token", required = false) String token,
                                  Model model) {
        try {
            patientService.validatePatient(patient, token);
            return updateModelWithPatients(token, model);

        } catch (PatientAlreadyExistsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    /**
     * Deletes a patient with the specified ID.
     *
     * @param id    The ID of the patient to be deleted.
     * @param token The Bearer token for authentication.
     * @param model The model to be populated with patient data.
     * @return A ResponseEntity indicating the result of the deletion.
     */
    @DeleteMapping("/patients/{id}")
    public String deletePatient(@PathVariable("id") Long id,
                                @CookieValue(name = "token", required = false) String token,
                                Model model) {
        try {
            patientService.deletePatientById(id, token);
            return updateModelWithPatients(token, model);

        } catch (PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
