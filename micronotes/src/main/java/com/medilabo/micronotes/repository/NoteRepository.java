package com.medilabo.micronotes.repository;

import com.medilabo.micronotes.domain.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    List<Note> findByPatientId(Long patientId);

    @Query(value = "{ 'patientId': ?0 }", fields = "{ 'content': 1, '_id': 0 }")
    List<String> findContentsByPatientId(Long patientId);

}
