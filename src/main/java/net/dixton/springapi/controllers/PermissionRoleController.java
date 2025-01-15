package net.dixton.springapi.controllers;

import lombok.RequiredArgsConstructor;
import net.dixton.enums.Role;
import net.dixton.model.permission.PermissionRole;
import net.dixton.springapi.services.PermissionRoleServiceImpl;
import net.dixton.springapi.services.ServerServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permission/roles")
@RequiredArgsConstructor
public class PermissionRoleController {

    private final ServerServiceImpl serverService;
    private final PermissionRoleServiceImpl permissionRoleService;

    @GetMapping("/{role}/{serverId}")
    public ResponseEntity<List<PermissionRole>> findAllByRoleAndServer(@PathVariable Role role, @PathVariable Long serverId) {
        return ResponseEntity.ok(permissionRoleService.findAllByRoleAndServer(role, this.serverService.findById(serverId)));
    }
}