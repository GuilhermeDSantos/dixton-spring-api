package net.dixton.springapi.listeners;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.player.balance.BalanceDepositDto;
import net.dixton.dtos.player.balance.BalanceTransactionDto;
import net.dixton.dtos.player.balance.BalanceUpdateDto;
import net.dixton.events.RedisMessageEvent;
import net.dixton.interfaces.Observer;
import net.dixton.model.player.Balance;
import net.dixton.services.RedisService;
import net.dixton.springapi.services.player.BalanceServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisListener implements Observer<RedisMessageEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RedisListener.class);
    private final BalanceServiceImpl balanceService;

    @Override
    public void accept(RedisMessageEvent event) {
        logger.info("New Redis Message Received: {}", event.getMessage());

        try {
            switch (event.getMessage()) {
                case BalanceTransactionDto balanceTransactionDto -> {
                    /* Call service */
                    Balance balance = null;
                    switch (balanceTransactionDto.type()) {
                        case DEPOSIT -> balance = this.balanceService.deposit(new BalanceDepositDto(balanceTransactionDto.balanceId(), balanceTransactionDto.amount()));
                        case WITHDRAW -> {}
                    }

                    /* Reply */
                    assert balance != null;
                    RedisService.getInstance().publish(balanceTransactionDto.fromServer(), new BalanceUpdateDto(balanceTransactionDto.balanceId(), balance.getCurrency(), balance.getAmount()));
                }

                default -> logger.warn("Received unknown message {}", event.getMessage());
            }
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        }
    }
}
