package com.medilabo.micronotes.service;

import com.medilabo.micronotes.domain.Note;
import com.medilabo.micronotes.exception.NoteNotFoundException;
import com.medilabo.micronotes.exception.PatientNotFoundException;
import com.medilabo.micronotes.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


/**
 * Service class for managing patient notes.
 * Provides methods to retrieve, create, update, delete, and perform operations related to patient notes
 * from the MongoDB database through the NoteRepository.
 */
@Service
public class NoteService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Retrieves a note by its ID.
     *
     * @param id the ID of the note
     * @return the retrieved Note
     * @throws NoteNotFoundException if no note is found with the given ID
     */
    public Note getNoteById(String id) {
        return noteRepository.findById(id).orElseThrow(
                () -> new NoteNotFoundException("No note found for id: " + id)
        );
    }

    /**
     * Retrieves all notes associated with a specific patient ID.
     * Checks if the patient exists by making a REST call to the patient microservice first.
     *
     * @param patientId the ID of the patient
     * @return a list of Note associated with the patient
     * @throws PatientNotFoundException if the patient is not found
     */
    public List<Note> getNotesByPatientId(Long patientId) {
        Boolean existsPatient = webClientBuilder
                .baseUrl("http://gateway:8080")
                .build()
                .get()
                .uri("/patients/{id}/exists", patientId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (Boolean.FALSE.equals(existsPatient)) {
            throw new PatientNotFoundException("Patient not found for id: " + patientId);
        }
        return noteRepository.findByPatientId(patientId);
    }

    /**
     * Saves a new note.
     *
     * @param note the note to be saved
     * @return the saved Note
     */
    @Transactional
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    /**
     * Deletes a new note.
     *
     * @param id the id of the note to be deleted
     */
    @Transactional
    public void deleteNoteById(String id) {
        noteRepository.deleteById(id);
    }

    /**
     * Updates an existing note.
     *
     * @param note the note containing updated information
     * @return the updated Note
     */
    @Transactional
    public Note updateNote(Note note) {
        Note noteToUpdate = getNoteById(note.getId());
        noteToUpdate.setContent(note.getContent());
        return noteRepository.save(noteToUpdate);
    }

    /**
     * Retrieves all notes from the repository.
     *
     * @return a list of all Note
     */
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }
}
