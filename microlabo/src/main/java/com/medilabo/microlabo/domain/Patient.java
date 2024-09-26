package com.medilabo.microlabo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Entity representing a Patient in the system.
 * This class maps to the "patient" table in the database and contains various details about the patient,
 * including their first name, last name, birthdate, gender, address, and phone number.
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    @NotBlank(message="First Name is mandatory")
    private String firstName;

    @Column(name = "last_name")
    @NotBlank(message="Last Name is mandatory")
    private String lastName;

    @Column(name = "birthdate")
    @NotNull(message="Birthdate is mandatory")
    private LocalDate birthdate;

    @Column(name = "gender")
    @NotNull(message="Gender is mandatory")
    @Size(min = 1, max = 1)
    private String gender;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;
}
