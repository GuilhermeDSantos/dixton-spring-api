package net.dixton.springapi.repositories;

import net.dixton.model.server.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Long> {

    Optional<Server> findByPort(Integer port);
    Optional<Server> findByName(String name);
}