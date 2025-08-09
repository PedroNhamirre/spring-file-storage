package tech.pedronhamirre.fileapi.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class KeepAliveTask {

    @Scheduled(cron = "0 0/14 * * * ?")
    public void keepServiceAlive() {
        System.out.println("Keep-alive task executada a cada 14 minutos em: " + LocalDateTime.now());
    }
}
