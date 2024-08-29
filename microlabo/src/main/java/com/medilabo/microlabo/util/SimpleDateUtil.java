package com.medilabo.experiment.microlabo.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SimpleDateUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, formatter);
    }
}
