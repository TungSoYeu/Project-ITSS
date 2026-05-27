package com.ooas;

import com.ooas.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OoasApplication {

    public static void main(String[] args) {
        EnvLoader.load();
        SpringApplication.run(OoasApplication.class, args);
    }
}
