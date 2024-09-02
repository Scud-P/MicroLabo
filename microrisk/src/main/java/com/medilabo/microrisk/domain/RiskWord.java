package com.medilabo.microrisk.domain;

import lombok.Getter;

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

        private final String riskWord;

        RiskWord(String riskWord) {
            this.riskWord = riskWord;
        }
}
