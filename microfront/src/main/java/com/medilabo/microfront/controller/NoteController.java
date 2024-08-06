package com.medilabo.microfront.controller;

import com.medilabo.microfront.beans.NoteBean;
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
    public String getNotes(@PathVariable("patientId") Long patientId, Model model) {

        WebClient webClient = webClientBuilder.build();

        List<NoteBean> notes = webClient.get()
                .uri("http://localhost:8083/notes/{patientId}", patientId)
                .retrieve()
                .bodyToFlux(NoteBean.class)
                .collectList()
                .block();

        model.addAttribute("notes", notes);
        return "notes/list";
    }
}
