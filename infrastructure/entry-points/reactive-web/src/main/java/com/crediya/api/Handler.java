package com.crediya.api;

import com.crediya.api.config.RoutesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpMethod;

@Component
@Slf4j
public class Handler {
    private final WebClient webClient;
    private final RoutesConfig routesConfig;

    public Handler(WebClient.Builder builder, RoutesConfig routesConfig) {
        this.webClient = builder.build();
        this.routesConfig = routesConfig;
    }

    public Mono<ServerResponse> proxy(ServerRequest request) {
        String[] segments = request.path().split("/");
        if (segments.length < 2) {
            return ServerResponse.badRequest().bodyValue("Servicio no especificado");
        }

        String serviceKey = segments[1];
        String baseUrl = routesConfig.getServices().get(serviceKey);

        if (baseUrl == null) {
            return ServerResponse.badRequest().bodyValue("Servicio desconocido: " + serviceKey);
        }

        String path = request.uri().getPath().replaceFirst("/" + serviceKey, "");

        String targetUrl = baseUrl + path +
                (request.uri().getQuery() != null ? "?" + request.uri().getQuery() : "");

        log.info("Redirigiendo petición a: {}", targetUrl);
        return webClient.method(HttpMethod.valueOf(request.methodName()))
                .uri(targetUrl)
                .headers(headers -> headers.addAll(request.headers().asHttpHeaders()))
                .body(request.bodyToMono(String.class), String.class)
                .exchangeToMono(response -> response.toEntity(String.class)
                        .flatMap(entity -> ServerResponse
                                .status(entity.getStatusCode())
                                .headers(h -> h.addAll(entity.getHeaders()))
                                .bodyValue(entity.getBody() != null ? entity.getBody() : "")
                        )
                );
    }
}

