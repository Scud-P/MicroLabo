package com.medilabo.micronotes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabo.micronotes.domain.Note;
import com.medilabo.micronotes.exception.NoteNotFoundException;
import com.medilabo.micronotes.repository.NoteRepository;
import com.medilabo.micronotes.service.NoteService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
public class NoteServiceTest {

    @Autowired
    private NoteService noteService;

    @MockBean
    private NoteRepository noteRepository;

    @MockBean
    private WebClient.Builder webClientBuilder;


    private static Note firstNote;
    private static Note secondNote;
    private static List<Note> notes;

    private static MockWebServer mockPatientServer;


    @BeforeAll
    public static void startMockServers() throws Exception {
        mockPatientServer = new MockWebServer();
        mockPatientServer.start(8081);
        System.out.println("Mock patient server started");
    }

    @AfterAll
    public static void shutDown() throws Exception {
        mockPatientServer.shutdown();
    }

    @BeforeEach
    public void setUp() {
        // cholestérol and anormal are riskWords
        firstNote = new Note("firstNoteId", 1L, "Kenobi", "Patient is leaning towards the dark side of the force, but also somehow has cholestérol");
        secondNote = new Note("secondNoteId", 1L, "Kenobi", "Patient admits being traumatized by killing his anormal Padawan");
        notes = List.of(firstNote, secondNote);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
    }

    @Test
    public void getAllNotes_shouldReturnTheContentOfOurNoteRepository() {
        when(noteRepository.findAll()).thenReturn(notes);
        List<Note> allNotes = noteService.getAllNotes();
        assertEquals(notes, allNotes);
    }

    @Test
    public void getNoteById_shouldReturnTheCorrectNote_forTheCorrectId() {
        when(noteRepository.findById(firstNote.getId())).thenReturn(Optional.ofNullable(firstNote));
        Note foundNote = noteService.getNoteById(firstNote.getId());
        assertEquals(firstNote, foundNote);
    }

    @Test
    public void getNoteById_shouldReturnNull_whenNotFound() {
        when(noteRepository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(NoteNotFoundException.class, () -> noteService.getNoteById("id"));
    }

    @Test
    public void getNotesByPatientId_shouldReturnAllNotesWithTheCorrectPatientId() throws JsonProcessingException {

        boolean exists = true;

        mockPatientServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new ObjectMapper().writeValueAsString(exists))
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build())
                .thenReturn(WebClient.builder()
                .baseUrl(mockPatientServer.url("/").toString())
                .build());

        when(noteRepository.findByPatientId(anyLong())).thenReturn(notes);

        List<Note> foundNotes = noteService.getNotesByPatientId(firstNote.getPatientId());
        assertEquals(notes, foundNotes);
    }

    @Test
    public void saveNote_shouldSaveTheCorrectNote() {
        when(noteRepository.save(any(Note.class))).thenReturn(firstNote);
        Note savedNote = noteService.saveNote(firstNote);
        assertEquals(firstNote, savedNote);
    }

    @Test
    public void deleteNoteById_shouldDeleteTheCorrectNote() {
        noteService.saveNote(firstNote);
        noteService.deleteNoteById("firstNoteId");
        verify(noteRepository, times(1)).deleteById("firstNoteId");
    }

    @Test
    public void updateNote_shouldUpdateTheCorrectNote() {

        Note updatedNote = new Note(
                firstNote.getId(), 1L, "Kenobi", "updatedContent"
        );

        when(noteRepository.findById(anyString())).thenReturn(Optional.ofNullable(firstNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note resultingNote = noteService.updateNote(updatedNote);

        assertEquals(1L, resultingNote.getPatientId());
        assertEquals(firstNote.getId(), resultingNote.getId());
        assertEquals(updatedNote.getContent(), resultingNote.getContent());

        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    public void getContentsByPatientId_ShouldReturnTheCompleteContents() {
        List<String> contents = List.of(firstNote.getContent(), secondNote.getContent());
        when(noteRepository.findContentsByPatientId(1L)).thenReturn(contents);

        List<String> foundContents = noteService.getContentsByPatientId(1L);

        assertEquals(contents, foundContents);
    }

    @Test
    public void getRiskWords_shouldReturnTheContentsOfOurEnum_asAListOfStrings() {

        List<String> expectedRiskWords = List.of("Hémoglobine A1C", "Microalbumine", "Taille", "Poids", "Fumeur", "Fumeuse",
                "Anormal", "Cholestérol", "Vertiges", "Rechute", "Réaction", "Anticorps");

        List<String> foundRiskWords = noteService.getRiskWords();
        assertEquals(expectedRiskWords, foundRiskWords);
    }

    @Test
    public void countTotalRiskWordOccurrences_shouldReturnTheCorrectAmountOfRiskWords_inAListOfStrings() {
        List<String> contents = List.of(firstNote.getContent(), secondNote.getContent());

        long result = noteService.countTotalRiskWordOccurrences(contents);

        assertEquals(2, result);
    }

    @Test
    public void countTotalRiskWordOccurrences_shouldReturnTheCorrectAmountOfRiskWords_inAListOfStringsWithWrongCase() {
        String content1 = firstNote.getContent().toUpperCase();
        String content2 = secondNote.getContent().toUpperCase();

        List<String> contents = List.of(content1, content2);

        long result = noteService.countTotalRiskWordOccurrences(contents);

        assertEquals(2, result);
    }

    @Test
    public void countTotalRiskWordOccurrences_shouldReturnTheCorrectAmountOfRiskWords_andAvoidCountingDuplicates() {
        List<String> contents = List.of(firstNote.getContent(), secondNote.getContent(), firstNote.getContent(), secondNote.getContent());

        long result = noteService.countTotalRiskWordOccurrences(contents);

        assertEquals(2, result);
    }
}
