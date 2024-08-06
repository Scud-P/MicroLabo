package com.medilabo.experiment.microlabo.repository;

import com.medilabo.experiment.microlabo.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE patient AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();

    @Query("SELECT p.birthdate FROM Patient p WHERE p.id = :id")
    LocalDate findBirthdateById(@Param("id") Long id);

    @Query("SELECT p.gender FROM Patient p WHERE p.id = :id")
    String findGenderById(Long id);
}
