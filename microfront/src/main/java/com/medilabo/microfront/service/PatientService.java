package com.medilabo.microfront.service;

import com.medilabo.microfront.beans.PatientBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

public class PatientService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    public List<PatientBean> fetchPatients(String token) {

        WebClient webClient = webClientBuilder.build();
        return webClient.get()
                .uri("/patients/list")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToFlux(PatientBean.class)
                .collectList()
                .block();
    }

}
