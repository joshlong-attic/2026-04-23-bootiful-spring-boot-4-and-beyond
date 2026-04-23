package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    RouterFunction<ServerResponse> apiRouter() {
        return route()
                .GET("/api/**", http())
                .before(BeforeFilterFunctions.uri("http://localhost:8081"))
                .filter(TokenRelayFilterFunctions.tokenRelay())
                .before(BeforeFilterFunctions.rewritePath("/api", "/"))
                .build();
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    RouterFunction<ServerResponse> uiRouter() {
        return route()
                .GET("/**", http())
                .before(BeforeFilterFunctions.uri("http://localhost:8020"))
                .build();
    }
}
