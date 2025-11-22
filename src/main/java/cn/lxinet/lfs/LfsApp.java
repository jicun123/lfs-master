package cn.lxinet.lfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LfsApp {
    public static void main(String[] args) {
        SpringApplication.run(LfsApp.class, args);
    }
}
