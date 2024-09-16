package com.medilabo.microfront.service;

import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.PatientAlreadyExistsException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service class for managing patients and interacting with external services using WebClient.
 * Provides methods to fetch, update, validate, and delete patients, handling common exceptions
 * such as patient not found and patient already exists.
 */
@Service
public class PatientService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Fetches a list of all patients.
     *
     * @param token Authorization token for the request.
     * @return List of {@link PatientBean} objects representing the patients.
     */
    public List<PatientBean> fetchPatients(String token) {
        return webClientBuilder.build()
                .get()
                .uri("/patients/list")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToFlux(PatientBean.class)
                .collectList()
                .block();
    }

    /**
     * Fetches a patient by their ID.
     *
     * @param id The ID of the patient to be fetched.
     * @param token Authorization token for the request.
     * @return The {@link PatientBean} object for the specified patient.
     * @throws PatientNotFoundException if the patient is not found.
     */
    public PatientBean fetchPatientById(long id, String token) {
        return webClientBuilder.build()
                .get()
                .uri("/patients/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new PatientNotFoundException("Patient not found for id: " + id)))
                .bodyToMono(PatientBean.class)
                .block();
    }

    /**
     * Updates the details of an existing patient by their ID.
     *
     * @param id The ID of the patient to be updated.
     * @param patient The {@link PatientBean} object containing the updated details.
     * @param token Authorization token for the request.
     * @return The updated {@link PatientBean} object.
     * @throws PatientNotFoundException if the patient is not found.
     * @throws PatientAlreadyExistsException if a patient with the same name and birthdate already exists.
     */
    public PatientBean updatePatient(long id, PatientBean patient, String token) {
        return webClientBuilder.build()
                .put()
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
    }

    /**
     * Validates and adds a new patient if they don't already exist.
     *
     * @param patient The {@link PatientBean} object to be validated.
     * @param token Authorization token for the request.
     * @return The validated {@link PatientBean} object.
     * @throws PatientAlreadyExistsException if a patient with the same name and birthdate already exists.
     */
    public PatientBean validatePatient(PatientBean patient, String token) {
        return webClientBuilder.build()
                .post()
                .uri("/patients/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(patient)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new PatientAlreadyExistsException(
                                "Patient can't be added because a patient with the same first name, last name and birthdate combination already exists")))
                .bodyToMono(PatientBean.class)
                .block();
    }

    /**
     * Deletes a patient by their ID.
     *
     * @param id The ID of the patient to be deleted.
     * @param token Authorization token for the request.
     * @throws PatientNotFoundException if the patient is not found.
     */
    public void deletePatientById(Long id, String token) {
        webClientBuilder.build()
                .delete()
                .uri("/patients/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new PatientNotFoundException(
                                "Patient not found for id: " + id)))
                .toBodilessEntity()
                .block();
    }
}
