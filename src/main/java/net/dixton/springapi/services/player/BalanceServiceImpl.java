package net.dixton.springapi.services.player;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.player.balance.*;
import net.dixton.enums.BalanceCurrency;
import net.dixton.enums.BalanceType;
import net.dixton.enums.Role;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.player.balance.BalanceNotFoundException;
import net.dixton.exceptions.player.balance.InsufficientBalanceException;
import net.dixton.exceptions.player.balance.InvalidAmountBalanceException;
import net.dixton.model.player.Balance;
import net.dixton.model.player.Player;
import net.dixton.model.skin.SkinData;
import net.dixton.services.player.BalanceService;
import net.dixton.springapi.repositories.player.BalanceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository balanceRepository;

    private final Map<Object, Balance> balanceCache = new ConcurrentHashMap<>();

    public Balance findById(Long id) {
        if (balanceCache.containsKey(id)) {
            return balanceCache.get(id);
        }

        return this.balanceRepository.findById(id)
                .orElseThrow(() -> new DixtonRuntimeException(new BalanceNotFoundException(id)));
    }

    @Transactional
    public Balance deposit(BalanceDepositDto balanceDepositDto) {
        if (balanceDepositDto.amount() == null || balanceDepositDto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DixtonRuntimeException(new InvalidAmountBalanceException(balanceDepositDto.amount()));
        }

        Balance balance = this.findById(balanceDepositDto.balanceId());
        balance.setAmount(balance.getAmount().add(balanceDepositDto.amount()).setScale(2, RoundingMode.HALF_EVEN));
        this.balanceRepository.save(balance);

        return balance;
    }

    @Transactional
    public Balance withdraw(BalanceWithdrawDto balanceWithdrawDto) {
        if (balanceWithdrawDto.amount() == null || balanceWithdrawDto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DixtonRuntimeException(new InvalidAmountBalanceException(balanceWithdrawDto.amount()));
        }

        Balance balance = this.findById(balanceWithdrawDto.balanceId());

        if (balance.getAmount().compareTo(balanceWithdrawDto.amount()) < 0) {
            throw new DixtonRuntimeException(new InsufficientBalanceException(balanceWithdrawDto.amount()));
        }

        balance.setAmount(balance.getAmount().subtract(balanceWithdrawDto.amount()).setScale(2, RoundingMode.HALF_EVEN));
        this.balanceRepository.save(balance);

        return balance;
    }

    @Transactional
    public BalanceTransferResultDto transfer(BalanceTransferDto balanceTransferDto) {
        Balance from = this.withdraw(new BalanceWithdrawDto(balanceTransferDto.fromBalanceId(), balanceTransferDto.amount()));
        Balance to = this.deposit(new BalanceDepositDto(balanceTransferDto.toBalanceId(), balanceTransferDto.amount()));

        return new BalanceTransferResultDto(from, to);
    }

    @Override
    public List<BalanceTopResultDto> findTop(int positions, BalanceCurrency currency, BalanceType type) {
        List<BalanceTopResultDto> result = new ArrayList<>();

        Pageable pageable = PageRequest.of(0, positions, Sort.by(Sort.Direction.DESC, "amount"));
        Page<Balance> pageResult = balanceRepository.findByTypeAndCurrencyOrderByAmountDesc(type, currency, pageable);

        pageResult.getContent().forEach(balance -> {
            String playerNick = "";
            Role playerRole = null;
            SkinData playerSkinData = null;
            if (balance.getHolder() instanceof Player player) {
                playerNick = player.getAccount().getNick();
                playerRole = player.getAccount().getRole();
                playerSkinData = player.getAccount().getSkinData();
            }
            result.add(new BalanceTopResultDto(result.size() + 1, playerNick, playerRole, playerSkinData, balance.getAmount()));
        });

        return result;
    }

    public void registerBalance(Balance balance) {
        this.balanceCache.put(balance.getId(), balance);
    }
}
