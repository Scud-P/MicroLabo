package com.medilabo.microfront;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.controller.NoteController;

import com.medilabo.microfront.proxies.MicroNotesProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteController noteController;

    @MockBean
    private MicroNotesProxy microNotesProxy;

    private static NoteBean firstNote;
    private static NoteBean secondNote;
    private static List<NoteBean> notes;

    @BeforeEach
    public void setUp() {
        firstNote = new NoteBean("firstNoteId", 1L, "Kenobi", "Patient is leaning towards the dark side of the force");
        secondNote = new NoteBean("secondNoteId", 1L, "Kenobi", "Patient admits being traumatized by killing his Padawan");
        notes = List.of(firstNote, secondNote);
    }

    @Test
    public void getRequest_toShowAddNoteForm_shouldCreateANewNoteBean_andAddItToTheModel() throws Exception {
        mockMvc.perform(get("/notes/add")
                .param("patientId", String.valueOf(1))
                .param("patientLastName", "Kenobi"))
                .andExpect(status().isOk())
                .andExpect(view().name("notes/add"))
                .andExpect(model().attributeExists("note"));
    }

    @Test
    public void getRequest_toGetAllNotesByPatientId_shouldDisplayAllPatientNotes() throws Exception {
        when(microNotesProxy.getAllNotesById(anyLong())).thenReturn(notes);

        mockMvc.perform(get("/notes/1")
                .param("patientId", String.valueOf(1L)))
                .andExpect(status().isOk())
                .andExpect(view().name("notes/list"))
                .andExpect(model().attributeExists("notes"));
    }

    @Test
    public void getRequest_toGetAllNotesByPatientId_shouldDisplayAnEmptyNoteList_whenProxySendsAnEmptyList() throws Exception {

        List<NoteBean> emptyList = new ArrayList<>();

        when(microNotesProxy.getAllNotesById(anyLong())).thenReturn(emptyList);

        mockMvc.perform(get("/notes/1")
                        .param("patientId", String.valueOf(1L)))
                .andExpect(status().isOk())
                .andExpect(view().name("notes/list"))
                .andExpect(model().attribute("notes", emptyList));
    }

    @Test
    public void postRequest_toValidateNote_shouldValidateTheNewNote_andRedirectToTheUpdatedNoteListForThePatient() throws Exception {
        mockMvc.perform(post("/notes/validate")
                        .param("patientId", String.valueOf(1L))
                        .param("patientLastName", firstNote.getPatientLastName())
                        .param("content", "Some new note!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes/list?patientId=1"));

        verify(microNotesProxy, times(1)).validateNote(any(NoteBean.class));
    }

    @Test
    public void getRequest_toShowUpdateForm_displaysTheCorrectForm() throws Exception {
        when(microNotesProxy.getNoteById(firstNote.getId())).thenReturn(firstNote);

        mockMvc.perform(get("/notes/update/{id}", firstNote.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("notes/update"))
                .andExpect(model().attribute("note", firstNote));
    }

    @Test
    public void putRequest_toUpdateNote_UpdatesTheCorrectForm() throws Exception {
        mockMvc.perform(put("/notes/update")
                        .flashAttr("note", secondNote))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes/list?patientId=1"));
    }

    @Test
    public void deleteRequest_toDeleteNote_deletesTheCorrectNote() throws Exception {
        when(microNotesProxy.getNoteById(anyString())).thenReturn(firstNote);

        mockMvc.perform(delete("/notes/{id}", firstNote.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes/list?patientId=1"));

        verify(microNotesProxy, times(1)).deleteNote(firstNote.getId());
    }

}
