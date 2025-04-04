package io.github.zuneho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SandBoxApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandBoxApiApplication.class, args);
    }
}