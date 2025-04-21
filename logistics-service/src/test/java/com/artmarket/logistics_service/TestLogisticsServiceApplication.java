package com.artmarket.logistics_service;

import org.springframework.boot.SpringApplication;

public class TestLogisticsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(LogisticsServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
