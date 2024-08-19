package com.solar_insight.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.solar.SolarBuildingInsightService;
import com.solar_insight.app.solar.SolarCostCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
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

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode response = objectMapper.readTree(new File("C:\\Users\\lipsa\\OneDrive\\Desktop\\solar-api-response.json"));
			//response = response.get(0);

			double pricePerKwh = calculateCurrentPricePerKWh(59141, 9256.011, 99.75386);
			double annualConsumption = (200 / pricePerKwh) * 12;
			System.out.println("Price Pre Kwh: " + pricePerKwh);
			System.out.println("Annual Consumption: " + annualConsumption);

			JsonNode solarPanelConfig = null;
			try {
				solarPanelConfig = response.path("solarPotential").path("solarPanelConfigs");
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}

			if (solarPanelConfig != null) {
				for (JsonNode config : solarPanelConfig) {
					int panelCount = config.path("panelsCount").asInt();
					double yearlyDcKwh = config.path("yearlyEnergyDcKwh").asDouble();
					System.out.println("Panel Count: " + panelCount);
					System.out.println("Yearly Energy DC: " + yearlyDcKwh);

					SolarCostCalculator solarCostCalculator = new SolarCostCalculator(200, pricePerKwh, panelCount, yearlyDcKwh);
					System.out.println("Savings: " + solarCostCalculator.getSavings());
					System.out.println("Yearly Production AC: " + solarCostCalculator.getYearlyProductionAcKwh());
					System.out.println("Total Cost With Solar: " + solarCostCalculator.getTotalCostWithSolar());
					System.out.println("Total Cost Without Solar: " + solarCostCalculator.getTotalCostWithoutSolar());
					System.out.println("Incentives: " + solarCostCalculator.getSolarIncentives());
					System.out.println("Monthly Energy Cost With Solar: " + solarCostCalculator.getMonthlyBillWithSolar());
					System.out.println("-----------------------");
				}
			}


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




















