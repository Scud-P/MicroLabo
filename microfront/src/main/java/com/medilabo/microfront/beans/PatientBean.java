package com.medilabo.microfront.beans;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientBean {
    private long id;
    private String firstName;
    private String lastName;
    @Getter
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
    private String gender;
    private String address;
    private String phoneNumber;
}
