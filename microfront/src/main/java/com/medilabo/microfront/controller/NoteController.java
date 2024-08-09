package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Controller
public class NoteController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("/notes/patient/{patientId}")
    public String getNotes(@PathVariable("patientId") Long patientId,
                           Model model) {
        return updateModelWithPatientNotes(patientId, model);
    }

    @GetMapping("/notes/update/{id}")
    public String showUpdateNote(@PathVariable("id") String id,
                                 Model model) {

        NoteBean note = webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/notes/{id}", id)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block();

        model.addAttribute("note", note);
        return "notes/update";
    }

    @PutMapping("/notes/{id}")
    public String updateNote(@PathVariable("id") String id,
                             @ModelAttribute NoteBean note,
                             Model model) {

        NoteBean updatedNote = webClientBuilder.build()
                .put()
                .uri("http://localhost:8083/notes/{id}", id)
                .bodyValue(note)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block();

        Long patientId = note.getPatientId();
        return updateModelWithPatientNotes(patientId, model);
    }

    @GetMapping("/notes/add/{patientId}")
    public String showAddNote(@PathVariable("patientId") Long patientId, Model model) {

        PatientBean patient = webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/patients/{id}", patientId)
                .retrieve()
                .bodyToMono(PatientBean.class)
                .block();

        NoteBean note = new NoteBean();
        note.setPatientId(patientId);
        note.setPatientLastName(patient.getLastName());
        model.addAttribute("note", note);
        return "notes/add";
    }

    @PostMapping("/notes/validate")
    public String validateNote(@ModelAttribute NoteBean note, Model model) {
        webClientBuilder.build()
                .post()
                .uri("http://localhost:8083/notes/validate")
                .bodyValue(note)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block();

        Long patientId = note.getPatientId();

        return updateModelWithPatientNotes(patientId, model);
    }

    @DeleteMapping("/notes/{id}")
    public String deleteNote(@PathVariable("id") String id,
                             Model model) {

        Long patientId = fetchPatientIdForNoteId(id);

        webClientBuilder.build()
                .delete()
                .uri("http://localhost:8083/notes/{id}", id)
                .retrieve()
                .toBodilessEntity()
                .block();

        return updateModelWithPatientNotes(patientId, model);
    }

    public Long fetchPatientIdForNoteId(String id) {
        NoteBean note = webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/notes/{id}", id)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block();

        return note != null ? note.getPatientId() : null;
    }

    private List<NoteBean> fetchNotesByPatientId(Long patientId) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/notes/patient/{patientId}", patientId)
                .retrieve()
                .bodyToFlux(NoteBean.class)
                .collectList()
                .block();
    }

    private String updateModelWithPatientNotes(Long patientId, Model model) {
        List<NoteBean> notes = fetchNotesByPatientId(patientId);
        model.addAttribute("notes", notes);
        model.addAttribute("patientId", patientId);
        return "notes/list";
    }


}
