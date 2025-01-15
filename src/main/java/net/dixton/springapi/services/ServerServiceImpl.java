package net.dixton.springapi.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.dixton.dtos.redis.ServerUpdateDto;
import net.dixton.enums.ServerKind;
import net.dixton.enums.ServerStatus;
import net.dixton.enums.ServerType;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.server.ServerNotFoundException;
import net.dixton.model.server.Server;
import net.dixton.model.server.ServerBukkit;
import net.dixton.services.RedisService;
import net.dixton.services.ServerService;
import net.dixton.springapi.DixtonSpringApiApplication;
import net.dixton.springapi.exceptions.CannotStartServerException;
import net.dixton.springapi.repositories.ServerRepository;
import net.dixton.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private static final Logger logger = LoggerFactory.getLogger(ServerServiceImpl.class);

    private final Map<Object, Server> serverCache = new ConcurrentHashMap<>();

    private final ServerRepository serverRepository;
    private final AccountServerService accountServerService;

    @PostConstruct
    private void loadServers() {
        logger.info("Loading servers...");

        List<Server> servers = serverRepository.findAll();
        for (Server server : servers) {
            this.setCache(server);
        }

        logger.info("Loaded {} servers", servers.size());
    }

    @Override
    public Server findById(Long id) {
        if (serverCache.containsKey(id)) {
            return serverCache.get(id);
        }

        Server server = this.serverRepository.findById(id)
                .orElseThrow(() -> new DixtonRuntimeException(new ServerNotFoundException(id)));

        this.setCache(server);
        return server;
    }

    @Override
    public Server findByPort(Integer port) {
        if (serverCache.containsKey(port)) {
            return serverCache.get(port);
        }

        Server server = this.serverRepository.findByPort(port)
                .orElseThrow(() -> new DixtonRuntimeException(new ServerNotFoundException(port)));

        this.setCache(server);
        return server;
    }

    @Override
    public Server findByName(String name) {
        if (serverCache.containsKey(name)) {
            return serverCache.get(name);
        }

        Server server = this.serverRepository.findByName(name)
                .orElseThrow(() -> new DixtonRuntimeException(new ServerNotFoundException(name)));

        this.setCache(server);
        return server;
    }

    @Override
    public List<Server> findByType(ServerType serverType) {
        return serverCache.values()
                .stream()
                .filter(server -> server.getType() == serverType)
                .distinct()
                .sorted(Comparator.comparing(Server::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Server> findByKind(ServerKind serverKind) {
        return serverCache.values()
                .stream()
                .filter(server -> server.getKind() == serverKind)
                .distinct()
                .sorted(Comparator.comparing(Server::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Server> findAll() {
        return serverCache.values()
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Server update(Long id, Server updatedServer) {
        Server existingServer = this.findById(id);

        if (existingServer.getStatus() != updatedServer.getStatus()) {
            logger.info("Server {} status changed from {} to {}", existingServer.getName(), existingServer.getStatus(), updatedServer.getStatus());
        }
        existingServer.updateData(updatedServer);

        try {
            RedisService.getInstance().publish(existingServer.getName(),
                    new ServerUpdateDto(existingServer.getId(), existingServer.getStatus(), existingServer.getConnectedPlayers()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this.serverRepository.save(existingServer);
    }

    @Override
    @Transactional
    public void restart(Long serverId) {
        Server server = this.findById(serverId);

        if (server.getStatus() != ServerStatus.ONLINE) {
            throw new DixtonRuntimeException(new ServerNotFoundException(serverId));
        }
        server.setStatus(ServerStatus.SHUTTING_DOWN);
        this.update(server.getId(), server);

        ServerBukkit target = (ServerBukkit) this.findByName(server.getType() == ServerType.MAIN_LOBBY ? "LOGIN" : "LOBBY_1");
        server.getConnectedAccounts().forEach((accountId, account) -> {
            this.accountServerService.sendToServer(account, target);
        });

        // wait 5 seconds or server.getConnectedPlayers == 0
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();

            while (server.getConnectedPlayers() > 0) {
                if (System.currentTimeMillis() - startTime > 5000) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            this.forceShutdown(server);

            CompletableFuture.runAsync(() -> {
                while (server.getStatus() != ServerStatus.SHUTDOWN) {
                    try {
                        Thread.sleep(100); // Evitar busy-wait
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                this.start(server);
            });
        });
    }

    private void setCache(Server server) {
        this.serverCache.put(server.getId(), server);
        this.serverCache.put(server.getPort(), server);
        this.serverCache.put(server.getName(), server);
    }

    /* Utils */

    @Transactional
    public void start(Server server) {
        logger.info("Starting server {}...", server.getName());

        if (server.getStatus() != ServerStatus.SHUTDOWN) {
            throw new CannotStartServerException(server);
        }

        server.setStatus(ServerStatus.STARTING);
        server.clearConnectedAccounts();
        this.update(server.getId(), server);

        try {
            /* Update files */
            updateFiles(server);

            /* Create server directory */
            FileUtils.createFolderIfNotExist(server.getDirectoryPath(DixtonSpringApiApplication.folderBasePath));

            /* Define java start command */
            String startCommand;
            if (server.isBukkit()) {
                startCommand = String.format(
                        server.getJar().getJavaPath() + " -Xms%dm -Xmx%dm -jar %s --port %d --max-players %d",
                        server.getMinimumMemory(), server.getMaximumMemory(), server.getJar().getPath(), server.getPort(), server.getMaxPlayers()
                );
            } else {
                startCommand = String.format(
                        server.getJar().getJavaPath() + " -Xms%dm -Xmx%dm -jar %s --port %d",
                        server.getMinimumMemory(), server.getMaxPlayers(), server.getJar().getPath(), server.getPort()
                );
            }

            /* Run process */
            String command = String.format("screen -S %s -dm bash -c '%s'", server.getName(), startCommand);

            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            processBuilder.directory(new File(server.getDirectoryPath(DixtonSpringApiApplication.folderBasePath)));
            processBuilder.inheritIO();
            Process process = processBuilder.start();

            process.waitFor();

            logger.info("Server started: {}", server.getName());
        } catch (IOException | InterruptedException e) {
            throw new CannotStartServerException(server, e);
        }
    }

    private void updateFiles(Server server) throws IOException {
        logger.info("Updating files for {}...", server.getName());

        String serverDirectoryPath = server.getDirectoryPath(DixtonSpringApiApplication.folderBasePath);

        copyFiles(server.getUpdaterDirectoryPath(DixtonSpringApiApplication.folderBasePath), serverDirectoryPath);
        copyFiles(server.getKind().getUpdaterDirectoryPath(DixtonSpringApiApplication.folderBasePath), serverDirectoryPath);
        copyFiles(server.getType().getUpdaterDirectoryPath(DixtonSpringApiApplication.folderBasePath), serverDirectoryPath);

        if (server.isBukkit()) {
            ServerBukkit serverBukkit = (ServerBukkit) server;

            if (serverBukkit.getGameMode() != null) {
                copyFiles(serverBukkit.getGameMode().getUpdaterDirectoryPath(DixtonSpringApiApplication.folderBasePath), serverDirectoryPath);
            }
        }

        logger.info("Server {} files updated", server.getName());
    }

    private void copyFiles(String fromPath, String toPath) throws IOException {
        for (File file : FileUtils.listFiles(fromPath)) {
            System.out.println(file.getAbsolutePath());
            if (file.isDirectory()) {
                switch (file.getName()) {
                    case "plugins":
                    case "translations":
                        FileUtils.createFolderIfNotExist(toPath + "/" + file.getName());
                        for (File listFile : FileUtils.listFiles(file)) {
                            FileUtils.copy(listFile, toPath + "/" + file.getName(), true);
                        }
                        continue;
                    case "worlds":
                        // select random world
                        continue;
                }
            }
            FileUtils.copy(file, toPath, true);
        }
    }

    public void shutdown(Server server) {

    }

    @Transactional
    public void forceShutdown(Server server) {
        server.setStatus(ServerStatus.SHUTTING_DOWN);
        logger.warn("Forcing shutdown server {}...", server.getName());

        String command = String.format("pkill -f \"java.*%d\" && pkill -f \"SCREEN -S %s\"", server.getPort(), server.getName());

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            processBuilder.inheritIO();
            Process process = processBuilder.start();

            process.waitFor();

            server.setStatus(ServerStatus.SHUTDOWN);
            this.update(server.getId(), server);

            logger.info("Server shut down: {}", server.getName());
        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}
