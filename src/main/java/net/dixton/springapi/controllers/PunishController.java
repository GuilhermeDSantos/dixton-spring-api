package net.dixton.springapi.controllers;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.punish.RegisterPunishmentDto;
import net.dixton.dtos.punish.RevokePunishmentDto;
import net.dixton.model.account.AccountPunish;
import net.dixton.model.punish.PunishReason;
import net.dixton.model.punish.PunishRevoke;
import net.dixton.springapi.services.PunishServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/punish")
@RequiredArgsConstructor
public class PunishController {

    private final PunishServiceImpl punishService;

    @GetMapping("/reason")
    public ResponseEntity<List<PunishReason>> findAllPunishReason() {
        return ResponseEntity.ok(punishService.findAllPunishReason());
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<AccountPunish>> findAllByAccountId(@PathVariable Long id) {
        return ResponseEntity.ok(punishService.findAllByAccountId(id));
    }

    @PostMapping
    public ResponseEntity<AccountPunish> punish(@RequestBody RegisterPunishmentDto registerPunishmentDto) {
        return ResponseEntity.ok(punishService.punish(registerPunishmentDto));
    }

    @PostMapping("/revoke")
    public ResponseEntity<PunishRevoke> revoke(@RequestBody RevokePunishmentDto revokePunishmentDto) {
        return ResponseEntity.ok(punishService.revoke(revokePunishmentDto));
    }
}
