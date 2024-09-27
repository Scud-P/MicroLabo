package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.exception.NoteNotFoundException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import com.medilabo.microfront.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Controller for handling operations related to notes for patients.
 * Provides endpoints to retrieve, create, update, and delete notes.
 */
@Controller
@RequestMapping("/api")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Retrieves all notes for a given patient by their ID.
     *
     * @param token     The Bearer token for authentication.
     * @param patientId The ID of the patient for whom notes are to be retrieved.
     * @param model     The model to be populated with note data.
     * @return The name of the view displaying the list of notes.
     */
    @GetMapping("/notes/patient/{patientId}")
    public String getNotes(@CookieValue(name = "token", required = false) String token,
                           @PathVariable("patientId") Long patientId,
                           Model model) {
        try {
            return updateModelWithPatientNotes(token, patientId, model);

        } catch (PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    /**
     * Updates the model with the notes of the specified patient.
     *
     * @param token     The Bearer token for authentication.
     * @param patientId The ID of the patient for whom notes are to be retrieved.
     * @param model     The model to be populated with note data.
     * @return The name of the view displaying the list of notes.
     */
    public String updateModelWithPatientNotes(String token, long patientId, Model model) {
        List<NoteBean> notes = noteService.fetchNotesByPatientId(token, patientId);
        model.addAttribute("notes", notes);
        model.addAttribute("patientId", patientId);
        return "notes/list";
    }


    /**
     * Retrieves a specific note by its ID to update it.
     *
     * @param id    The ID of the note to be updated.
     * @param token The Bearer token for authentication.
     * @param model The model to be populated with note data.
     * @return The name of the view for updating the note.
     */
    @GetMapping("/notes/update/{id}")
    public String showUpdateNote(@PathVariable("id") String id,
                                 @CookieValue(name = "token", required = false) String token,
                                 Model model) {
        try {
            NoteBean note = noteService.fetchUpdateNote(id, token);
            model.addAttribute("note", note);
            return "notes/update";

        } catch (NoteNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    /**
     * Updates a note with the provided details.
     *
     * @param id    The ID of the note to be updated.
     * @param note  The note data to be updated.
     * @param token The Bearer token for authentication.
     * @param model The model to be populated with note data.
     * @return The name of the view displaying the list of notes.
     */
    @PutMapping("/notes/{id}")
    public String updateNote(@PathVariable("id") String id,
                             @ModelAttribute NoteBean note,
                             @CookieValue(name = "token", required = false) String token,
                             Model model) {
        try {
            noteService.updateNote(id, note, token);
            Long patientId = note.getPatientId();
            return updateModelWithPatientNotes(token, patientId, model);

        } catch (NoteNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    /**
     * Displays a form to create a new empty note for the specified patient.
     *
     * @param patientId The ID of the patient for whom the note is to be created.
     * @param token     The Bearer token for authentication.
     * @param model     The model to be populated with note data.
     * @return The name of the view for adding a new note.
     */
    @GetMapping("/notes/add/{patientId}")
    public String showAddNote(@PathVariable("patientId") Long patientId,
                              @CookieValue(name = "token", required = false) String token,
                              Model model) {
        NoteBean note = noteService.showAddNote(patientId, token);
        model.addAttribute("note", note);
        return "notes/add";
    }

    /**
     * Validates a newly created note and returns the list of notes for the patient.
     *
     * @param token The Bearer token for authentication.
     * @param note  The note data to be validated.
     * @param model The model to be populated with note data.
     * @return The name of the view displaying the list of notes.
     */
    @PostMapping("/notes/validate")
    public String validateNote(@CookieValue(name = "token", required = false) String token,
                               @ModelAttribute NoteBean note,
                               Model model) {
        noteService.validateNote(note, token);
        Long patientId = note.getPatientId();
        return updateModelWithPatientNotes(token, patientId, model);
    }

    /**
     * Deletes a specific note by its ID.
     *
     * @param id    The ID of the note to be deleted.
     * @param token The Bearer token for authentication.
     * @param model The model to be populated with note data.
     * @return The name of the view displaying the list of notes.
     */
    @DeleteMapping("/notes/{id}")
    public String deleteNote(@CookieValue(name = "token", required = false) String token,
                             @PathVariable("id") String id,
                             Model model) {
        try {
            Long patientId = noteService.fetchPatientIdForNoteId(token, id);
            noteService.deleteNote(id, token);
            return updateModelWithPatientNotes(token, patientId, model);

        } catch (NoteNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}