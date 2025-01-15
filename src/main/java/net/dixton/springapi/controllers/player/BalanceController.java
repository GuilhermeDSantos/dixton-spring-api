package net.dixton.springapi.controllers.player;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.player.balance.*;
import net.dixton.enums.BalanceCurrency;
import net.dixton.enums.BalanceType;
import net.dixton.model.player.Balance;
import net.dixton.springapi.services.player.BalanceServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceServiceImpl balanceService;

    @PostMapping("/deposit")
    public ResponseEntity<Balance> deposit(@RequestBody BalanceDepositDto balanceDepositDto) {
        return ResponseEntity.ok(balanceService.deposit(balanceDepositDto));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Balance> withdraw(@RequestBody BalanceWithdrawDto balanceWithdrawDto) {
        return ResponseEntity.ok(balanceService.withdraw(balanceWithdrawDto));
    }

    @PostMapping("/transfer")
    public ResponseEntity<BalanceTransferResultDto> transfer(@RequestBody BalanceTransferDto balanceTransferDto) {
        return ResponseEntity.ok(balanceService.transfer(balanceTransferDto));
    }

    @GetMapping("/top/{positions}/{currency}/{type}")
    public ResponseEntity<List<BalanceTopResultDto>> findTop(@PathVariable int positions,
                                                         @PathVariable BalanceCurrency currency,
                                                         @PathVariable BalanceType type) {
        return ResponseEntity.ok(balanceService.findTop(positions, currency, type));
    }
}