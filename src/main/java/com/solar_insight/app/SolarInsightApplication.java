package com.solar_insight.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.solar.SolarBuildingInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@SpringBootApplication
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	@Autowired
	private RestTemplate restTemplate;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {

			double pricePerKwh = calculateCurrentPricePerKWh(29096, 4047.7117, 94.06412);
			System.out.println("Price Pre Kwh: " + pricePerKwh);

		};

	}

	public double calculateCurrentPricePerKWh(double costOfElectricityWithoutSolar,
											  double initialAcKwhPerYear, double solarPercentage) {
		// Constants
		final double INFLATION_RATE = 0.022; // 2.2%
		final int YEARS = 20; // 20 years

		// Calculate the total annual consumption without solar
		double totalEnergyConsumptionKWh = initialAcKwhPerYear / (solarPercentage / 100.0);
		System.out.println("totalEnergyConsumptionKWh: " + totalEnergyConsumptionKWh);

		// Calculate the Present Value of the total cost by reversing inflation
		double presentValueTotalCost = costOfElectricityWithoutSolar / Math.pow(1 + INFLATION_RATE, YEARS);
		System.out.println("presentValueTotalCost: " + presentValueTotalCost);

		// Total energy consumption over the specified number of years
		double totalEnergyConsumptionOverYears = totalEnergyConsumptionKWh * YEARS;
		System.out.println("totalEnergyConsumptionOverYears: " + totalEnergyConsumptionOverYears);

		// Current price per kWh
		return new BigDecimal(presentValueTotalCost / totalEnergyConsumptionOverYears)
				.setScale(3, RoundingMode.HALF_UP)
				.doubleValue();
	}




}




















