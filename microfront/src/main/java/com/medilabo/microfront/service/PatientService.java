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

@Service
public class PatientService {

    @Autowired
    private WebClient.Builder webClientBuilder;

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
