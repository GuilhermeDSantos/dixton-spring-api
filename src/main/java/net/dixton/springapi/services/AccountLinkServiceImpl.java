package net.dixton.springapi.services;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.platform.AccountLinkCodeDto;
import net.dixton.dtos.platform.AccountLinkDto;
import net.dixton.dtos.platform.AccountUnlinkDto;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.platform.AccountAlreadyLinkedException;
import net.dixton.exceptions.platform.AccountLinkCodeExpiredException;
import net.dixton.exceptions.platform.AccountNotLinkedException;
import net.dixton.exceptions.platform.InvalidAccountLinkException;
import net.dixton.model.account.Account;
import net.dixton.model.account.AccountLink;
import net.dixton.services.AccountLinkService;
import net.dixton.springapi.events.AccountLinkedEvent;
import net.dixton.springapi.events.AccountUnlinkedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AccountLinkServiceImpl implements AccountLinkService {

    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, AccountLink> accountLinkCache = new ConcurrentHashMap<>();
    private final AccountServiceImpl accountService;

    @Override
    public AccountLinkCodeDto generateLinkCode(AccountLink accountLink) {
        SecureRandom random = new SecureRandom();
        String code = String.valueOf(100000 + random.nextInt(900000));

        accountLinkCache.put(code, accountLink);

        return new AccountLinkCodeDto(code);
    }

    @Override
    public void linkAccount(AccountLinkDto accountLinkDto) {
        if (!accountLinkCache.containsKey(accountLinkDto.code())) {
            throw new DixtonRuntimeException(new InvalidAccountLinkException());
        }

        AccountLink accountLink = accountLinkCache.get(accountLinkDto.code());

        if (accountLink.getPlatform() != accountLinkDto.platform()) {
            throw new DixtonRuntimeException(new InvalidAccountLinkException());
        }

        if (accountLink.isExpired()) {
            this.accountLinkCache.remove(accountLinkDto.code());
            throw new DixtonRuntimeException(new AccountLinkCodeExpiredException());
        }

        Account account = this.accountService.findById(accountLinkDto.accountId());

        if (account.getLinkAccounts().containsKey(accountLinkDto.platform())) {
            throw new DixtonRuntimeException(new AccountAlreadyLinkedException(account.getId(), accountLinkDto.platform()));
        }

        accountLink.setAccount(account);
        account.getLinkAccounts().put(accountLinkDto.platform(), accountLink);

        this.accountService.update(account.getId(), account);
        this.accountLinkCache.remove(accountLinkDto.code());

        this.accountService.refreshAccount(account);

        eventPublisher.publishEvent(new AccountLinkedEvent(this, accountLink));
    }

    @Override
    public void unlinkAccount(AccountUnlinkDto accountUnlinkDto) {
        Account account = this.accountService.findById(accountUnlinkDto.accountId());

        if (!account.getLinkAccounts().containsKey(accountUnlinkDto.platform())) {
            throw new DixtonRuntimeException(new AccountNotLinkedException(account.getId(), accountUnlinkDto.platform()));
        }
        AccountLink accountLink = account.getLinkAccounts().get(accountUnlinkDto.platform());

        eventPublisher.publishEvent(new AccountUnlinkedEvent(this, accountLink));

        account.getLinkAccounts().remove(accountUnlinkDto.platform());
        this.accountService.update(account.getId(), account);

        this.accountService.refreshAccount(account);
    }
}
