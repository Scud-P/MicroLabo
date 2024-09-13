package com.medilabo.microrisk.repository;

import com.medilabo.microrisk.domain.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    @Query(value = "{ $text: { $search: ?0 }, patientId: ?1 }", fields = "{ 'content' : 1 }")
    List<String> findNoteContentsByContentAndPatientId(String searchQuery, Long patientId);
}

