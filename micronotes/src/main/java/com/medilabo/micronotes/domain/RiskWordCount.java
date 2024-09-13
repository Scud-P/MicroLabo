package com.medilabo.micronotes.domain;

import lombok.Data;

@Data
public class RiskWordCount {
    private String riskWord;
    private int count;
}
