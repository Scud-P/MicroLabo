package com.medilabo.microrisk.repository;

import com.medilabo.microrisk.domain.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for accessing {@link Note} documents in the MongoDB database.
 * This interface extends {@link MongoRepository} to provide CRUD operations
 * and custom query methods for {@link Note} entities.
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    /**
     * Finds the contents of notes that match the specified search query
     * for a given patient ID.
     *
     * @param searchQuery the search query to match against note contents.
     * @param patientId   the ID of the patient whose notes are to be searched.
     * @return a list of note contents that match the search criteria.
     */
    @Query(value = "{ $text: { $search: ?0 }, patientId: ?1 }", fields = "{ 'content' : 1 }")
    List<String> findNoteContentsByContentAndPatientId(String searchQuery, Long patientId);
}

