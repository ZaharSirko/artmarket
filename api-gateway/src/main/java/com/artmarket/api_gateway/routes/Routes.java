package com.artmarket.api_gateway.routes;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
public class Routes {

        @Bean
        public RouterFunction<ServerResponse> paintingServiceRoutes() {
            return route("painting_service")
                    .route(RequestPredicates.path("/paintings/**"),
                            HandlerFunctions.http("http://localhost:8080"))
                    .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                            "paintingServiceCircuitBreaker",
                            URI.create("forward:/fallbackRoute")))
                    .build();
        }

        @Bean
        public RouterFunction<ServerResponse> paintingServiceSwaggerRoute() {
            return route("painting_service_swagger")
                    .route(RequestPredicates.path("/aggregate/painting-service/v3/api-docs"),
                            HandlerFunctions.http("http://localhost:8080"))
                    .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                            "paintingServiceSwaggerCircuitBreaker",
                            URI.create("forward:/fallbackRoute")))
                    .filter(setPath("/v3/api-docs"))
                    .build();
        }

        @Bean
        public RouterFunction<ServerResponse> userServiceRoutes() {
            return route("user_service")
                    .route(RequestPredicates.path("/users/**"),
                            HandlerFunctions.http("http://localhost:8081")) // порт user-service
                    .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                            "userServiceCircuitBreaker",
                            URI.create("forward:/fallbackRoute")))
                    .build();
        }

        @Bean
        public RouterFunction<ServerResponse> userServiceSwaggerRoute() {
            return route("user_service_swagger")
                    .route(RequestPredicates.path("/aggregate/user-service/v3/api-docs"),
                            HandlerFunctions.http("http://localhost:8081"))
                    .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                            "userServiceSwaggerCircuitBreaker",
                            URI.create("forward:/fallbackRoute")))
                    .filter(setPath("/v3/api-docs"))
                    .build();
        }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoutes() {
        return route("user_service")
                .route(RequestPredicates.path("/users/**"),
                        HandlerFunctions.http("http://localhost:8082")) // порт user-service
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "orderServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceSwaggerRoute() {
        return route("user_service_swagger")
                .route(RequestPredicates.path("/aggregate/order-service/v3/api-docs"),
                        HandlerFunctions.http("http://localhost:8082"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "orderServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/v3/api-docs"))
                .build();
    }


    @Bean
    public RouterFunction<ServerResponse> fallbackRoute(){
        return route("fallbackRoute")
                .GET("/fallbackRoute",request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Service Unavailable,try again latter"))
                .build();
    }

}
