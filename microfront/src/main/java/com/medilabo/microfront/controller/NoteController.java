package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.NoteNotFoundException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
public class NoteController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("/notes/patient/{patientId}")
    public String getNotes(@PathVariable("patientId") Long patientId, Model model) {
        try {
            return updateModelWithPatientNotes(patientId, model);

        } catch (PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/notes/update/{id}")
    public String showUpdateNote(@PathVariable("id") String id, Model model) {
        try {
            NoteBean note = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8083/notes/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> Mono.error(new NoteNotFoundException(
                                    "Note note found for id: " + id)))
                    .bodyToMono(NoteBean.class)
                    .block();

            model.addAttribute("note", note);
            return "notes/update";

        } catch (NoteNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @PutMapping("/notes/{id}")
    public String updateNote(@PathVariable("id") String id,
                             @ModelAttribute NoteBean note,
                             Model model) {
        try {
            webClientBuilder.build()
                    .put()
                    .uri("http://localhost:8083/notes/{id}", id)
                    .bodyValue(note)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> Mono.error(new NoteNotFoundException(
                                    "Note note found for id: " + id)))
                    .bodyToMono(NoteBean.class)
                    .block();

            Long patientId = note.getPatientId();
            return updateModelWithPatientNotes(patientId, model);

        } catch (NoteNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
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

        try {
            Long patientId = fetchPatientIdForNoteId(id);

            webClientBuilder.build()
                    .delete()
                    .uri("http://localhost:8083/notes/{id}", id)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return updateModelWithPatientNotes(patientId, model);

        } catch (NoteNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
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
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new PatientNotFoundException(
                                "Patient not found for id: " + patientId)))
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
