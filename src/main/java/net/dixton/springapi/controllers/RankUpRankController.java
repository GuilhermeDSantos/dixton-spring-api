package net.dixton.springapi.controllers;

import lombok.RequiredArgsConstructor;
import net.dixton.model.server.types.rankup.ServerRankUp;
import net.dixton.model.server.types.rankup.utils.RankUpRank;
import net.dixton.springapi.services.RankUpRankServiceImpl;
import net.dixton.springapi.services.ServerServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servers/rankup/ranks")
@RequiredArgsConstructor
public class RankUpRankController {

    private final ServerServiceImpl serverService;
    private final RankUpRankServiceImpl rankUpRankService;

    @GetMapping("/{serverId}")
    public ResponseEntity<List<RankUpRank>> findAllByServerRankUp(@PathVariable Long serverId) {
        return ResponseEntity.ok(this.rankUpRankService.findAllByServerRankUp((ServerRankUp) serverService.findById(serverId)));
    }
}
