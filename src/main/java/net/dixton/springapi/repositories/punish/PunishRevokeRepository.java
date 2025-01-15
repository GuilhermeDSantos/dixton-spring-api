package net.dixton.springapi.repositories.punish;

import net.dixton.model.punish.PunishRevoke;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PunishRevokeRepository extends JpaRepository<PunishRevoke, Long> { }