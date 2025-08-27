package com.crediya.api.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Setter
@Getter
@ConfigurationProperties(prefix = "routes")
public class RoutesConfig {
    private Map<String, String> services;

    @PostConstruct
    public void init() {
        System.out.println("Servicios configurados: " + services);
    }
}
