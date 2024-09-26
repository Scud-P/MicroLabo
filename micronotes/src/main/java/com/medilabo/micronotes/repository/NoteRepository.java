package com.medilabo.micronotes.repository;

import com.medilabo.micronotes.domain.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on {@link Note} entities.
 * This interface extends {@link MongoRepository}, providing various methods to
 * interact with the MongoDB database for the {@code notes} collection.
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    /**
     * Finds all notes associated with the specified patient ID.
     *
     * @param patientId the ID of the patient whose notes are to be retrieved.
     * @return a list of {@link Note} entities associated with the given patient ID.
     */
    List<Note> findByPatientId(Long patientId);
}
