package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.PatientAlreadyExistsException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import com.medilabo.microfront.service.PatientService;
import com.medilabo.microfront.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;


import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/api")
public class PatientController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private PatientService patientService;

    @Autowired
    private RiskService riskService;


    @GetMapping("/home")
    @Operation(summary = "Gets the home page and populate it with the list of patients",
            description = "Retrieves all patients found in the MySQL DB",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"))},
            responses = {
                    @ApiResponse(
                            description = "HTML page displaying the list of patients",
                            content = @Content(
                                    mediaType = "text/html"
                            )),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Redirect to login when the Auth token is not found",
                            content = @Content(
                                    mediaType = "text/html"
                            ))})
    public String home(@CookieValue(name = "token", required = false) String token,
                       Model model) {
        if (token == null || token.isEmpty()) {
            return "redirect:/login";
        }
        return updateModelWithPatients(token, model);
    }

    public String updateModelWithPatients(String token, Model model) {
        List<PatientBean> patients = patientService.fetchPatients(token);
        model.addAttribute("patients", patients);
        return "home";
    }

    @GetMapping("/patients/{id}")
    @Operation(summary = "Gets a patient by its ID",
            description = "Retrieves a patient's information by its ID",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "id",
                            description = "The id of the patient we want to retrieve",
                            required = false,
                            schema = @Schema(type = "long"))},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "HTML page displaying the patient information",
                            content = @Content(
                                    mediaType = "text/html"
                            )),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The error page with a message if the patient is not found",
                            content = @Content(
                                    mediaType = "text/html"
                            ))})
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

    @GetMapping("/patients/update/{id}")
    @Operation(summary = "Gets the form to update a patient with his ID",
            description = "Gets the form to update a patient's information with his ID",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "id",
                            description = "The id of the patient we want to update",
                            schema = @Schema(type = "long"))},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "HTML page displaying the modifiable patient information",
                            content = @Content(
                                    mediaType = "text/html"
                            )),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The error page with a message if the patient is not found",
                            content = @Content(
                                    mediaType = "text/html"
                            ))
            })
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

    @PutMapping("/patients/{id}")
    @Operation(summary = "Updates a patient with his ID",
            description = "Updates a patient with the new information provided in the form",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "id",
                            description = "The id of the patient we are updating",
                            schema = @Schema(type = "long"))},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "HTML page displaying the new patient information",
                            content = @Content(
                                    mediaType = "text/html"
                            )),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The error page with a message if the patient is not found",
                            content = @Content(
                                    mediaType = "text/html"
                            )),
                    @ApiResponse(
                            responseCode = "409",
                            description = "The error page with a message in case of conflict",
                            content = @Content(
                                    mediaType = "text/html"
                            ))
            })
    public String updatePatient(@PathVariable("id") long id,
                                @ModelAttribute PatientBean patient,
                                Model model,
                                @CookieValue(value = "token", required = false) String token) {
        try {
            PatientBean updatedPatient = patientService.updatePatient(id, patient, token);
            model.addAttribute("patient", updatedPatient);

            // TODO TRY REDIRECTING TO redirect:http://gateway/api/patients/" + id
            return "redirect:http://192.168.0.22:8080/api/patients/" + id;

        } catch (PatientAlreadyExistsException | PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/patients/add")
    @Operation(summary = "Gets the form to add a new patient",
            description = "Gets the form to add a new patient to the list of patients",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"))},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "HTML page displaying the empty form to fill the patient's information",
                            content = @Content(
                                    mediaType = "text/html"
                            ))
            })
    public String showAddPatient(Model model) {
        PatientBean patient = new PatientBean();
        model.addAttribute("patient", patient);
        return "add";
    }

    @PostMapping("/patients/validate")
    @Operation(summary = "Validates adding a new patient",
            description = "Validates the patient with all the information from the form",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "patient",
                            description = "The PatientBean dto containing the patient's information",
                            schema = @Schema(type = "PatientBean"))},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "HTML page displaying the list of patients",
                            content = @Content(
                                    mediaType = "text/html"
                            )),
                    @ApiResponse(
                            responseCode = "409",
                            description = "The error page with a message in case of conflict",
                            content = @Content(
                                    mediaType = "text/html"
                            ))
            })
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

    @Operation(summary = "Deletes a patient",
            description = "Delete a patient using his id",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "id",
                            description = "The id of the patient we are trying to delete",
                            schema = @Schema(type = "long"))},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "HTML page displaying the list of patients",
                            content = @Content(
                                    mediaType = "text/html"
                            )),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The error page with a message in case of patient not found",
                            content = @Content(
                                    mediaType = "text/html"
                            ))
            })
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
