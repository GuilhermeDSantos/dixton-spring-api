package net.dixton.springapi.controllers.player;

import net.dixton.model.player.types.rankup.PlayerRankUpMedieval;
import net.dixton.springapi.controllers.AbstractPlayerController;
import net.dixton.springapi.services.player.PlayerRankUpMedievalService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players/rank-up-medieval")
public class PlayerRankUpMedievalController extends AbstractPlayerController<PlayerRankUpMedieval> {

    public PlayerRankUpMedievalController(PlayerRankUpMedievalService playerRankUpMedievalService) {
        super(playerRankUpMedievalService);
    }
}