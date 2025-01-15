package net.dixton.springapi.repositories.punish;

import net.dixton.model.account.Account;
import net.dixton.model.account.AccountPunish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountPunishRepository extends JpaRepository<AccountPunish, Long> {

    List<AccountPunish> findAllByAccount(Account account);

}