package net.dixton.springapi.repositories.player;

import net.dixton.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface PlayerRepository<T extends Player> extends JpaRepository<T, Long> {
    Optional<T> findByAccountId(Long accountId);
}