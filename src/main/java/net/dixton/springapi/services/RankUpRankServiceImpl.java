package net.dixton.springapi.services;

import lombok.RequiredArgsConstructor;
import net.dixton.enums.ExceptionError;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.NotFoundException;
import net.dixton.model.server.types.rankup.ServerRankUp;
import net.dixton.model.server.types.rankup.utils.RankUpRank;
import net.dixton.services.RankUpRankService;
import net.dixton.springapi.repositories.player.RankUpRankRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RankUpRankServiceImpl implements RankUpRankService {

    private final Map<Object, RankUpRank> rankUpRankCache = new ConcurrentHashMap<>();

    private final RankUpRankRepository rankUpRankRepository;

    @Override
    public List<RankUpRank> findAllByServerRankUp(ServerRankUp serverRankUp) {
        return this.rankUpRankRepository.findAllByServerRankUp(serverRankUp);
    }

    @Override
    public RankUpRank findById(Long id) {
        if (rankUpRankCache.containsKey(id)) {
            return rankUpRankCache.get(id);
        }

        RankUpRank rankUpRank = this.rankUpRankRepository.findById(id)
                .orElseThrow(() -> new DixtonRuntimeException(new NotFoundException(ExceptionError.NOT_FOUND, "RankUpRank with Id " + id + " not found")));

        this.setCache(rankUpRank);
        return rankUpRank;
    }

    private void setCache(RankUpRank rankUpRank) {
        this.rankUpRankCache.put(rankUpRank.getId(), rankUpRank);
    }
}
