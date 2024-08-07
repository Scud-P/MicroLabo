package com.medilabo.experiment.microrisk.service;

import com.medilabo.experiment.microrisk.domain.RiskWord;
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
                .uri("http://localhost:8083/notes/patient/contents/{patientId}", patientId)
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

    public int getRiskWordOccurrences(Long patientId) {
        List<String> contents = fetchContents(patientId);
        List<String> riskWords = getRiskWords();

        Set<String> countedRiskWords = contents.stream()
                .flatMap(content -> riskWords.stream()
                        .filter(riskWord -> content.toLowerCase().contains(riskWord.toLowerCase())))
                .collect(Collectors.toSet());
        return countedRiskWords.size();
    }

    public String calculateRiskForPatient(Long patientId) {
        int age = calculateAge(patientId);
        String gender = fetchGender(patientId);
        int riskWordOccurrences = getRiskWordOccurrences(patientId);

        if (riskWordOccurrences == 0) {
            return "None";
        } else if (riskWordOccurrences >= 2 && riskWordOccurrences <= 5) {
            if (age > 30) {
                return "Borderline";
            }
        }
        if (gender.equalsIgnoreCase("m")) {
            if (age < 30) {
                if (riskWordOccurrences >= 3 && riskWordOccurrences < 5) {
                    return "In Danger";

                } else if(riskWordOccurrences < 3) {
                    return "None";
                }
                else return "Early onset";
            }
            if (riskWordOccurrences >= 6 && riskWordOccurrences < 8) {
                return "In Danger";
            } else return "Early onset";
        }
        if (gender.equalsIgnoreCase("f")) {
            if (age < 30) {
                if (riskWordOccurrences >= 4 && riskWordOccurrences < 7) {
                    return "In Danger";

                } else if(riskWordOccurrences < 4) {
                    return "None";
                }
                else return "Early onset";
            }
            if (riskWordOccurrences == 7) {
                return "In Danger";
            } else return "Early onset";
        }
        return "None";
    }
}
