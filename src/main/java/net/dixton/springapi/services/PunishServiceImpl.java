package net.dixton.springapi.services;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.punish.RegisterPunishmentDto;
import net.dixton.dtos.punish.RevokePunishmentDto;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.account.AccountPunishNotFoundException;
import net.dixton.model.account.Account;
import net.dixton.model.account.AccountPunish;
import net.dixton.model.punish.PunishReason;
import net.dixton.model.punish.PunishRevoke;
import net.dixton.services.PunishService;
import net.dixton.springapi.repositories.punish.AccountPunishRepository;
import net.dixton.springapi.repositories.punish.PunishReasonRepository;
import net.dixton.springapi.repositories.punish.PunishRevokeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PunishServiceImpl implements PunishService {

    private final AccountServiceImpl accountService;
    private final PunishReasonRepository punishReasonRepository;
    private final AccountPunishRepository accountPunishRepository;
    private final PunishRevokeRepository punishRevokeRepository;

    @Override
    public List<PunishReason> findAllPunishReason() {
        return punishReasonRepository.findAll();
    }

    @Override
    public List<AccountPunish> findAllByAccountId(Long accountId) {
        Account account = this.accountService.findById(accountId);
        return this.accountPunishRepository.findAllByAccount(account);
    }

    @Override
    public AccountPunish punish(RegisterPunishmentDto registerPunishmentDto) {
        Account targetAccount = this.accountService.findById(registerPunishmentDto.targetAccountId());
        Account punisherAccount = this.accountService.findById(registerPunishmentDto.punisherAccountId());

        Instant expirationDate = null;

        if (registerPunishmentDto.punishReason().getDuration() != null) {
            expirationDate = Instant.now().plusSeconds(registerPunishmentDto.punishReason().getDuration().toSeconds());
        }

        AccountPunish accountPunish = new AccountPunish(targetAccount, punisherAccount, registerPunishmentDto.punishReason(), expirationDate);
        targetAccount.getPunishments().add(accountPunish);
        this.accountService.refreshAccount(targetAccount);

        return this.accountPunishRepository.save(accountPunish);
    }

    @Override
    public PunishRevoke revoke(RevokePunishmentDto revokePunishmentDto) {
        Optional<AccountPunish> optionalAccountPunish = this.accountPunishRepository.findById(revokePunishmentDto.punishmentId());

        if (optionalAccountPunish.isEmpty()) {
            throw new DixtonRuntimeException(new AccountPunishNotFoundException(revokePunishmentDto.punishmentId()));
        }
        Account revokerAccount = this.accountService.findById(revokePunishmentDto.revokerAccountId());
        AccountPunish accountPunish = optionalAccountPunish.get();

        PunishRevoke punishRevoke = new PunishRevoke(revokePunishmentDto.revokeType(), revokerAccount);
        this.punishRevokeRepository.save(punishRevoke);

        accountPunish.setRevoke(punishRevoke);

        accountPunish = this.accountPunishRepository.save(accountPunish);

        return accountPunish.getRevoke();
    }
}