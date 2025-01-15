package net.dixton.springapi.repositories;

import net.dixton.enums.Role;
import net.dixton.model.account.Account;
import net.dixton.model.account.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRoleRepository extends JpaRepository<AccountRole, Long> {

    List<AccountRole> findByAccount(Account account);
    List<AccountRole> findByAccountAndRole(Account account, Role role);

}