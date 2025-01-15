package net.dixton.springapi.services;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.role.*;
import net.dixton.enums.Role;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.role.AccountRoleAlreadyLifetimeException;
import net.dixton.exceptions.role.AccountRoleNotFoundException;
import net.dixton.model.account.Account;
import net.dixton.model.account.AccountRole;
import net.dixton.services.AccountRoleService;
import net.dixton.springapi.repositories.AccountRoleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountRoleServiceImpl implements AccountRoleService {

    private final AccountRoleRepository accountRoleRepository;
    private final AccountServiceImpl accountService;

    @Override
    public RoleGivenDto assignRole(String playerNick, GiveRoleDto giveRoleDto) {
        Account account = this.accountService.findByNick(playerNick);

        AccountRole accountRole = account.getRoles().stream()
                .filter(role -> role.getRole() == giveRoleDto.role() && role.isGroupActive())
                .findFirst()
                .orElse(null);

        if (accountRole != null) {
            if (accountRole.isLifetime()) {
                throw new DixtonRuntimeException(new AccountRoleAlreadyLifetimeException(account.getNick(), giveRoleDto.role()));
            }

            accountRole.setExpirationDate(giveRoleDto.duration() == null ? null : accountRole.getExpirationDate().plus(giveRoleDto.duration()));
        } else {
            accountRole = new AccountRole();
            accountRole.setAccount(account);
            accountRole.setRole(giveRoleDto.role());
            if (giveRoleDto.duration() != null) {
                accountRole.setExpirationDate(Instant.now().plus(giveRoleDto.duration()));
            }
            account.getRoles().add(accountRole);
        }
        this.accountRoleRepository.save(accountRole);
        this.accountService.refreshAccount(account);

        return new RoleGivenDto(account.getNick(), giveRoleDto.role(), accountRole.getExpirationDate());
    }

    @Override
    public RoleRemovedDto revokeRole(String playerNick, Role role) {
        System.out.println("revokeRole: " + playerNick + ", " + role);
        Account account = this.accountService.findByNick(playerNick);

        System.out.println("account: " + account.getId());

        AccountRole roleToRemove = account.getRoles().stream()
                .filter(accountRole -> accountRole.getRole() == role && accountRole.isGroupActive())
                .findFirst()
                .orElseThrow(() -> new DixtonRuntimeException(
                        new AccountRoleNotFoundException(account.getNick(), role)
                ));

        roleToRemove.setExpirationDate(Instant.now());
        this.accountRoleRepository.save(roleToRemove);
        this.accountService.refreshAccount(account);

        return new RoleRemovedDto(account.getNick(), role);
    }

    @Override
    public RoleListDto listRoles(String playerNick) {
        Account account = this.accountService.findByNick(playerNick);

        List<AccountRoleDto> roles = account.getRoles().stream()
                .map(role -> new AccountRoleDto(role.getRole(), role.getExpirationDate(), role.isGroupActive()))
                .toList();

        return new RoleListDto(account.getNick(), roles);
    }
}
