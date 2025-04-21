package com.artmarket.painting_service;

import org.springframework.boot.SpringApplication;

public class TestPaintingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(PaintingServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
