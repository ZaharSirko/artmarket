package com.artmarket.api_gateway;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.from(ApiGatewayApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
