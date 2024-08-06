package com.medilabo.experiment.micronotes.domain;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
