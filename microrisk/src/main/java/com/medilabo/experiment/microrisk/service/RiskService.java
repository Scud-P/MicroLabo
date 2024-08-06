package com.medilabo.experiment.microrisk.service;

import com.medilabo.experiment.microrisk.RiskWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RiskService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("/fetchBirthdate/{id}")
    public LocalDate fetchBirthDate(@PathVariable Long id) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/patients/{id}/birthdate", id)
                .retrieve()
                .bodyToMono(LocalDate.class)
                .block();
    }

    @GetMapping("/fetchGender/{id}")
    public String fetchGender(@PathVariable Long id) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/patients/{id}/gender", id)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @GetMapping("/fetchContents/{patientId}")
    public List<String> fetchContents(@PathVariable Long patientId) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/patients/contents/{patientId}", patientId)
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .block();
    }

    public int calculateAge(Long patientId) {
        LocalDate birthDate = fetchBirthDate(patientId);
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public List<String> getRiskWords() {
        return Stream.of(RiskWord.values())
                .map(RiskWord::getRiskWord)
                .toList();
    }

    public Integer getRiskWordOccurrences(Long patientId) {
        List<String> contents = fetchContents(patientId);
        List<String> riskWords = getRiskWords();

        Set<String> countedRiskWords = contents.stream()
                .flatMap(content -> riskWords.stream()
                        .filter(riskWord -> content.toLowerCase().contains(riskWord.toLowerCase())))
                .collect(Collectors.toSet());
        return countedRiskWords.size();
    }

}
