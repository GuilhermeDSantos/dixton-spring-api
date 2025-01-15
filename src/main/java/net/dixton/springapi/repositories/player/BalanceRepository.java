package net.dixton.springapi.repositories.player;

import net.dixton.enums.BalanceCurrency;
import net.dixton.enums.BalanceType;
import net.dixton.model.player.Balance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceRepository extends JpaRepository<Balance, Long> {

    Page<Balance> findByTypeAndCurrencyOrderByAmountDesc(
            BalanceType type,
            BalanceCurrency currency,
            Pageable pageable
    );
}