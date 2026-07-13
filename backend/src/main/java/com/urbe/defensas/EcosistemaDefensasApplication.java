package com.urbe.defensas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EcosistemaDefensasApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcosistemaDefensasApplication.class, args);
    }

}
