package com.fundoo.fundoo.controller;

import com.fundoo.fundoo.model.Note;
import com.fundoo.fundoo.model.payload.request.NoteDTO;
import com.fundoo.fundoo.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/note")
@Slf4j
public class NoteController {

    private final NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/create")
    public ResponseEntity<Note> createNote(@RequestBody NoteDTO noteDTO,
                                             @RequestHeader("Authorization") String token) {
        log.info("Request to create Note: {}", noteDTO);
        return new ResponseEntity<>(noteService.createNote(noteDTO, token), HttpStatus.CREATED);
    }

    @GetMapping("/")
    public List<Note> getAllNotes(@RequestHeader("Authorization") String token) {
        return noteService.getAllNotes(token);
    }

    @DeleteMapping("/delete/{noteId}")
    public ResponseEntity deleteNoteById(@PathVariable Long noteId, @RequestHeader("Authorization") String token) {
        return noteService.deleteNoteById(noteId, token);
    }

    @PutMapping("/update/{noteId}")
    public ResponseEntity updateNote(@PathVariable Long noteId, @RequestBody NoteDTO noteDTO, @RequestHeader("Authorization") String token) {

        return noteService.updateNote(noteId, noteDTO, token);
    }

}
