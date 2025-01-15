package net.dixton.springapi.services;

import net.dixton.dtos.redis.SendPlayerToServerDto;
import net.dixton.enums.ServerStatus;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.account.AccountAlreadyConnectedToTheServerException;
import net.dixton.exceptions.server.ServerNotOnlineException;
import net.dixton.model.account.Account;
import net.dixton.model.server.ServerBukkit;
import net.dixton.services.RedisService;
import org.springframework.stereotype.Service;

@Service
public class AccountServerService {

    public void sendToServer(Account account, ServerBukkit serverBukkit) {
        if (serverBukkit.getStatus() != ServerStatus.ONLINE) throw new DixtonRuntimeException(new ServerNotOnlineException());

        if (account.getServerBukkit().getId().equals(serverBukkit.getId())) throw new DixtonRuntimeException(new AccountAlreadyConnectedToTheServerException(account.getId(), serverBukkit.getId()));

        try {
            RedisService.getInstance().publish(account.getServerProxy().getName(), new SendPlayerToServerDto(account.getUniqueId(), serverBukkit.getId()));
        } catch (Exception ignored) {}
    }
}
