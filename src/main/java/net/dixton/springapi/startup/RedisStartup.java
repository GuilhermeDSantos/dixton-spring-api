package net.dixton.springapi.startup;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.dixton.services.RedisService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisStartup {

    @PostConstruct
    public void start() {
        RedisService.getInstance().subscribe("API");
    }
}
