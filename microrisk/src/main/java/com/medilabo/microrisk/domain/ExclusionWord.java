package com.medilabo.microrisk.domain;

import lombok.Getter;

/**
 * Enum representing various exclusion words that can be used for filtering
 * of {@link RiskWord}
 * Each exclusion word is associated with a specific string representation.
 */
@Getter
public enum ExclusionWord {

    EGAL("Égal"),
    RECOMMANDE("Recommandé"),
    ;

    /** The string representation of the exclusion word. */
    private final String exclusionWord;

    /**
     * Constructs an {@code ExclusionWord} with the specified string representation.
     *
     * @param exclusionWord the string representation of the exclusion word.
     */
    ExclusionWord(String exclusionWord) {
        this.exclusionWord = exclusionWord;
    }
}
