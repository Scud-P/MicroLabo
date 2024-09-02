package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.NoteNotFoundException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequestMapping("/api")
public class NoteController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("/notes/patient/{patientId}")
    public String getNotes(@CookieValue(name = "token", required = false) String token,
                           @PathVariable("patientId") Long patientId,
                           Model model) {
        try {
            return updateModelWithPatientNotes(token, patientId, model);

        } catch (PatientNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    private List<NoteBean> fetchNotesByPatientId(@CookieValue(name = "token", required = false) String token,
                                                 Long patientId) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8080/notes/patient/{patientId}", patientId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new PatientNotFoundException(
                                "Patient not found for id: " + patientId)))
                .bodyToFlux(NoteBean.class)
                .collectList()
                .block();
    }

    private String updateModelWithPatientNotes(String token, Long patientId, Model model) {
        List<NoteBean> notes = fetchNotesByPatientId(token, patientId);
        model.addAttribute("notes", notes);
        model.addAttribute("patientId", patientId);
        return "notes/list";
    }

    @GetMapping("/notes/update/{id}")
    public String showUpdateNote(@PathVariable("id") String id, Model model) {
        try {
            NoteBean note = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8080/notes/{id}", id)
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
                             @CookieValue(name = "token", required = false) String token,
                             Model model) {
        try {
            webClientBuilder.build()
                    .put()
                    .uri("http://localhost:8080/notes/{id}", id)
                    .bodyValue(note)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> Mono.error(new NoteNotFoundException(
                                    "Note note found for id: " + id)))
                    .bodyToMono(NoteBean.class)
                    .block();

            Long patientId = note.getPatientId();
            return updateModelWithPatientNotes(token, patientId, model);

        } catch (NoteNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/notes/add/{patientId}")
    public String showAddNote(@PathVariable("patientId") Long patientId, Model model) {

        PatientBean patient = webClientBuilder.build()
                .get()
                .uri("http://localhost:8080/patients/{id}", patientId)
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
    public String validateNote(@CookieValue(name = "token", required = false) String token,
                               @ModelAttribute NoteBean note,
                               Model model) {

        webClientBuilder.build()
                .post()
                .uri("http://localhost:8080/notes/validate")
                .bodyValue(note)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block();

        Long patientId = note.getPatientId();

        return updateModelWithPatientNotes(token, patientId, model);
    }

    @DeleteMapping("/notes/{id}")
    public String deleteNote(@CookieValue(name = "token", required = false) String token,
                             @PathVariable("id") String id,
                             Model model) {

        try {
            Long patientId = fetchPatientIdForNoteId(token, id);

            webClientBuilder.build()
                    .delete()
                    .uri("http://localhost:8080/notes/{id}", id)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return updateModelWithPatientNotes(token, patientId, model);

        } catch (NoteNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    public Long fetchPatientIdForNoteId(@CookieValue(name = "token", required = false) String token,
                                        String id) {
        NoteBean note = webClientBuilder.build()
                .get()
                .uri("http://localhost:8080/notes/{id}", id)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block();
        return note != null ? note.getPatientId() : null;
    }

}
