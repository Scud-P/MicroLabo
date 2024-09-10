package com.medilabo.micronotes.service;

import com.medilabo.micronotes.domain.Note;
import com.medilabo.micronotes.domain.RiskWord;
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

@Service
public class NoteService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Note getNoteById(String id) {
        return noteRepository.findById(id).orElseThrow(
                () -> new NoteNotFoundException("No note found for id: " + id)
        );
    }

    public List<Note> getNotesByPatientId(Long patientId) {
        Boolean existsPatient = webClientBuilder.build()
                .get()
                .uri("http://microlabo:8081/patients/{id}/exists", patientId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (Boolean.FALSE.equals(existsPatient)) {
            throw new PatientNotFoundException("Patient not found for id: " + patientId);
        }
        return noteRepository.findByPatientId(patientId);
    }

    @Transactional
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    @Transactional
    public void deleteNoteById(String id) {
        noteRepository.deleteById(id);
    }

    @Transactional
    public Note updateNote(Note note) {
        Note noteToUpdate = getNoteById(note.getId());
        noteToUpdate.setContent(note.getContent());
        return noteRepository.save(noteToUpdate);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public List<String> getContentsByPatientId(long patientId) {
        return noteRepository.findContentsByPatientId(patientId);
    }

    public List<String> getRiskWords() {
        return Stream.of(RiskWord.values())
                .map(RiskWord::getRiskWord)
                .toList();
    }

    public long countTotalRiskWordOccurrences(List<String> contents) {
        List<String> riskWords = getRiskWords();
        Set<String> countedRiskWords = contents.stream()
                .flatMap(content -> riskWords.stream()
                        .filter(riskWord -> content.toLowerCase().contains(riskWord.toLowerCase())))
                .collect(Collectors.toSet());
        return countedRiskWords.size();
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
