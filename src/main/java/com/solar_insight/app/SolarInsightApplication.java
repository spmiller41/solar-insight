package com.solar_insight.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	/*
	@Bean
	public CommandLineRunner demo() {
		return (args) -> {

		};
	}
	 */

}




















