package com.medilabo.microfront;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.service.NoteService;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
    private static List<NoteBean> notes;
    private static PatientBean firstPatient;

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
        notes = List.of(firstNote, secondNote);

        LocalDate obiWanBirthdate = LocalDate.of(2017, 5, 8);

        firstPatient = new PatientBean
                (1L, "Obiwan", "Kenobi", obiWanBirthdate, "M", "666 Devil Drive", "111-111-111");
    }
}
