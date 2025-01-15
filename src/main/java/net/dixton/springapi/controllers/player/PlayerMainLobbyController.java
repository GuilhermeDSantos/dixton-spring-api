package net.dixton.springapi.controllers.player;

import net.dixton.model.player.types.lobby.PlayerMainLobby;
import net.dixton.springapi.controllers.AbstractPlayerController;
import net.dixton.springapi.services.player.PlayerMainLobbyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players/main-lobby")
public class PlayerMainLobbyController extends AbstractPlayerController<PlayerMainLobby> {

    public PlayerMainLobbyController(PlayerMainLobbyService playerMainLobbyService) {
        super(playerMainLobbyService);
    }
}