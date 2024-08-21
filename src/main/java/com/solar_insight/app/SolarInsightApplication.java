package com.solar_insight.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.solar_insight.app.solar.service.SolarBuildingInsightService;
import com.solar_insight.app.solar.utility.SolarConsumptionAnalyzer;
import com.solar_insight.app.solar.utility.SolarOutcomeAnalysis;
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
	private RestTemplate restTemplate;

	@Autowired
	private SolarBuildingInsightService buildingInsightService;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {

			GeocodedLocation location = new GeocodedLocation(40.75880146718224, -72.85091345762694);
			JsonNode response = buildingInsightService.getSolarData(location);
			// System.out.println(response);
			final int AVERAGE_MONTHLY_UB = 200;

			//ObjectMapper objectMapper = new ObjectMapper();
			//JsonNode response = objectMapper.readTree(new File("C:\\Users\\lipsa\\OneDrive\\Desktop\\solar-api-response.json"));

			SolarConsumptionAnalyzer consumptionAnalysis = buildingInsightService.parseConsumptionAnalysis(response, AVERAGE_MONTHLY_UB);
			SolarOutcomeAnalysis solarOutcome = buildingInsightService.idealSolarAnalysis(response, consumptionAnalysis, AVERAGE_MONTHLY_UB);
			System.out.println(solarOutcome);
		};

	}

}




















