package net.dixton.springapi.startup;

import lombok.RequiredArgsConstructor;
import net.dixton.enums.GameMode;
import net.dixton.enums.ServerKind;
import net.dixton.enums.ServerType;
import net.dixton.model.server.Server;
import net.dixton.springapi.DixtonSpringApiApplication;
import net.dixton.springapi.services.ServerServiceImpl;
import net.dixton.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ServerStartup {

    private static final Logger logger = LoggerFactory.getLogger(ServerStartup.class);

    private final ServerServiceImpl serverService;

    public void start() {
        try {
            this.createDefaultFolders();
            this.startAllServers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDefaultFolders() throws IOException {
        logger.info("Creating default folders");

        /* Server Directories */
        for (Server server : this.serverService.findAll()) {
            FileUtils.createFolderIfNotExist(server.getDirectoryPath(DixtonSpringApiApplication.folderBasePath));
        }

        /* Updaters directories */
        for (Server server : this.serverService.findAll()) {
            FileUtils.createFolderIfNotExist(server.getUpdaterDirectoryPath(DixtonSpringApiApplication.folderBasePath));
        }
        for (GameMode value : GameMode.values()) {
            FileUtils.createFolderIfNotExist(value.getUpdaterDirectoryPath(DixtonSpringApiApplication.folderBasePath));
        }
        for (ServerKind value : ServerKind.values()) {
            FileUtils.createFolderIfNotExist(value.getUpdaterDirectoryPath(DixtonSpringApiApplication.folderBasePath));
        }
        for (ServerType value : ServerType.values()) {
            FileUtils.createFolderIfNotExist(value.getUpdaterDirectoryPath(DixtonSpringApiApplication.folderBasePath));
        }

        logger.info("Default folders created");
    }

    private void startAllServers() {
        logger.info("Starting servers");

        int started = 0;

        for (Server server : this.serverService.findAll()) {
            if (server.isAutoStart()) {
                this.serverService.forceShutdown(server);
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        for (Server server : this.serverService.findAll()) {
            if (server.isAutoStart()) {
                this.serverService.start(server);
                started++;
            }
        }

        logger.info("{} servers started", started);
    }
}
