package net.dixton.springapi.repositories.punish;

import net.dixton.model.punish.PunishReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PunishReasonRepository extends JpaRepository<PunishReason, Long> { }