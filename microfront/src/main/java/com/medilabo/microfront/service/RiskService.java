package com.medilabo.microfront.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RiskService {

    @Autowired
    private WebClient.Builder webClientBuilder;


    public String fetchRiskById(long id, String token) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8080/risk/{patientId}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
