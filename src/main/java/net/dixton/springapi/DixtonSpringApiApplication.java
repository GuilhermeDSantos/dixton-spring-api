package net.dixton.springapi;

import net.dixton.events.RedisMessageEvent;
import net.dixton.services.EventService;
import net.dixton.springapi.listeners.RedisListener;
import net.dixton.springapi.services.player.BalanceServiceImpl;
import net.dixton.springapi.startup.ServerStartup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "net.dixton")
public class DixtonSpringApiApplication {

    // This should come from settings.json in the future
    public static final String folderBasePath = System.getProperty("os.name").contains("Mac") ? "/Users/guilhermedutra/GoogleDrive/Documents/Business/Dixton" : "/root/";

    private static ServerStartup serverStartup;
    private static BalanceServiceImpl balanceService;

    @Autowired
    public DixtonSpringApiApplication(ServerStartup serverStartup, BalanceServiceImpl balanceService) {
        DixtonSpringApiApplication.serverStartup = serverStartup;
        DixtonSpringApiApplication.balanceService = balanceService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DixtonSpringApiApplication.class, args);

        DixtonSpringApiApplication.serverStartup.start();

        /* Listeners */
        EventService.getInstance().registerObserver(RedisMessageEvent.class, new RedisListener(balanceService));
    }

}
