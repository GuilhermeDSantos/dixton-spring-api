package net.dixton.springapi.controllers;

import lombok.RequiredArgsConstructor;
import net.dixton.enums.ServerKind;
import net.dixton.enums.ServerType;
import net.dixton.model.server.Server;
import net.dixton.springapi.services.ServerServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerServiceImpl serverService;

    @GetMapping
    public ResponseEntity<List<Server>> findAll() {
        List<Server> servers = serverService.findAll();
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Server> findById(@PathVariable Long id) {
        Server server = serverService.findById(id);
        return ResponseEntity.ok(server);
    }

    @GetMapping(value = "/port/{port}")
    public ResponseEntity<Server> findByPort(@PathVariable Integer port) {
        Server server = serverService.findByPort(port);
        return ResponseEntity.ok(server);
    }

    @GetMapping(value = "/name/{name}")
    public ResponseEntity<Server> findByName(@PathVariable String name) {
        return ResponseEntity.ok(serverService.findByName(name));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Server>> findByType(@PathVariable ServerType type) {
        return ResponseEntity.ok(serverService.findByType(type));
    }

    @GetMapping("/kind/{kind}")
    public ResponseEntity<List<Server>> findByType(@PathVariable ServerKind kind) {
        return ResponseEntity.ok(serverService.findByKind(kind));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Server> update(@PathVariable Long id, @RequestBody Server updatedServer) {
        return ResponseEntity.ok(serverService.update(id, updatedServer));
    }

    @PutMapping("/{id}/restart")
    public ResponseEntity<Void> restart(@PathVariable Long id) {
        serverService.restart(id);
        return ResponseEntity.noContent().build();
    }
}
