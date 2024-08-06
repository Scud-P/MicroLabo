package com.medilabo.experiment.micronotes.controller;

import com.medilabo.experiment.micronotes.domain.Note;
import com.medilabo.experiment.micronotes.exception.NoteNotFoundException;
import com.medilabo.experiment.micronotes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping("")
    public List<Note> getAllNotes() {
        return noteService.getAllNotes();
    }

    @GetMapping("/patient/{patientId}")
    public List<Note> getNotesByPatientId(@PathVariable("patientId") Long patientId) {
        return noteService.getNotesByPatientId(patientId);
    }

    @PostMapping("/validate")
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note, BindingResult result) {
        System.out.println("Received note data: " + note);

        if(result.hasErrors()){
            System.out.println("Validation errors: " + result.getAllErrors());
        }

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

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable ("id") String id) {
        Note note = noteService.getNoteById(id);
        if(note == null) throw new NoteNotFoundException("Note with id: " + id + " not found.");
        noteService.deleteNoteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable("id") String id, @RequestBody Note note) {
        if(!id.equals(note.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Note updatedNote = noteService.updateNote(note);
        return ResponseEntity.ok(updatedNote);
    }

    @GetMapping("/{id}")
    public Note getNoteById(@PathVariable("id") String id) {
        Note note = noteService.getNoteById(id);
        if(note == null) throw new NoteNotFoundException("Note with id: " + id + " not found.");
        return note;
    }

//    @GetMapping("/patient/risk/{patientId}")
//    public ResponseEntity<Long> getRiskByPatientId(@PathVariable long patientId) {
//        List<String> contents =  noteService.getContentsByPatientId(patientId);
//        long risk = noteService.countTotalRiskWordOccurrences(contents);
//        return ResponseEntity.ok(risk);
//    }
}
