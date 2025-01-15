package net.dixton.springapi.controllers;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.CreateAccountDto;
import net.dixton.model.account.Account;
import net.dixton.springapi.services.AccountServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountServiceImpl accountService;

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody CreateAccountDto createAccountDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(createAccountDto));
    }

    @GetMapping("/uniqueId/{uniqueId}")
    public ResponseEntity<Account> findByUniqueId(@PathVariable UUID uniqueId) {
        Account account = accountService.findByUniqueId(uniqueId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> findById(@PathVariable Long id) {
        Account account = accountService.findById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/nick/{nick}")
    public ResponseEntity<Account> findByNick(@PathVariable String nick) {
        Account account = accountService.findByNick(nick);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable Long id, @RequestBody Account updatedAccount) {
        Account account = accountService.update(id, updatedAccount);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{id}/send/{serverId}")
    public ResponseEntity<Account> update(@PathVariable Long id, @PathVariable Long serverId) {
        this.accountService.sendToServer(id, serverId);
        return ResponseEntity.noContent().build();
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
//        accountService.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
}
