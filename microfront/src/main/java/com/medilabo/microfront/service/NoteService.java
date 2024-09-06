package com.medilabo.microfront.service;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.NoteNotFoundException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class NoteService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    public List<NoteBean> fetchNotesByPatientId(String token, Long patientId) {
        return webClientBuilder.build()
                .get()
                .uri("/notes/patient/{patientId}", patientId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new PatientNotFoundException(
                                "Patient not found for id: " + patientId)))
                .bodyToFlux(NoteBean.class)
                .collectList()
                .block();
    }

    public NoteBean fetchUpdateNote(String id, String token) {
        return webClientBuilder.build()
                .get()
                .uri("/notes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new NoteNotFoundException(
                                "Note note found for id: " + id)))
                .bodyToMono(NoteBean.class)
                .block();
    }

    public NoteBean updateNote(String id, NoteBean note, String token) {
        return webClientBuilder.build()
                .put()
                .uri("/notes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(note)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new NoteNotFoundException(
                                "Note note found for id: " + id)))
                .bodyToMono(NoteBean.class)
                .block();
    }

    public NoteBean showAddNote(Long patientId, String token) {
        PatientBean patient = webClientBuilder.build()
                .get()
                .uri("/patients/{id}", patientId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new PatientNotFoundException(
                                "Patient not found for id: " + patientId)))
                .bodyToMono(PatientBean.class)
                .block();

        NoteBean note = new NoteBean();
        note.setPatientId(patientId);
        note.setPatientLastName(patient.getLastName());
        return note;
    }

    public NoteBean validateNote(NoteBean note, String token) {
       return webClientBuilder.build()
                .post()
                .uri("/notes/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(note)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block();
    }

    public Long fetchPatientIdForNoteId(String token, String id) {
        NoteBean note =  webClientBuilder.build()
                .get()
                .uri("/notes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(NoteBean.class)
                .block();
        return note != null ? note.getPatientId() : null;
    }

    public void deleteNote(String id, String token) {
        webClientBuilder.build()
                .delete()
                .uri("/notes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new NoteNotFoundException(
                                "Note note found for id: " + id)))
                .toBodilessEntity()
                .block();
    }
}
