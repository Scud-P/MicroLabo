package com.medilabo.experiment.microrisk;

import com.medilabo.experiment.microrisk.service.RiskService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class RiskServiceTest {
    @InjectMocks
    private RiskService riskService;

    @Test
    public void testCalculateAge() {
        RiskService spyRiskService = Mockito.spy(this.riskService);
        LocalDate birthdate = LocalDate.of(2000, 1, 1);
        Mockito.doReturn(birthdate).when(spyRiskService).fetchBirthDate(anyLong());
        int age = spyRiskService.calculateAge(1L);
        Assertions.assertEquals(24, age);
    }

    @Test
    public void testCalculateAge_WithNullBirthdate() {
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(null).when(spyRiskService).fetchBirthDate(anyLong());
        int age = spyRiskService.calculateAge(1L);
        Assertions.assertEquals(0, age);
    }

    @Test
    public void testGetRiskWords() {
        List<String> expectedRiskWords = List.of("Hémoglobine A1C", "Microalbumine", "Taille", "Poids", "Fumeur", "Fumeuse", "Anormal", "Cholestérol", "Vertiges", "Rechute", "Réaction", "Anticorps");
        List<String> actualRiskWords = this.riskService.getRiskWords();
        Assertions.assertEquals(expectedRiskWords, actualRiskWords);
    }

    @Test
    public void testGetExclusionWords() {
        List<String> expectedExclusionWords = List.of("Égal", "Recommandé");
        List<String> actualExclusionWords = this.riskService.getExclusionWords();
        Assertions.assertEquals(expectedExclusionWords, actualExclusionWords);
    }

    @Test
    public void testGetRiskWordOccurrences_WithNoExclusionWord() {
        List<String> contents = List.of("Je vais chez ce cher Serge", "J'aime les petits poids", "Vertiges de l'amour");
        List<String> riskWords = List.of("Hémoglobine A1C", "Microalbumine", "Taille", "Poids", "Fumeur", "Fumeuse", "Anormal", "Cholestérol", "Vertiges", "Rechute", "Réaction", "Anticorps");
        List<String> exclusionWords = List.of("Égal", "Recommandé");
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(contents).when(spyRiskService).fetchContents(anyLong());
        Mockito.doReturn(riskWords).when(spyRiskService).getRiskWords();
        Mockito.doReturn(exclusionWords).when(spyRiskService).getExclusionWords();
        int riskWordOccurrences = spyRiskService.getRiskWordOccurrences(1L);
        Assertions.assertEquals(2, riskWordOccurrences);
    }

    @Test
    public void testGetRiskWordOccurrences_WithOneExclusionWordAfterARiskWord() {
        List<String> contents = List.of("Je vais chez ce cher Serge", "J'aime les petits poids recommandé", "Vertiges de l'amour");
        List<String> riskWords = List.of("Hémoglobine A1C", "Microalbumine", "Taille", "Poids", "Fumeur", "Fumeuse", "Anormal", "Cholestérol", "Vertiges", "Rechute", "Réaction", "Anticorps");
        List<String> exclusionWords = List.of("Égal", "Recommandé");
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(contents).when(spyRiskService).fetchContents(anyLong());
        Mockito.doReturn(riskWords).when(spyRiskService).getRiskWords();
        Mockito.doReturn(exclusionWords).when(spyRiskService).getExclusionWords();
        int riskWordOccurrences = spyRiskService.getRiskWordOccurrences(1L);
        Assertions.assertEquals(1, riskWordOccurrences);
    }

    @Test
    public void testGetRiskWordOccurrences_WithOneExclusionWordBeforeARiskWord() {
        List<String> contents = List.of("Je vais chez ce cher Serge", "J'aime les petits égal poids", "Vertiges de l'amour");
        List<String> riskWords = List.of("Hémoglobine A1C", "Microalbumine", "Taille", "Poids", "Fumeur", "Fumeuse", "Anormal", "Cholestérol", "Vertiges", "Rechute", "Réaction", "Anticorps");
        List<String> exclusionWords = List.of("Égal", "Recommandé");
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(contents).when(spyRiskService).fetchContents(anyLong());
        Mockito.doReturn(riskWords).when(spyRiskService).getRiskWords();
        Mockito.doReturn(exclusionWords).when(spyRiskService).getExclusionWords();
        int riskWordOccurrences = spyRiskService.getRiskWordOccurrences(1L);
        Assertions.assertEquals(1, riskWordOccurrences);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnNone_whenNoRiskWordOccurrencesAreFound() {
        int age = 24;
        String gender = "m";
        int riskWordOccurrences = 0;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "None";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnInDanger_forMaleUnder30_With3RiskWordOccurrences() {
        int age = 24;
        String gender = "m";
        int riskWordOccurrences = 3;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "In Danger";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnInDanger_forMaleOver30_With7RiskWordOccurrences() {
        int age = 40;
        String gender = "m";
        int riskWordOccurrences = 7;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "In Danger";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnBorderline_forMaleOver30_With3RiskWordOccurrences() {
        int age = 40;
        String gender = "m";
        int riskWordOccurrences = 3;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "Borderline";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnNone_forMaleUnder30_With2RiskWordOccurrences() {
        int age = 25;
        String gender = "m";
        int riskWordOccurrences = 2;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "None";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnEarlyOnset_forMaleUnder30_With5RiskWordOccurrences() {
        int age = 25;
        String gender = "m";
        int riskWordOccurrences = 5;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "Early onset";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnEarlyOnset_forMaleUnder30_With10RiskWordOccurrences() {
        int age = 25;
        String gender = "m";
        int riskWordOccurrences = 10;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "Early onset";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnEarlyOnset_forFemaleUnder30_With5RiskWordOccurrences() {
        int age = 25;
        String gender = "f";
        int riskWordOccurrences = 5;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "In Danger";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnEarlyOnset_forFemaleUnder30_With3RiskWordOccurrences() {
        int age = 25;
        String gender = "f";
        int riskWordOccurrences = 3;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "None";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnEarlyOnset_forFemaleUnder30_With8RiskWordOccurrences() {
        int age = 25;
        String gender = "f";
        int riskWordOccurrences = 8;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "Early onset";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnInDanger_forFemaleOver30_With7RiskWordOccurrences() {
        int age = 40;
        String gender = "f";
        int riskWordOccurrences = 7;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "In Danger";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnInDanger_forFemaleOver30_With8RiskWordOccurrences() {
        int age = 40;
        String gender = "f";
        int riskWordOccurrences = 8;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "Early onset";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnNone_forFemaleOver30_With1RiskWordOccurrences() {
        int age = 40;
        String gender = "f";
        int riskWordOccurrences = 1;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "Early onset";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnEarlyOnset_forMaleUnder30_With7RiskWordOccurrences() {
        int age = 25;
        String gender = "m";
        int riskWordOccurrences = 7;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "Early onset";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }

    @Test
    public void calculateRiskForAPatient_ShouldReturnEarlyOnset_ForMaleOver30_With10RiskWordOccurrences() {
        int age = 40;
        String gender = "m";
        int riskWordOccurrences = 10;
        RiskService spyRiskService = Mockito.spy(this.riskService);
        Mockito.doReturn(age).when(spyRiskService).calculateAge(anyLong());
        Mockito.doReturn(gender).when(spyRiskService).fetchGender(anyLong());
        Mockito.doReturn(riskWordOccurrences).when(spyRiskService).getRiskWordOccurrences(anyLong());
        String expectedRisk = "Early onset";
        String actualRisk = spyRiskService.calculateRiskForPatient(1L);
        Assertions.assertEquals(expectedRisk, actualRisk);
    }
}