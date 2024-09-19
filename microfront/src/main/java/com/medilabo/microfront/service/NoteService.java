package com.medilabo.microfront.service;

import com.medilabo.microfront.beans.NoteBean;
import com.medilabo.microfront.beans.PatientBean;
import com.medilabo.microfront.exception.NoteNotFoundException;
import com.medilabo.microfront.exception.PatientNotFoundException;
import com.medilabo.microfront.exception.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service class for managing notes and interacting with external services using WebClient.
 * Provides methods to fetch, update, validate, and delete notes, as well as to find
 * notes for a patient
 */
@Service
public class NoteService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Fetches a list of notes associated with a given patient ID.
     *
     * @param token     Authorization token for the request.
     * @param patientId The ID of the patient whose notes are being fetched.
     * @return List of {@link NoteBean} objects for the specified patient.
     * @throws PatientNotFoundException    if the patient is not found.
     * @throws UnauthorizedAccessException if no valid token is found.
     */
    public List<NoteBean> fetchNotesByPatientId(String token, Long patientId) {
        return webClientBuilder.build()
                .get()
                .uri("/notes/patient/{patientId}", patientId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> Mono.error(new UnauthorizedAccessException("Unauthorized access, could not find a valid token")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new PatientNotFoundException(
                                "Patient not found for id: " + patientId)))
                .bodyToFlux(NoteBean.class)
                .collectList()
                .block();
    }

    /**
     * Fetches a note for update based on its ID.
     *
     * @param id    The ID of the note to be updated.
     * @param token Authorization token for the request.
     * @return The {@link NoteBean} object for the specified note.
     * @throws NoteNotFoundException       if the note is not found.
     * @throws UnauthorizedAccessException if no valid token is found.
     */
    public NoteBean fetchUpdateNote(String id, String token) {
        return webClientBuilder.build()
                .get()
                .uri("/notes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> Mono.error(new UnauthorizedAccessException("Unauthorized access, could not find a valid token")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new NoteNotFoundException(
                                "Note note found for id: " + id)))
                .bodyToMono(NoteBean.class)
                .block();
    }

    /**
     * Updates a note with the given ID and data.
     *
     * @param id    The ID of the note to be updated.
     * @param note  The {@link NoteBean} object containing the updated note data.
     * @param token Authorization token for the request.
     * @return The updated {@link NoteBean} object.
     * @throws NoteNotFoundException       if the note is not found.
     * @throws UnauthorizedAccessException if no valid token is found.
     */
    public NoteBean updateNote(String id, NoteBean note, String token) {
        return webClientBuilder.build()
                .put()
                .uri("/notes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(note)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> Mono.error(new UnauthorizedAccessException("Unauthorized access, could not find a valid token")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new NoteNotFoundException(
                                "Note note found for id: " + id)))
                .bodyToMono(NoteBean.class)
                .block();
    }

    /**
     * Creates a new note for a specific patient.
     *
     * @param patientId The ID of the patient for whom the note is being created.
     * @param token     Authorization token for the request.
     * @return A new {@link NoteBean} object initialized with the patient's data.
     * @throws PatientNotFoundException    if the patient is not found.
     * @throws UnauthorizedAccessException if no valid token is found.
     */
    public NoteBean showAddNote(Long patientId, String token) {
        PatientBean patient = webClientBuilder.build()
                .get()
                .uri("/patients/{id}", patientId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> Mono.error(new UnauthorizedAccessException("Unauthorized access, could not find a valid token")))
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

    /**
     * Validates the provided note.
     *
     * @param note  The {@link NoteBean} object to be validated.
     * @param token Authorization token for the request.
     * @return The validated {@link NoteBean} object.
     * @throws UnauthorizedAccessException if no valid token is found.
     */
    public NoteBean validateNote(NoteBean note, String token) {
        return webClientBuilder.build()
                .post()
                .uri("/notes/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(note)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> Mono.error(new UnauthorizedAccessException("Unauthorized access, could not find a valid token")))
                .bodyToMono(NoteBean.class)
                .block();
    }

    /**
     * Fetches the patient ID for a given note ID.
     *
     * @param token Authorization token for the request.
     * @param id    The ID of the note for which the patient ID is being fetched.
     * @return The patient ID associated with the note, or null if the note is not found.
     * @throws UnauthorizedAccessException if no valid token is found.
     */
    public Long fetchPatientIdForNoteId(String token, String id) {
        NoteBean note = webClientBuilder.build()
                .get()
                .uri("/notes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> Mono.error(new UnauthorizedAccessException("Unauthorized access, could not find a valid token")))
                .bodyToMono(NoteBean.class)
                .block();
        return note != null ? note.getPatientId() : null;
    }

    /**
     * Deletes a note based on its ID.
     *
     * @param id    The ID of the note to be deleted.
     * @param token Authorization token for the request.
     * @throws NoteNotFoundException if the note is not found.
     * @throws UnauthorizedAccessException if no valid token is found.
     */
    public void deleteNote(String id, String token) {
        webClientBuilder.build()
                .delete()
                .uri("/notes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> Mono.error(new UnauthorizedAccessException("Unauthorized access, could not find a valid token")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new NoteNotFoundException(
                                "Note note found for id: " + id)))
                .toBodilessEntity()
                .block();
    }
}
