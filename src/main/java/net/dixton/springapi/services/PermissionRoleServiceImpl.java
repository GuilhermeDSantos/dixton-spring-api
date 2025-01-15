package net.dixton.springapi.services;

import lombok.RequiredArgsConstructor;
import net.dixton.enums.Role;
import net.dixton.model.permission.PermissionRole;
import net.dixton.model.server.Server;
import net.dixton.services.PermissionRoleService;
import net.dixton.springapi.repositories.PermissionRoleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionRoleServiceImpl implements PermissionRoleService {

    private final PermissionRoleRepository permissionRoleRepository;

    @Override
    public List<PermissionRole> findAllByRoleAndServer(Role role, Server server) {
        return this.permissionRoleRepository.findAllByRoleAndServer(role, server, Instant.now());
    }
}
