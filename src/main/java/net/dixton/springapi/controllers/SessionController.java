package net.dixton.springapi.controllers;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.CreateSessionDto;
import net.dixton.model.session.Session;
import net.dixton.springapi.services.SessionServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionServiceImpl sessionService;

    @PostMapping
    public ResponseEntity<Session> create(@RequestBody CreateSessionDto createSessionDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(createSessionDto));
    }

    @PutMapping("/{sessionId}/close")
    public ResponseEntity<Session> close(@PathVariable Long sessionId) {
        sessionService.close(sessionId);
        return ResponseEntity.noContent().build();
    }
}