package com.medilabo.microfront.beans;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Bean class representing a note in the system.
 * This class is used to transfer note data between different
 * microservices of the application.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteBean {
    @Id
    private String id;
    private Long patientId;
    private String patientLastName;
    private String content;
}
