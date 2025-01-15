package net.dixton.springapi.repositories;

import net.dixton.enums.Role;
import net.dixton.model.permission.PermissionRole;
import net.dixton.model.server.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PermissionRoleRepository extends JpaRepository<PermissionRole, Long> {

    @Query("SELECT pr FROM PermissionRole pr WHERE pr.role = :role AND (pr.permission.server = :server OR pr.permission.server IS NULL) AND (pr.permission.expirationDate IS NULL OR pr.permission.expirationDate > :now)")
    List<PermissionRole> findAllByRoleAndServer(Role role, Server server, Instant now);}