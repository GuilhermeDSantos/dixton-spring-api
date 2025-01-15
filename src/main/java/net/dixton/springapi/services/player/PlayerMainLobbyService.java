package net.dixton.springapi.services.player;

import net.dixton.model.account.Account;
import net.dixton.model.player.types.lobby.PlayerMainLobby;
import net.dixton.springapi.repositories.player.PlayerRepository;
import net.dixton.springapi.services.AbstractPlayerService;
import net.dixton.springapi.services.AccountServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PlayerMainLobbyService extends AbstractPlayerService<PlayerMainLobby> {

    public PlayerMainLobbyService(AccountServiceImpl accountService, BalanceServiceImpl balanceService, PlayerRepository<PlayerMainLobby> playerRepository) {
        super(accountService, balanceService, playerRepository);
    }

    @Override
    protected PlayerMainLobby createInstance(Account account) {
        return new PlayerMainLobby(account, false);
    }
}