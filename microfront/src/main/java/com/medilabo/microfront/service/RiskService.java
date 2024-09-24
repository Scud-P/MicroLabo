package com.medilabo.microfront.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service class responsible for interacting with the risk service to retrieve risk information
 * for patients using WebClient.
 */
@Service
public class RiskService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Fetches the risk assessment for a patient by their ID. Includes the cookie to the request to Authenticate on Microrisk Service.
     *
     * @param id    The ID of the patient whose risk assessment is to be fetched.
     * @param token Authorization token for the request.
     * @return String representing the risk assessment for the specified patient.
     */
    public String fetchRiskById(long id, String token) {
        return webClientBuilder.build()
                .get()
                .uri("/risk/{patientId}", id)
                .cookie("token", token)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
