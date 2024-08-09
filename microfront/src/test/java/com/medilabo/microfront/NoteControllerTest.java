package com.medilabo.microfront;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.controller.NoteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NoteControllerTest {

    @InjectMocks
    private NoteController noteController;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient webClient;

    @Mock(answer = Answers.RETURNS_SELF)
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
    public void testShowUpdateNote() {
        String id = firstNote.getId();

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()
                .uri("http://localhost:8083/notes/{id}", id)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block())
                .thenReturn(firstNote);

        String result = noteController.showUpdateNote(id, model);

        assertEquals("notes/update", result);
        verify(model, times(1)).addAttribute("note", firstNote);
    }

    @Test
    public void testUpdateNote() {
        String id = firstNote.getId();

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.put()
                .uri("http://localhost:8083/notes/{id}", id)
                .bodyValue(anyString())
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block())
                .thenReturn(firstNote);

        String result = noteController.updateNote(id, firstNote, model);

        assertEquals("notes/list", result);
    }

    @Test
    public void testShowAddNote() {
        Long patientId = firstNote.getPatientId();
        String patientLastName = firstNote.getPatientLastName();
        NoteBean expectedNote = new NoteBean();
        expectedNote.setPatientId(patientId);
        expectedNote.setPatientLastName(patientLastName);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()
                .uri("http://localhost:8081/patients/{id}", patientId)
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block())
                .thenReturn(firstPatient);

        String result = noteController.showAddNote(patientId, model);
        assertEquals("notes/add", result);
        verify(model, times(1)).addAttribute("note", expectedNote);
    }

    @Test
    public void testValidateNote() {
        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.post()
                .uri("http://localhost:8083/notes/validate")
                .bodyValue(firstNote)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block())
                .thenReturn(firstNote);

        String result = noteController.validateNote(firstNote, model);

        assertEquals("notes/list", result);

        verify(model).addAttribute("patientId", firstPatient.getId());
        verify(model).addAttribute(eq("notes"), anyList());
    }

    @Test
    public void testDeleteNote() {
        String id = firstNote.getId();

        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.delete()
                .uri("http://localhost:8083/notes/{id}", id)
                .retrieve()
                .toBodilessEntity()
                .block())
                .thenReturn(new ResponseEntity<>(HttpStatusCode.valueOf(200)));

        String result = noteController.deleteNote(id, model);

        verify(model).addAttribute(eq("notes"), anyList());
        assertEquals("notes/list", result);
    }

    @Test
    public void testFetchPatientIdForNoteId() {
        String id = firstNote.getId();

        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.get()
                .uri("http://localhost:8083/notes/{id}", id)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block())
                .thenReturn(firstNote);

        Long result = noteController.fetchPatientIdForNoteId(id);

        assertEquals(result, firstPatient.getId());
    }

    @Test
    public void testGetNotes() {

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()
                .uri("http://localhost:8083/notes/patient/{patientId}", firstPatient.getId())
                .retrieve()
                .bodyToFlux(NoteBean.class)
                .collectList()
                .block())
                .thenReturn(notes);

        String result = noteController.getNotes(1L, model);

        assertEquals("notes/list", result);

        verify(model, times(1)).addAttribute("notes", notes);
        verify(model, times(1)).addAttribute("patientId", firstPatient.getId());
    }
}
