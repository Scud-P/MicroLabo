package com.medilabo.micronotes.service;

import com.medilabo.micronotes.domain.Note;
import com.medilabo.micronotes.domain.RiskWord;
import com.medilabo.micronotes.domain.RiskWordCount;
import com.medilabo.micronotes.exception.NoteNotFoundException;
import com.medilabo.micronotes.exception.PatientNotFoundException;
import com.medilabo.micronotes.repository.NoteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class for managing patient notes.
 * Provides methods to retrieve, create, update, delete, and perform operations related to patient notes
 * from the MongoDB database through the NoteRepository.
 */
@Service
public class NoteService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Retrieves a note by its ID.
     *
     * @param id the ID of the note
     * @return the retrieved Note
     * @throws NoteNotFoundException if no note is found with the given ID
     */
    public Note getNoteById(String id) {
        return noteRepository.findById(id).orElseThrow(
                () -> new NoteNotFoundException("No note found for id: " + id)
        );
    }

    /**
     * Retrieves all notes associated with a specific patient ID.
     * Checks if the patient exists by making a REST call to the patient microservice first.
     *
     * @param patientId the ID of the patient
     * @return a list of Note associated with the patient
     * @throws PatientNotFoundException if the patient is not found
     */
    public List<Note> getNotesByPatientId(Long patientId) {
        Boolean existsPatient = webClientBuilder
                .baseUrl("http://microlabo:8081")
                .build()
                .get()
                .uri("/patients/{id}/exists", patientId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (Boolean.FALSE.equals(existsPatient)) {
            throw new PatientNotFoundException("Patient not found for id: " + patientId);
        }
        return noteRepository.findByPatientId(patientId);
    }

    /**
     * Saves a new note.
     *
     * @param note the note to be saved
     * @return the saved Note
     */
    @Transactional
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    /**
     * Deletes a new note.
     *
     * @param id the id of the note to be deleted
     */
    @Transactional
    public void deleteNoteById(String id) {
        noteRepository.deleteById(id);
    }

    /**
     * Updates an existing note.
     *
     * @param note the note containing updated information
     * @return the updated Note
     */
    @Transactional
    public Note updateNote(Note note) {
        Note noteToUpdate = getNoteById(note.getId());
        noteToUpdate.setContent(note.getContent());
        return noteRepository.save(noteToUpdate);
    }

    /**
     * Retrieves all notes from the repository.
     *
     * @return a list of all Note
     */
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    /**
     * Retrieves the contents of notes associated with a specific patient ID.
     *
     * @param patientId the ID of the patient
     * @return a list of note contents
     */
    public List<String> getContentsByPatientId(long patientId) {
        return noteRepository.findContentsByPatientId(patientId);
    }

    /**
     * Retrieves all risk words from the {@link RiskWord} Enum
     *
     * @return a list of risk words
     */
    public List<String> getRiskWords() {
        return Stream.of(RiskWord.values())
                .map(RiskWord::getRiskWord)
                .toList();
    }

    /**
     * Counts the total occurrences of risk words within a list of note contents.
     *
     * @param contents a list of note contents
     * @return the total count of risk word occurrences
     */
    public long countTotalRiskWordOccurrences(List<String> contents) {
        List<String> riskWords = getRiskWords();
        Set<String> countedRiskWords = contents.stream()
                .flatMap(content -> riskWords.stream()
                        .filter(riskWord -> content.toLowerCase().contains(riskWord.toLowerCase())))
                .collect(Collectors.toSet());
        return countedRiskWords.size();
    }

    public int getUniqueRiskWordOccurrences(Long patientId) {
        List<String> riskWords = getRiskWords();
        List<RiskWordCount> riskWordCounts = noteRepository.findRiskWordOccurrences(patientId, riskWords);
        return riskWordCounts.size();
    }

    /**
     * For testing purposes, we need to create Patient Notes
     */

    @PostConstruct
    public void populateNotesCollection() {

        List<Note> allNotes = noteRepository.findAll();

        if (allNotes.isEmpty()) {

            Note firstNote = new Note
                    ("1", 1L, "TestNone",
                            "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé");
            Note secondNote = new Note
                    ("2", 2L, "TestBorderline",
                            "Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement");
            Note thirdNote = new Note
                    ("3", 2L, "TestBorderline",
                            "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois Il remarque également que son audition continue d'être anormale");
            Note fourthNote = new Note
                    ("4", 3L, "TestInDanger",
                            "Le patient déclare qu'il fume depuis peu");
            Note fifthNote = new Note
                    ("5", 3L, "TestInDanger",
                            "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière Il se plaint également de crises d’apnée respiratoire anormales Tests de laboratoire indiquant un taux de cholestérol LDL élevé");
            Note sixthNote = new Note
                    ("6", 4L, "TestEarlyOnset",
                            "Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il se plaint également d’être essoufflé Tests de laboratoire indiquant que les anticorps sont élevés Réaction aux médicaments");
            Note seventhNote = new Note
                    ("7", 4L, "TestEarlyOnset",
                            "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps");
            Note eigthNote = new Note
                    ("8", 4L, "TestEarlyOnset",
                            "Le patient déclare avoir commencé à fumer depuis peu Hémoglobine A1C supérieure au niveau recommandé");
            Note ninthNote = new Note
                    ("9", 4L, "TestEarlyOnset",
                            "Taille, Poids, Cholestérol, Vertige et Réaction");

            List<Note> mockNotes = List.of(firstNote, secondNote, thirdNote, fourthNote, fifthNote, sixthNote, seventhNote, eigthNote, ninthNote);

            noteRepository.saveAll(mockNotes);

            System.out.println("Notes DB populated");
        } else {
            System.out.println("Mock DB was already populated");
        }
    }
}
