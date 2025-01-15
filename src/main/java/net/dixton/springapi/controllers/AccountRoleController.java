package net.dixton.springapi.controllers;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.role.GiveRoleDto;
import net.dixton.dtos.role.RoleGivenDto;
import net.dixton.dtos.role.RoleListDto;
import net.dixton.dtos.role.RoleRemovedDto;
import net.dixton.enums.Role;
import net.dixton.springapi.services.AccountRoleServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts/{playerNick}/roles")
@RequiredArgsConstructor
public class AccountRoleController {

    private final AccountRoleServiceImpl accountRoleService;

    @PostMapping("/assign")
    public ResponseEntity<RoleGivenDto> assignRole(@PathVariable String playerNick, @RequestBody GiveRoleDto giveRoleDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountRoleService.assignRole(playerNick, giveRoleDto));
    }

    @PostMapping("/revoke")
    public ResponseEntity<RoleRemovedDto> revokeRole(@PathVariable String playerNick, @RequestBody Role role) {
        return ResponseEntity.ok(accountRoleService.revokeRole(playerNick, role));
    }

    @GetMapping
    public ResponseEntity<RoleListDto> listRoles(@PathVariable String playerNick) {
        return ResponseEntity.ok(accountRoleService.listRoles(playerNick));
    }
}