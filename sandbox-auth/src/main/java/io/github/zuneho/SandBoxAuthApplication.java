package io.github.zuneho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SandBoxAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(SandBoxAuthApplication.class, args);
    }
}