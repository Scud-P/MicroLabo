package com.medilabo.microfront;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.controller.NoteController;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;



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

}
