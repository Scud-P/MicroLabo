package com.medilabo.microrisk.service;

import com.medilabo.microrisk.domain.ExclusionWord;
import com.medilabo.microrisk.domain.RiskWord;
import com.medilabo.microrisk.repository.NoteRepository;
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


/**
 * Service class for calculating risk levels for patients based on their age, gender, and notes.
 * Provides methods for fetching patient data and analyzing note contents.
 */
@Service
public class RiskService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private NoteRepository noteRepository;

    //TODO DOUBLE CHECK THAT THIS WORKS LIVE


    /**
     * Fetches the birthdate of a patient by making a REST call the patient microservice (microlabo).
     *
     * @param id the ID of the patient
     * @return the birthdate of the patient
     */
    @GetMapping("/fetchBirthdate/{id}")
    public LocalDate fetchBirthDate(@PathVariable Long id) {
        return webClientBuilder.build()
                .get()
                .uri("http://gateway:8080/patients/{id}/birthdate", id)
                .retrieve()
                .bodyToMono(LocalDate.class)
                .block();
    }

    /**
     * Fetches the gender of a patient by making a REST call the patient microservice (microlabo).
     *
     * @param id the ID of the patient
     * @return the gender of the patient
     */
    @GetMapping("/fetchGender/{id}")
    public String fetchGender(@PathVariable Long id) {
        return webClientBuilder.build()
                .get()
                .uri("http://gateway:8080/patients/{id}/gender", id)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Calculates the age of a patient based on their birthdate.
     *
     * @param patientId the ID of the patient
     * @return the age of the patient, or 0 if birthdate is not available
     */
    public int calculateAge(Long patientId) {
        LocalDate birthDate = fetchBirthDate(patientId);
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Retrieves a list of risk words used to determine the patient's risk level.
     *
     * @return a list of risk words
     */
    public List<String> getRiskWords() {
        return Stream.of(RiskWord.values())
                .map(RiskWord::getRiskWord)
                .toList();
    }

    /**
     * Retrieves a list of exclusion words used to filter out certain combinations
     * from being counted as risk factors. Helps fine tune the algorithm.
     *
     * @return a list of exclusion words
     */
    public List<String> getExclusionWords() {
        return Stream.of(ExclusionWord.values())
                .map(ExclusionWord::getExclusionWord)
                .toList();
    }

    /**
     * Builds a search query string by joining the list of risk words with a space.
     * This query is used for performing text search in MongoDB.
     *
     * @param riskWords the list of risk words to be joined into a search query
     * @return a string representing the search query for MongoDB text search
     */
    private String buildSearchQuery(List<String> riskWords) {
        return String.join(" ", riskWords);
    }

    /**
     * Counts the occurrences of risk words in the contents of the notes of a specific patient, excluding
     * occurrences where exclusion words are present.
     *
     * @param patientId the ID of the patient whose notes will be analyzed
     * @return the count of risk words in the patient's notes after applying exclusion logic
     */
    public long getRiskWordsOccurrences(Long patientId) {
        List<String> riskWords = getRiskWords();
        String query = buildSearchQuery(riskWords);
        List<String> exclusionWords = getExclusionWords();
        List<String> contents = noteRepository.findNoteContentsByContentAndPatientId(query, patientId);

        Set<String> countedRiskWords = contents.stream()
                .flatMap(content -> riskWords.stream()
                        .filter(riskWord -> isToBeCountedRiskWord(content, riskWord, exclusionWords)))
                .collect(Collectors.toSet());
        return countedRiskWords.size();
    }

    /**
     * Checks if a note content contains any risk word and is not excluded by exclusion words.
     *
     * @param content        the content to search in
     * @param riskWord       riskWord to match
     * @param exclusionWords the list of exclusion words to consider
     * @return true if the content contains a risk word that is not excluded, false otherwise
     */
    private boolean isToBeCountedRiskWord(String content, String riskWord, List<String> exclusionWords) {
        String lowerContent = content.toLowerCase();
        String lowerRiskWord = riskWord.toLowerCase();

        return lowerContent.contains(lowerRiskWord) && !isToBeExcludedRiskWord(lowerContent, lowerRiskWord, exclusionWords);
    }

    /**
     * Checks if a risk word should be excluded based on the presence of exclusion words.
     *
     * @param content        the content to search in
     * @param riskWord       the risk word to look for
     * @param exclusionWords the list of exclusion words
     * @return true if the risk word should be excluded, false otherwise
     */
    private boolean isToBeExcludedRiskWord(String content, String riskWord, List<String> exclusionWords) {
        String lowerContent = content.toLowerCase();
        String lowerRiskWord = riskWord.toLowerCase();

        return exclusionWords.stream()
                .map(String::toLowerCase)
                .anyMatch(lowerExclusionWord ->
                        lowerContent.contains(lowerExclusionWord + " " + lowerRiskWord) ||
                                lowerContent.contains(lowerRiskWord + " " + lowerExclusionWord));
    }

    /**
     * Determines the risk level for a patient based on their age, gender, and the risk words found in their notes.
     *
     * @param patientId the ID of the patient
     * @return a string representing the risk level ("None", "Borderline", "In Danger", "Early onset")
     */
    public String calculateRiskForPatient(Long patientId) {
        int age = calculateAge(patientId);
        String gender = fetchGender(patientId);
        long riskWordOccurrences = getRiskWordsOccurrences(patientId);

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

                } else if (riskWordOccurrences < 3) {
                    return "None";
                } else return "Early onset";
            }
            if (riskWordOccurrences >= 6 && riskWordOccurrences < 8) {
                return "In Danger";
            } else return "Early onset";
        }
        if (gender.equalsIgnoreCase("f")) {
            if (age < 30) {
                if (riskWordOccurrences >= 4 && riskWordOccurrences < 7) {
                    return "In Danger";

                } else if (riskWordOccurrences < 4) {
                    return "None";
                } else return "Early onset";
            }
            if (riskWordOccurrences == 7) {
                return "In Danger";
            } else return "Early onset";
        }
        return "None";
    }
}
