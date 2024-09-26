package com.medilabo.micronotes.domain;

import lombok.Getter;

/**
 * Enum representing various risk words associated with patient notes.
 * Each constant in this enum corresponds to a specific medical term
 * that can be used to identify potential risks in patient health assessments.
 */
@Getter
public enum RiskWord {

    HEMOGLOBINE_A1C("Hémoglobine A1C"),
    MICROALBUMINE("Microalbumine"),
    TAILLE("Taille"),
    POIDS("Poids"),
    FUMEUR("Fumeur"),
    FUMEUSE("Fumeuse"),
    ANORMAL("Anormal"),
    CHOLESTEROL("Cholestérol"),
    VERTIGES("Vertiges"),
    RECHUTE("Rechute"),
    REACTION("Réaction"),
    ANTICORPS("Anticorps"),
    ;

    /**
     * The string representation of the risk word.
     */
    private final String riskWord;

    /**
     * Constructor for RiskWord enum.
     *
     * @param riskWord the string representation of the risk word.
     */
    RiskWord(String riskWord) {
        this.riskWord = riskWord;
    }
}
