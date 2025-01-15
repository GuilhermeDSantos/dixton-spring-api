package net.dixton.springapi.controllers;

import lombok.RequiredArgsConstructor;
import net.dixton.model.player.Player;
import net.dixton.springapi.services.AbstractPlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
public abstract class AbstractPlayerController<T extends Player> {

    private final AbstractPlayerService<T> playerService;

    @PostMapping("/{accountId}")
    public ResponseEntity<T> create(@PathVariable Long accountId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.create(accountId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<T> findByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(playerService.findByAccount(accountId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable Long id, @RequestBody T updatedPlayer) {
        return ResponseEntity.ok(playerService.update(id, updatedPlayer));
    }
}