package net.dixton.springapi.controllers;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.platform.AccountLinkDto;
import net.dixton.dtos.platform.AccountUnlinkDto;
import net.dixton.model.server.Server;
import net.dixton.springapi.services.AccountLinkServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts/link")
@RequiredArgsConstructor
public class AccountLinkController {

    private final AccountLinkServiceImpl accountLinkService;

    @PutMapping()
    public ResponseEntity<Server> linkAccount(@RequestBody AccountLinkDto accountLinkDto) {
        this.accountLinkService.linkAccount(accountLinkDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/unlink")
    public ResponseEntity<Server> unlinkAccount(@RequestBody AccountUnlinkDto accountUnlinkDto) {
        this.accountLinkService.unlinkAccount(accountUnlinkDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
