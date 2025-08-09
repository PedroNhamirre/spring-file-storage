package tech.pedronhamirre.fileapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApiApplication.class, args);
    }

}
