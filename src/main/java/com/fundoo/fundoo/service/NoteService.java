package com.fundoo.fundoo.service;

import com.fundoo.fundoo.exception.ApiRequestException;
import com.fundoo.fundoo.jwt.JwtUtil;
import com.fundoo.fundoo.model.Note;
import com.fundoo.fundoo.model.User;
import com.fundoo.fundoo.model.payload.request.NoteDTO;
import com.fundoo.fundoo.repository.INoteRepository;
import com.fundoo.fundoo.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NoteService {

    private final INoteRepository noteRepository;
    private final IUserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public NoteService(INoteRepository noteRepository, IUserRepository userRepository, JwtUtil jwtUtil) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public Note createNote(NoteDTO noteDTO, String bearerToken) {

        Optional<User> user = extractUserFromToken(bearerToken);
        // TODO: invalid user
        Note note = new Note();
        note.setTitle(noteDTO.getTitle());
        note.setDescription(noteDTO.getDescription());
        note.setCreatedAt(LocalDateTime.now());
        note.setUser(user.get());
        Note saveNote = noteRepository.save(note);

        if(saveNote == null) {
            throw new ApiRequestException("Error while saving");
        }
        return saveNote;
    }

    public List<Note> getAllNotes(String bearerToken) {
        Optional<User> user = extractUserFromToken(bearerToken);
        //Optional<List<Note>> notes = noteRepository.findNoteById(user.get().getId());
        return user.get().getNotes();
    }

    public ResponseEntity deleteNoteById(Long noteId, String bearerToken) {
        Optional<User> user = extractUserFromToken(bearerToken);

        if(!noteRepository.existsById(noteId)) {
            throw new ApiRequestException("Error in delete");
        }
        noteRepository.deleteById(noteId);
        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity updateNote(Long noteId, NoteDTO noteDTO, String bearerToken) {
        Optional<User> user = extractUserFromToken(bearerToken);

       // Optional<Note> note = noteRepository.findById(noteId);
        if(!noteRepository.existsById(noteId)) {
            throw new ApiRequestException("Error in update");
        }

        int row = noteRepository.updateTitleById(noteDTO.getTitle(), noteDTO.getDescription(), noteId);
        return null;
    }

    private Optional<User> extractUserFromToken(String bearerToken) {
        String jwtToken = bearerToken.substring(7);
        String email = jwtUtil.extractUsername(jwtToken);

        Optional<User> user = userRepository.findByEmail(email);
        if(!user.isPresent()) {
            throw new ApiRequestException("Invalid User");
        }
        return user;
    }
}
