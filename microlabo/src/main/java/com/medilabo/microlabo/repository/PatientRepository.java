package com.medilabo.microlabo.repository;

import com.medilabo.microlabo.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Repository interface for managing {@link Patient} entities.
 * This interface extends {@link JpaRepository} and provides additional
 * custom query methods for interacting with the {@code patient} table in the database.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Resets the auto-increment value for the {@code patient} table.
     * This method modifies the table schema to set the auto-increment value back to 1.
     * It is transactional and requires the method to complete in one atomic operation.
     */
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE patient AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();

    /**
     * Retrieves the birthdate of a patient by their ID.
     *
     * @param id the ID of the patient
     * @return the birthdate of the patient, or {@code null} if no patient is found
     */
    @Query("SELECT p.birthdate FROM Patient p WHERE p.id = :id")
    LocalDate findBirthdateById(@Param("id") Long id);

    /**
     * Retrieves the gender of a patient by their ID.
     *
     * @param id the ID of the patient
     * @return the gender of the patient, or {@code null} if no patient is found
     */
    @Query("SELECT p.gender FROM Patient p WHERE p.id = :id")
    String findGenderById(Long id);

    /**
     * Checks if a patient exists with the same first name, last name, and birthdate.
     *
     * @param patient the patient object containing the first name, last name, and birthdate to check
     * @return {@code true} if a patient with the same first name, last name, and birthdate exists, {@code false} otherwise
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Patient p WHERE p.firstName = :#{#patient.firstName} AND p.lastName = :#{#patient.lastName} AND p.birthdate = :#{#patient.birthdate}")
    boolean existsPatientByFirstNameAndLastNameAndBirthdate(Patient patient);

    /**
     * Checks if a patient exists with the same first name, last name, birthdate, and an ID that is not the given ID.
     *
     * @param firstName the first name of the patient
     * @param lastName the last name of the patient
     * @param birthdate the birthdate of the patient
     * @param id the ID of the patient that should be excluded from the search
     * @return {@code true} if a patient with the same details exists, excluding the given ID, {@code false} otherwise
     */
    boolean existsByFirstNameAndLastNameAndBirthdateAndIdNot(String firstName, String lastName, LocalDate birthdate, Long id);
}
