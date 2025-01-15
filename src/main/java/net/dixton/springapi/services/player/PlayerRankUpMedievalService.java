package net.dixton.springapi.services.player;

import net.dixton.enums.BalanceCurrency;
import net.dixton.enums.BalanceType;
import net.dixton.model.account.Account;
import net.dixton.model.player.Balance;
import net.dixton.model.player.types.rankup.PlayerRankUpMedieval;
import net.dixton.springapi.repositories.player.PlayerRepository;
import net.dixton.springapi.services.AbstractPlayerService;
import net.dixton.springapi.services.AccountServiceImpl;
import net.dixton.springapi.services.RankUpRankServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PlayerRankUpMedievalService extends AbstractPlayerService<PlayerRankUpMedieval> {

    private final RankUpRankServiceImpl rankUpRankService;

    public PlayerRankUpMedievalService(AccountServiceImpl accountService, BalanceServiceImpl balanceService, PlayerRepository<PlayerRankUpMedieval> playerRepository, RankUpRankServiceImpl rankUpRankService) {
        super(accountService, balanceService, playerRepository);
        this.rankUpRankService = rankUpRankService;
    }

    @Override
    protected PlayerRankUpMedieval createInstance(Account account) {
        PlayerRankUpMedieval playerRankUpMedieval = new PlayerRankUpMedieval(account, this.rankUpRankService.findById(1L));
        playerRankUpMedieval.addBalance(new Balance(BalanceCurrency.MONEY, BalanceType.PLAYER_RANK_UP_MEDIEVAL));
        playerRankUpMedieval.addBalance(new Balance(BalanceCurrency.SOULS, BalanceType.PLAYER_RANK_UP_MEDIEVAL));

        return playerRankUpMedieval;
    }
}