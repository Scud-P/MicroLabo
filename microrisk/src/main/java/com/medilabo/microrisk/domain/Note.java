package com.medilabo.microrisk.domain;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a note associated with a patient.
 * This class is annotated as a MongoDB document and contains information related to the
 * patient's note, including the patient's ID, last name, and the content of the note.
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
