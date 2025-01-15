package net.dixton.springapi.services;

import lombok.RequiredArgsConstructor;
import net.dixton.dtos.CreateSessionDto;
import net.dixton.dtos.redis.ServerUpdateDto;
import net.dixton.enums.ServerStatus;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.server.ServerNotOnlineException;
import net.dixton.exceptions.session.MasterSessionNotFoundException;
import net.dixton.exceptions.session.SessionIpMismatchException;
import net.dixton.model.account.Account;
import net.dixton.model.server.Server;
import net.dixton.model.server.ServerBukkit;
import net.dixton.model.server.ServerProxy;
import net.dixton.model.session.MasterSession;
import net.dixton.model.session.Session;
import net.dixton.model.session.SubSession;
import net.dixton.services.RedisService;
import net.dixton.services.SessionService;
import net.dixton.springapi.repositories.SessionRepository;
import net.dixton.springapi.services.player.PlayerMainLobbyService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final ServerServiceImpl serverService;
    private final AccountServiceImpl accountService;

    private final PlayerMainLobbyService playerMainLobbyService;

    private final Map<Long, MasterSession> sessionMap = new HashMap<>();

    private Session save(Session session) {
        return sessionRepository.save(session);
    }

    @Override
    public Session create(CreateSessionDto createSessionDto) {
        Server server = this.serverService.findById(createSessionDto.serverId());
        Account account = this.accountService.findById(createSessionDto.accountId());
        Session session = null;

        switch (server.getKind()) {
            case PROXY -> {
                session = new MasterSession(
                        account,
                        server,
                        createSessionDto.playerIp(), createSessionDto.hostname()
                );

                account.setSession(session);
                sessionMap.put(createSessionDto.accountId(), (MasterSession) session);

                session.getAccount().setServerProxy((ServerProxy) server);
            }
            case BUKKIT -> {
                Optional<MasterSession> masterSession = this.getMasterSession(createSessionDto.accountId());

                if (masterSession.isEmpty()) {
                    throw new DixtonRuntimeException(new MasterSessionNotFoundException(account.getId()));
                }

                if (!masterSession.get().getPlayerIp().equalsIgnoreCase(createSessionDto.playerIp())
                        || !masterSession.get().getHostname().equalsIgnoreCase(createSessionDto.hostname())) {
                    throw new DixtonRuntimeException(new SessionIpMismatchException());
                }

                if (server.getStatus() != ServerStatus.ONLINE) {
                    throw new DixtonRuntimeException(new ServerNotOnlineException());
                }

                session = new SubSession(
                        account,
                        server,
                        createSessionDto.playerIp(), createSessionDto.hostname().toLowerCase(),
                        masterSession.get()
                );

                session.getAccount().setServerBukkit((ServerBukkit) server);
            }
        }

        server.addConnectedAccount(account);

        try {
            RedisService.getInstance().publish(server.getName(),
                    new ServerUpdateDto(server.getId(), server.getStatus(), server.getConnectedPlayers()));

            System.out.println("Server: " + server.getName() + ", Connected: " + server.getConnectedPlayers());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Save Session
        return this.save(session);
    }

    @Override
    public void close(Long sessionId) {
        Optional<Session> sessionOptional = this.sessionRepository.findById(sessionId);

        if (sessionOptional.isPresent()) {
            Session session = sessionOptional.get();
            Server server = this.serverService.findById(session.getServer().getId());

            if (session instanceof MasterSession) {
                this.sessionMap.remove(session.getAccount().getId());

                this.accountService.clearCache(session.getAccount());
                this.playerMainLobbyService.clearCache(session.getAccount().getId());
            }

            // Update Server
            switch (server.getKind()) {
                case PROXY -> session.getAccount().setServerProxy(null);
                case BUKKIT -> session.getAccount().setServerBukkit(null);
            }
            server.removeConnectedAccount(session.getAccount().getId());

            try {
                RedisService.getInstance().publish(server.getName(),
                        new ServerUpdateDto(server.getId(), server.getStatus(), server.getConnectedPlayers()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Save Session
            session.setClosedAt(LocalDateTime.now());
            this.save(session);
        }
    }

    private Optional<MasterSession> getMasterSession(Long accountId) {
        return Optional.ofNullable(this.sessionMap.get(accountId));
    }
}
