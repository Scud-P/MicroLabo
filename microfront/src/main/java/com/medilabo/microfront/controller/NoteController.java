package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.exception.NoteNotFoundException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import com.medilabo.microfront.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "Gets all notes by patient ID",
            description = "Retrieves all notes for a given patient by their ID.",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "id",
                            description = "The id of the patient for whom we want to retrieve the notes",
                            schema = @Schema(type = "long"))},
            responses = {
                    @ApiResponse(
                            description = "HTML page displaying the list of notes for the patient",
                            content = @Content(
                                    mediaType = "text/html"
                            )),
                    @ApiResponse(
                            description = "Patient not found",
                            content = @Content(
                                    mediaType = "text/html"
                            ))})
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
    @Operation(summary = "Finds a note by its ID to update its fields",
            description = "Retrieves a specific note by its ID to update it.",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "id",
                            description = "The id of the note we want to update",
                            schema = @Schema(type = "string"))},
            responses = {
                    @ApiResponse(
                            description = "HTML page for updating the note",
                            content = @Content(mediaType = "text/html")),
                    @ApiResponse(
                            description = "Note not found",
                            content = @Content(mediaType = "text/html"))})

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
    @Operation(summary = "Updates a note",
            description = "Updates the note with the id in parameter. Uses a NoteBean DTO in the request body",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "id",
                            description = "The id of the note to update",
                            schema = @Schema(type = "string"))},
            responses = {
                    @ApiResponse(
                            description = "HTML page for the list of notes of the patient",
                            content = @Content(
                                    mediaType = "text/html")),
                    @ApiResponse(
                            description = "Note not found",
                            responseCode = "404")
            })
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
    @Operation(summary = "Creates an empty note",
            description = "Creates an empty note for the targeted patient",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "id",
                            description = "The id of the patient for whom we want to add a note",
                            schema = @Schema(type = "long"))},
            responses = {
                    @ApiResponse(
                            description = "HTML page with the new empty note for the patient",
                            content = @Content(mediaType = "text/html")),
                    @ApiResponse(
                            description = "Note not found",
                            responseCode = "404")})

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
    @Operation(summary = "Validates a new note",
            description = "Validates the newly created note",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"))},
            responses = {
                    @ApiResponse(
                            description = "HTML page for the list of notes of the patient",
                            content = @Content(mediaType = "text/html"))})

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
    @Operation(summary = "Deletes a note",
            description = "Deletes a note, targeted by its id",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "The Bearer token for authentication. Syntax: Authorization:token",
                            required = true,
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string")),
                    @Parameter(
                            name = "id",
                            description = "The id of note we want to delete",
                            schema = @Schema(type = "string"))},
            responses = {
                    @ApiResponse(
                            description = "HTML page for the list of notes of the patient",
                            content = @Content(mediaType = "text/html"))
            })
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