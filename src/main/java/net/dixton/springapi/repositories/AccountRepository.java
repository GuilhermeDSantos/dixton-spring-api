package net.dixton.springapi.repositories;

import net.dixton.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUniqueId(UUID uniqueId);
    Optional<Account> findByNick(String nick);

}