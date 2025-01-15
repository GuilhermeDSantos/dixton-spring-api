package net.dixton.springapi.services;

import lombok.RequiredArgsConstructor;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.player.PlayerNotFoundException;
import net.dixton.model.account.Account;
import net.dixton.model.player.Balance;
import net.dixton.model.player.Player;
import net.dixton.services.PlayerService;
import net.dixton.springapi.repositories.player.PlayerRepository;
import net.dixton.springapi.services.player.BalanceServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public abstract class AbstractPlayerService<T extends Player> implements PlayerService<T> {

    private final Map<Object, T> playerCache = new ConcurrentHashMap<>();

    private final AccountServiceImpl accountService;
    private final BalanceServiceImpl balanceService;
    private final PlayerRepository<T> playerRepository;

    protected abstract T createInstance(Account account);

    @Override
    public T create(Long accountId) {
        Account account = accountService.findById(accountId);
        T player = this.playerRepository.save(createInstance(account));

        this.setCache(accountId, player);
        return player;
    }

    @Override
    public T findByAccount(Long accountId) {
        if (playerCache.containsKey(accountId)) {
            return playerCache.get(accountId);
        }

        T player = this.playerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DixtonRuntimeException(new PlayerNotFoundException(accountId)));
        this.setCache(accountId, player);

        return player;
    }

    @Transactional
    public T update(Long accountId, Player updatedPlayer) {
        System.out.println("Update: " + accountId);

        T existingPlayer = this.findByAccount(accountId);

        existingPlayer.updateData(updatedPlayer);

        T t = null;

        try {
            t = this.playerRepository.save(existingPlayer);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }

        return t;
    }

    private void setCache(Long accountId, T player) {
        this.playerCache.put(accountId, player);

        for (Balance balance : player.getBalances()) {
            balanceService.registerBalance(balance);
        }
    }

    public void clearCache(Long accountId) {
        this.playerCache.remove(accountId);
    }
}