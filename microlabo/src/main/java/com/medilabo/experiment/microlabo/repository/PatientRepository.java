package com.medilabo.experiment.microlabo.repository;

import com.medilabo.experiment.microlabo.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE patient AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}
