package com.medilabo.micronotes.controller;

import com.medilabo.micronotes.exception.NoteNotFoundException;
import com.medilabo.micronotes.service.NoteService;
import com.medilabo.micronotes.domain.Note;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * REST controller for managing notes.
 * Provides endpoints for retrieving, creating, updating, and deleting notes.
 */
@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    /**
     * Retrieves all notes.
     *
     * @return a list of all {@link Note}
     */
    @GetMapping("")
    public List<Note> getAllNotes() {
        return noteService.getAllNotes();
    }

    /**
     * Retrieves all notes associated with a specific patient ID.
     *
     * @param patientId the ID of the patient
     * @return a list of Note associated with the patient
     */
    @GetMapping("/patient/{patientId}")
    public List<Note> getNotesByPatientId(@PathVariable("patientId") Long patientId) {
        return noteService.getNotesByPatientId(patientId);
    }

    /**
     * Validates the creation of a new note
     *
     * @param note the Note to create
     * @return a ResponseEntity with the HTTP status and location of the created note
     */
    @PostMapping("/validate")
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note) {
        Note addedNote = noteService.saveNote(note);
        if(Objects.isNull(addedNote)) {
            return ResponseEntity.noContent().build();
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(addedNote.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    /**
     * Deletes a note by its ID.
     *
     * @param id the ID of the note to be deleted
     * @return a ResponseEntity with no content if deletion was successful
     * @throws NoteNotFoundException if the note with the given ID is not found
     */
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable ("id") String id) {
        Note note = noteService.getNoteById(id);
        if(note == null) throw new NoteNotFoundException("Note with id: " + id + " not found.");
        noteService.deleteNoteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates an existing note.
     *
     * @param id the ID of the note to update
     * @param note the Note with updated information
     * @return a ResponseEntity with the updated note
     */
    @PutMapping(value = "/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable("id") String id, @RequestBody Note note) {
        if(!id.equals(note.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Note updatedNote = noteService.updateNote(note);
        return ResponseEntity.ok(updatedNote);
    }

    /**
     * Retrieves a note by its ID.
     *
     * @param id the ID of the note to retrieve
     * @return the retrieved Note
     * @throws NoteNotFoundException if the note with the given ID is not found
     */
    @GetMapping("/{id}")
    public Note getNoteById(@PathVariable("id") String id) {
        Note note = noteService.getNoteById(id);
        if(note == null) throw new NoteNotFoundException("Note with id: " + id + " not found.");
        return note;
    }

    /**
     * Retrieves the contents of notes associated with a specific patient ID.
     *
     * @param patientId the ID of the patient
     * @return a ResponseEntity containing a list of note contents
     */
    @GetMapping("/patient/contents/{patientId}")
    public ResponseEntity<List<String>> getContentsForPatient(@PathVariable("patientId") long patientId) {
        List<String> contents =  noteService.getContentsByPatientId(patientId);
        return ResponseEntity.ok(contents);
    }

}
