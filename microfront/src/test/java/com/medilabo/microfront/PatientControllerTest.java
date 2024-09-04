package com.medilabo.microfront;

import com.medilabo.microfront.beans.PatientBean;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.time.LocalDate;

public class PatientControllerTest {

    private static PatientBean firstPatient;
    private static PatientBean secondPatient;

    @BeforeEach
    public void setUp() throws IOException {
        LocalDate rafaelBirthdate = LocalDate.of(2017, 5, 8);
        LocalDate thaliaBirthDate = LocalDate.of(2012, 9, 7);

        firstPatient = new PatientBean
                (1L, "Rafael", "Doe", rafaelBirthdate, "M", "666 Devil Drive", "111-111-111");
        secondPatient = new PatientBean
                (2L, "Thalia", "Smith", thaliaBirthDate, "F", "777 Jackpot Road", "222-222-222");
    }
}

