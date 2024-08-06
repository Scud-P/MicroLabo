//package com.medilabo.microfront.controller;
//
//
//import com.medilabo.microfront.beans.NoteBean;
//import com.medilabo.microfront.proxies.MicroNotesProxy;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Controller
//public class NoteController {
//
//    private final MicroNotesProxy microNotesProxy;
//
//    public NoteController(MicroNotesProxy microNotesProxy) {
//        this.microNotesProxy = microNotesProxy;
//    }
//
//    @GetMapping("/notes/add")
//    public String showAddNoteForm(@RequestParam(value = "patientId", required = false) Long patientId,
//                                  @RequestParam(value = "patientLastName", required = false) String patientLastName,
//                                  Model model) {
//        NoteBean note = new NoteBean();
//        if (patientId != null) {
//            note.setPatientId(patientId);
//        }
//        if (patientLastName != null) {
//            note.setPatientLastName(patientLastName);
//        }
//        model.addAttribute("note", note);
//        return "notes/add";
//    }
//
//
//    @GetMapping("/notes/{patientId}")
//    public String getAllNotesByPatientId(@RequestParam Long patientId, Model model) {
//        List<NoteBean> notes = microNotesProxy.getAllNotesById(patientId);
//        if (notes.isEmpty()) {
//            List<NoteBean> emptyList = new ArrayList<>();
//            model.addAttribute("notes", emptyList);
//        } else {
//            model.addAttribute("notes", notes);
//        }
//        return "notes/list";
//    }
//
//    @PostMapping("/notes/validate")
//    public String validateNote(@RequestParam Long patientId,
//                               @RequestParam String patientLastName,
//                               @RequestParam String content,
//                               Model model) {
//
//        System.out.println("Patient Id: " + patientId);
//        System.out.println("Patient LastName: " + patientLastName);
//        System.out.println("Note content: " + content);
//
//        NoteBean noteToAdd = new NoteBean();
//        noteToAdd.setPatientLastName(patientLastName);
//        noteToAdd.setPatientId(patientId);
//        noteToAdd.setContent(content);
//        microNotesProxy.validateNote(noteToAdd);
//        System.out.println("Note: " + noteToAdd);
//        System.out.println("Patient ID: " + patientId);
//        List<NoteBean> notes = microNotesProxy.getAllNotesById(patientId);
//        model.addAttribute("notes", notes);
//        return "redirect:/notes/list?patientId=" + patientId;
//    }
//
//    @GetMapping("/notes/update/{id}")
//    public String showUpdateForm(@PathVariable("id") String id, Model model) {
//        NoteBean noteToUpdate = microNotesProxy.getNoteById(id);
//        System.out.println(noteToUpdate);
//        model.addAttribute("note", noteToUpdate);
//        return "notes/update";
//    }
//
//    @PutMapping("/notes/update")
//    public String updateNote(@ModelAttribute("note") NoteBean note) {
//        System.out.println("Received note: " + note);
//        microNotesProxy.updateNote(note);
//        return "redirect:/notes/list?patientId=" + note.getPatientId();
//    }
//
//    @DeleteMapping(value = "/notes/{id}")
//    public String deleteNote(@PathVariable String id, Model model) {
//        NoteBean noteToDelete = microNotesProxy.getNoteById(id);
//        long patientId = noteToDelete.getPatientId();
//        microNotesProxy.deleteNote(id);
//        List<NoteBean> notes = microNotesProxy.getAllNotesById(patientId);
//        model.addAttribute("notes", notes);
//        return "redirect:/notes/list?patientId=" + patientId;
//    }
//
//    // TODO Continue exploring this, it works while accessing http://localhost:8081/notes/patient/risk/4
//
//    @GetMapping(value = "/notes/patient/risk/{patientId}")
//    public String getRisk(@PathVariable long patientId, Model model) {
//
//        long risk = microNotesProxy.getRisk(patientId);
//        model.addAttribute("risk", risk);
//
//        return "notes/riskTest";
//    }
//
//
//    //TODO catch backend exceptions?
//}
