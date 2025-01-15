package net.dixton.springapi.services;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.CreateAccountDto;
import net.dixton.dtos.redis.RefreshAccountDto;
import net.dixton.enums.BalanceCurrency;
import net.dixton.enums.BalanceType;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.account.AccountNotFoundException;
import net.dixton.exceptions.server.ServerMustBeBukkitException;
import net.dixton.model.account.Account;
import net.dixton.model.account.AccountCracked;
import net.dixton.model.account.AccountPremium;
import net.dixton.model.player.Balance;
import net.dixton.model.server.Server;
import net.dixton.model.server.ServerBukkit;
import net.dixton.model.skin.Skin;
import net.dixton.services.AccountService;
import net.dixton.services.RedisService;
import net.dixton.springapi.repositories.AccountRepository;
import net.dixton.springapi.services.player.BalanceServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final Map<Object, Account> accountCache = new ConcurrentHashMap<>();

    private final AccountRepository accountRepository;
    private final ServerServiceImpl serverService;
    private final SkinServiceImpl skinService;
    private final BalanceServiceImpl balanceService;
    private final AccountServerService accountServerService;

    private Account save(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account create(CreateAccountDto createAccountDto) {
        Account account = null;

        switch (createAccountDto.accountType()) {
            case PREMIUM -> account = new AccountPremium(createAccountDto.uniqueId(), createAccountDto.nick(), createAccountDto.premiumSkinData());
            case CRACKED -> {
                Skin skin = this.skinService.findRandomSkin();

                account = new AccountCracked(createAccountDto.uniqueId(), createAccountDto.nick(), skin);
            }
        }
        assert account != null;

        account.addBalance(new Balance(BalanceCurrency.CASH, BalanceType.GLOBAL));
        account = this.accountRepository.save(account);

        this.setCache(account);
        return account;
    }

    @Override
    public Account findById(Long id) {
        if (accountCache.containsKey(id)) {
            return accountCache.get(id);
        }

        Account account = this.accountRepository.findById(id)
                .orElseThrow(() -> new DixtonRuntimeException(new AccountNotFoundException(id)));

        this.setCache(account);

        return account;
    }

    @Override
    public Account findByUniqueId(UUID uniqueId) {
        if (accountCache.containsKey(uniqueId)) {
            return accountCache.get(uniqueId);
        }

        Account account = this.accountRepository.findByUniqueId(uniqueId)
                .orElseThrow(() -> new DixtonRuntimeException(new AccountNotFoundException(uniqueId)));
        this.setCache(account);

        return account;
    }

    @Override
    public Account findByNick(String nick) {
        if (accountCache.containsKey(nick.toLowerCase())) {
            return accountCache.get(nick.toLowerCase());
        }

        Account account = this.accountRepository.findByNick(nick)
                .orElseThrow(() -> new DixtonRuntimeException(new AccountNotFoundException(nick)));
        this.setCache(account);

        return account;
    }

    @Transactional
    public Account update(Long id, Account updatedAccount) {
        Account existingAccount = this.findById(id);

        System.out.println(existingAccount instanceof AccountPremium);
        System.out.println(updatedAccount instanceof AccountPremium);

        existingAccount.updateData(updatedAccount);

        return this.save(existingAccount);
    }

    @Override
    public void sendToServer(Long id, Long serverId) {
        Account account = this.findById(id);
        Server server = this.serverService.findById(serverId);

        if (!server.isBukkit()) throw new DixtonRuntimeException(new ServerMustBeBukkitException());

        this.accountServerService.sendToServer(account, (ServerBukkit) server);
    }

    private void setCache(Account account) {
        this.accountCache.put(account.getId(), account);
        this.accountCache.put(account.getNick().toLowerCase(), account);
        this.accountCache.put(account.getUniqueId(), account);

        for (Balance balance : account.getBalances()) {
            balanceService.registerBalance(balance);
        }
    }

    public void clearCache(Account account) {
        this.accountCache.remove(account.getId());
        this.accountCache.remove(account.getNick().toLowerCase());
        this.accountCache.remove(account.getUniqueId());
    }

    /* Utils */

    public void refreshAccount(Account account) {
        /* Force Role update */
        account.setRole(null);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }

            try {
                RedisService.getInstance().publish(account.getServerProxy().getName(), new RefreshAccountDto(account.getUniqueId()));
                RedisService.getInstance().publish(account.getServerBukkit().getName(), new RefreshAccountDto(account.getUniqueId()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
