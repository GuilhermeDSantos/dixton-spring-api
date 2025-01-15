package net.dixton.springapi.repositories;

import net.dixton.model.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}