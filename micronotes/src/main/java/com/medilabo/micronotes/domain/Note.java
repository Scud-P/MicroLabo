package com.medilabo.micronotes.domain;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a note entity in the system.
 * This class maps to the MongoDB {@code notes} collection and contains information
 * about a note, such as the patient's ID, the patient's last name, and the note content.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "notes")
public class Note {

    @Id
    private String id;
    private Long patientId;
    private String patientLastName;
    private String content;

}
