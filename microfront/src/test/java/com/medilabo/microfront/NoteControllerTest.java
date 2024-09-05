package com.medilabo.microfront;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.controller.NoteController;
import com.medilabo.microfront.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteController noteController;

    @MockBean
    private NoteService noteService;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @Mock
    private Model model;

    private static NoteBean firstNote;
    private static NoteBean secondNote;
    private static List<NoteBean> notes;
    private static PatientBean firstPatient;

    @BeforeEach
    public void setUp() {
        firstNote = new NoteBean("firstNoteId", 1L, "Kenobi", "Patient is leaning towards the dark side of the force");
        secondNote = new NoteBean("secondNoteId", 1L, "Kenobi", "Patient admits being traumatized by killing his Padawan");
        notes = List.of(firstNote, secondNote);

        LocalDate obiWanBirthdate = LocalDate.of(2017, 5, 8);

        firstPatient = new PatientBean
                (1L, "Obiwan", "Kenobi", obiWanBirthdate, "M", "666 Devil Drive", "111-111-111");
    }

    @Test
    public void testGetNotes() {
        when(noteService.fetchNotesByPatientId(anyString(), anyLong())).thenReturn(notes);

        String result = noteController.getNotes("someValidToken", firstPatient.getId(), model);

        assertEquals("notes/list", result);
        verify(model, times(1)).addAttribute("notes", notes);
        verify(model, times(1)).addAttribute("patientId", firstPatient.getId());
    }

    @Test
    public void testUpdateModelWithPatientNotes() {
        when(noteService.fetchNotesByPatientId(anyString(), anyLong())).thenReturn(notes);

        String result = noteController.updateModelWithPatientNotes("someValidToken", firstPatient.getId(), model);

        assertEquals("notes/list", result);
        verify(model, times(1)).addAttribute("notes", notes);
        verify(model, times(1)).addAttribute("patientId", firstPatient.getId());
    }

    @Test
    public void testShowUpdateNote() {
        when(noteService.fetchUpdateNote(anyString(), anyString())).thenReturn(firstNote);

        String result = noteController.showUpdateNote(firstNote.getId(), "someValidToken", model);

        assertEquals("notes/update", result);
        verify(model, times(1)).addAttribute("note", firstNote);
    }

    @Test
    public void testUpdateNote() {
        when(noteService.updateNote(anyString(), any(NoteBean.class), anyString())).thenReturn(firstNote);
        when(noteService.fetchNotesByPatientId(anyString(), anyLong())).thenReturn(notes);

        String result = noteController.updateNote(firstNote.getId(), firstNote, "someValidToken", model);

        assertEquals("notes/list", result);
        verify(model, times(1)).addAttribute("notes", notes);
        verify(model, times(1)).addAttribute("patientId", firstPatient.getId());
    }

    @Test
    public void testShowAddNote() {
        when(noteService.showAddNote(anyLong(), anyString())).thenReturn(firstNote);
        String result = noteController.showAddNote(firstPatient.getId(), "someValidToken", model);
        assertEquals("notes/add", result);
        verify(model, times(1)).addAttribute("note", firstNote);
    }

    @Test
    public void testValidateNote() {
        when(noteService.validateNote(any(NoteBean.class), anyString())).thenReturn(firstNote);
        when(noteService.fetchNotesByPatientId(anyString(), anyLong())).thenReturn(notes);

        String result = noteController.validateNote( "someValidToken", firstNote, model);

        assertEquals("notes/list", result);
        verify(model, times(1)).addAttribute("notes", notes);
        verify(model, times(1)).addAttribute("patientId", firstPatient.getId());
    }

    @Test
    public void testDeleteNote() {
        when(noteService.fetchPatientIdForNoteId(anyString(), anyString())).thenReturn(firstNote.getPatientId());
        doNothing().when(noteService).deleteNote(anyString(), anyString());
        when(noteService.fetchNotesByPatientId(anyString(), anyLong())).thenReturn(notes);

        String result = noteController.deleteNote("someValidToken", firstNote.getId(), model);
        assertEquals("notes/list", result);
        verify(model, times(1)).addAttribute("notes", notes);
        verify(model, times(1)).addAttribute("patientId", firstPatient.getId());
    }
}
