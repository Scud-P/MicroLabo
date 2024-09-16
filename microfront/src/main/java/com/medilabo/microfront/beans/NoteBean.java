package com.medilabo.microfront.beans;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
