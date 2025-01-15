package net.dixton.springapi.repositories.player;

import net.dixton.model.server.types.rankup.ServerRankUp;
import net.dixton.model.server.types.rankup.utils.RankUpRank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankUpRankRepository extends JpaRepository<RankUpRank, Long> {

    List<RankUpRank> findAllByServerRankUp(ServerRankUp serverRankUp);
}