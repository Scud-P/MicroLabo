package com.medilabo.micronotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabo.micronotes.controller.NoteController;
import com.medilabo.micronotes.domain.Note;
import com.medilabo.micronotes.exception.NoteNotFoundException;
import com.medilabo.micronotes.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NoteControllerTest {

    @Autowired
    private NoteController noteController;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    private static Note firstNote;
    private static Note secondNote;
    private static List<Note> notes;

    @BeforeEach
    public void setUp() {
        firstNote = new Note("firstNoteId", 1L, "Kenobi", "Patient is leaning towards the dark side of the force");
        secondNote = new Note("secondNoteId", 1L, "Kenobi", "Patient admits being traumatized by killing his Padawan");
        notes = List.of(firstNote, secondNote);
    }

    @Test
    public void getAllNotes_shouldReturnAllNotes() throws Exception {

        when(noteService.getAllNotes()).thenReturn(notes);

        mockMvc.perform(get("/notes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(firstNote.getId())))
                .andExpect(jsonPath("$[0].patientId", is(firstNote.getPatientId().intValue())))
                .andExpect(jsonPath("$[0].content", is(firstNote.getContent())))
                .andExpect(jsonPath("$[1].id", is(secondNote.getId())))
                .andExpect(jsonPath("$[1].patientId", is(secondNote.getPatientId().intValue())))
                .andExpect(jsonPath("$[1].content", is(secondNote.getContent())));
    }

    @Test
    public void getNotesByPatientId_shouldReturnAllNotesWithTheCorrectPatientId() throws Exception {

        when(noteService.getNotesByPatientId(anyLong())).thenReturn(notes);

        mockMvc.perform(get("/notes/patient/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(firstNote.getId())))
                .andExpect(jsonPath("$[0].patientId", is(firstNote.getPatientId().intValue())))
                .andExpect(jsonPath("$[0].content", is(firstNote.getContent())))
                .andExpect(jsonPath("$[1].id", is(secondNote.getId())))
                .andExpect(jsonPath("$[1].patientId", is(secondNote.getPatientId().intValue())))
                .andExpect(jsonPath("$[1].content", is(secondNote.getContent())));
    }

    @Test
    public void postRequest_toValidateNote_shouldReturnCreatedCode_whenNoteIsCreated() throws Exception {

        when(noteService.saveNote(any(Note.class))).thenReturn(firstNote);

        mockMvc.perform(post("/notes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(firstNote)))
                .andExpect(status().isCreated());
    }

    @Test
    public void postRequest_toValidateNote_shouldReturnNoContentCode_whenNoteObjectIsNull() throws Exception {

        Note note = new Note();

        when(noteService.saveNote(any(Note.class))).thenReturn(null);

        mockMvc.perform(post("/notes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(note)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void getNoteById_shouldThrowNoteNotFoundException_whenIdDoesNotExist() {
        assertThrows(NoteNotFoundException.class, () -> noteController.getNoteById("0"));
    }

    @Test
    public void deleteRequest_toDeleteNoteById_shouldDeleteTheCorrectNote() throws Exception {

        when(noteService.getNoteById(anyString())).thenReturn(firstNote);

        mockMvc.perform(delete("/notes/firstNoteId"))
                .andExpect(status().isNoContent());

        verify(noteService, times(1)).deleteNoteById("firstNoteId");
    }

    @Test
    public void deleteRequest_toDeleteNoteById_shouldThrowNoteNotFoundException_whenNoteIsNotFound() {
        assertThrows(NoteNotFoundException.class, () -> noteController.deleteNoteById("someIdThatDoesNotExist"));
        verify(noteService, never()).deleteNoteById(anyString());
    }

    @Test
    public void getRequest_toUpdateNoteById_ShouldReturnTheCorrectNote_whenItIsFound() throws Exception {

        when(noteService.getNoteById(anyString())).thenReturn(firstNote);

        mockMvc.perform(get("/notes/firstNoteId"))
                .andExpect(status().isOk());

        verify(noteService, times(1)).getNoteById(firstNote.getId());
    }

    @Test
    public void getRequest_toUpdateNoteById_ShouldThrowNoteNotFoundException_whenNoteDoesNotExist() {
        assertThrows(NoteNotFoundException.class, () -> noteController.getNoteById("someIdThatDoesNotExist"));
    }

    @Test
    public void putRequest_toUpdateNote_shouldReturnOkCode_whenUpdatedSuccessfully() throws Exception {

        when(noteService.updateNote(any(Note.class))).thenReturn(firstNote);

        mockMvc.perform(put("/notes/firstNoteId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(firstNote)))
                .andExpect(status().isOk());

        verify(noteService, times(1)).updateNote(firstNote);
    }

    @Test
    public void putRequest_toUpdateNote_shouldReturnBadRequest_whenIdsDoNotMatch() throws Exception {
        when(noteService.updateNote(any(Note.class))).thenReturn(firstNote);

        mockMvc.perform(put("/notes/anotherId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(firstNote)))
                .andExpect(status().isBadRequest());
    }

    // Utility method for MockMvc testing

    public static String asJsonString(final Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

