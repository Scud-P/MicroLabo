package com.medilabo.microrisk.domain;

import lombok.Getter;

@Getter
public enum ExclusionWord {

    EGAL("Égal"),
    RECOMMANDE("Recommandé"),
    ;

    private final String exclusionWord;

    ExclusionWord(String exclusionWord) {
        this.exclusionWord = exclusionWord;
    }
}
