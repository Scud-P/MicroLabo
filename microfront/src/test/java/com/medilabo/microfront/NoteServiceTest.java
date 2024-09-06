package com.medilabo.microfront;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.NoteNotFoundException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import com.medilabo.microfront.service.NoteService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class NoteServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @Autowired
    private NoteService noteService;

    @Mock
    private Model model;

    private static MockWebServer gatewayMockServer;
    private static MockWebServer authMockServer;

    private static NoteBean firstNote;
    private static NoteBean secondNote;
    private static NoteBean updatedFirstNote;
    private static List<NoteBean> notes;
    private static PatientBean firstPatient;

    private String validToken;
    private ObjectMapper mapper;

    @BeforeAll
    public static void startMockServers() throws Exception {
        gatewayMockServer = new MockWebServer();
        gatewayMockServer.start(8080);
        authMockServer = new MockWebServer();
        authMockServer.start(8085);
    }

    @AfterAll
    public static void shutDown() throws Exception {
        gatewayMockServer.shutdown();
        authMockServer.shutdown();
    }


    @BeforeEach
    public void setUp() {
        firstNote = new NoteBean("firstNoteId", 1L, "Kenobi", "Patient is leaning towards the dark side of the force");
        secondNote = new NoteBean("secondNoteId", 1L, "Kenobi", "Patient admits being traumatized by killing his Padawan");
        updatedFirstNote = new NoteBean("firstNoteId", 1L, "Kenobi", "Patient assaulted his Padawan");
        notes = List.of(firstNote, secondNote);

        LocalDate obiWanBirthdate = LocalDate.of(2017, 5, 8);

        firstPatient = new PatientBean
                (1L, "Obiwan", "Kenobi", obiWanBirthdate, "M", "666 Devil Drive", "111-111-111");
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        validToken = "someValidToken";
    }

    @Test
    public void testFetchNotesByPatientId() throws JsonProcessingException {
        String jsonFirstNote = mapper.writeValueAsString(firstNote);
        String jsonSecondNote = mapper.writeValueAsString(secondNote);

        String jsonNotes = "[" + jsonFirstNote + "," + jsonSecondNote + "]";

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonNotes)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        List<NoteBean> foundNotes = noteService.fetchNotesByPatientId(validToken, firstPatient.getId());

        assertEquals(notes, foundNotes);
    }

    @Test
    public void testFetchNotesByPatientIdPatientNotFound() {
        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(PatientNotFoundException.class, () -> noteService.fetchNotesByPatientId(validToken, firstPatient.getId()));
    }

    @Test
    public void testFetchUpdateNote() throws JsonProcessingException {
        String jsonFirstNote = mapper.writeValueAsString(firstNote);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonFirstNote)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        NoteBean foundNote = noteService.fetchUpdateNote(firstNote.getId(), validToken);

        assertEquals(firstNote, foundNote);
    }

    @Test
    public void testFetchUpdateNoteNotFound() {
        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(NoteNotFoundException.class, () -> noteService.fetchUpdateNote(firstNote.getId(), validToken));
    }

    @Test
    public void testUpdateNote() throws JsonProcessingException {
        String jsonUpdatedNote = mapper.writeValueAsString(updatedFirstNote);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonUpdatedNote)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        NoteBean updatedNote = noteService.updateNote(firstNote.getId(), firstNote, validToken);

        assertEquals(updatedFirstNote, updatedNote);
    }

    @Test
    public void testUpdateNoteNotFound() {
        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(NoteNotFoundException.class, () -> noteService.updateNote(firstNote.getId(), firstNote, validToken));
    }

    @Test
    public void testShowAddNote() throws JsonProcessingException {

        NoteBean newNote = new NoteBean();
        newNote.setPatientId(firstPatient.getId());
        newNote.setPatientLastName(firstPatient.getLastName());

        String jsonPatient = mapper.writeValueAsString(firstPatient);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonPatient)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        NoteBean prefilledNewNote = noteService.showAddNote(firstPatient.getId(), validToken);

        assertEquals(newNote, prefilledNewNote);
    }

    @Test
    public void testShowAddNotePatientNotFound() {
        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(PatientNotFoundException.class, () -> noteService.showAddNote(firstPatient.getId(), validToken));
    }

    @Test
    public void testValidateNote() throws JsonProcessingException {
        String jsonFirstNote = mapper.writeValueAsString(firstNote);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonFirstNote)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        NoteBean addedNote = noteService.validateNote(firstNote, validToken);
        assertEquals(firstNote, addedNote);
    }

    @Test
    public void testFetchPatientIdForNoteId() throws JsonProcessingException {
        String jsonFirstNote = mapper.writeValueAsString(firstNote);

        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonFirstNote)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        long patientId = noteService.fetchPatientIdForNoteId(validToken, firstNote.getId());

        assertEquals(firstNote.getPatientId(), patientId);
    }

    @Test
    public void testDeleteNote() {
        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        noteService.deleteNote(firstNote.getId(), validToken);
    }

    @Test
    public void testDeleteNoteNotFound() {
        gatewayMockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json"));

        when(webClientBuilder.build()).thenReturn(WebClient.builder()
                .baseUrl(gatewayMockServer.url("/").toString())
                .build());

        assertThrows(NoteNotFoundException.class, () ->  noteService.deleteNote(firstNote.getId(), validToken));
    }

}
