package com.solar_insight.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.solar_insight.app.solar.service.SolarBuildingInsightService;
import com.solar_insight.app.solar.utility.SolarConsumptionAnalyzer;
import com.solar_insight.app.solar.utility.SolarOutcomeAnalysis;
import com.solar_insight.app.zoho_crm.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	@Autowired
	private TokenService tokenService;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {

			String zohoAccessToken = tokenService.getAccessToken();
			System.out.println("Zoho Access Token: " + zohoAccessToken);

		};

	}

}




















