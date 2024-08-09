package com.solar_insight.app;

import com.solar_insight.app.solar.SolarBuildingInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	@Autowired
	private SolarBuildingInsightService solarBuildingInsightService;


	@Bean
	public CommandLineRunner demo() {
		return (args) -> {
			solarBuildingInsightService.solarCostCalculator();
		};
	}



}