package com.solar_insight.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.solar.service.SolarBuildingInsightService;
import com.solar_insight.app.solar.utility.SolarConsumptionAnalyzer;
import com.solar_insight.app.solar.utility.SolarCostCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.File;

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

			// GeocodedLocation location = new GeocodedLocation(40.75880146718224, -72.85091345762694);
			// JsonNode response = buildingInsightService.getSolarData(location);
			// System.out.println(response);
			final int AVERAGE_MONTHLY_UB = 200;

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode response = objectMapper.readTree(new File("C:\\Users\\lipsa\\OneDrive\\Desktop\\solar-api-response.json"));
			//System.out.println(response);

			Double costOfElectricityWithoutSolar = null;
			Double initialAcKwhPerYear = null;
			Double solarPercentage = null;

			try {
				JsonNode financialDetails = response.path("solarPotential")
						.path("financialAnalyses")
						.get(7)
						.path("financialDetails");

				costOfElectricityWithoutSolar =
						financialDetails.path("costOfElectricityWithoutSolar").path("units").asDouble();

				initialAcKwhPerYear =
						financialDetails.path("initialAcKwhPerYear").asDouble();

				solarPercentage =
						financialDetails.path("solarPercentage").asDouble();

				System.out.println("Cost of Electricity Without Solar: " + costOfElectricityWithoutSolar);
				System.out.println("Initial AC Kwh Per Year: " + initialAcKwhPerYear);
				System.out.println("Solar Percentage: " + solarPercentage);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}

			if (costOfElectricityWithoutSolar != null && initialAcKwhPerYear != null && solarPercentage != null) {
				SolarConsumptionAnalyzer solarConsumptionAnalyzer =
						new SolarConsumptionAnalyzer(costOfElectricityWithoutSolar, initialAcKwhPerYear, solarPercentage);
				solarConsumptionAnalyzer.setAnnualAcKwhNeeded(200);

				double pricePerKwh = solarConsumptionAnalyzer.getPricePerKwh();
				int consumptionNeeded = solarConsumptionAnalyzer.getAnnualAcKwhNeeded();

				System.out.println("Price Per Kwh: " + pricePerKwh);
				System.out.println("Consumption Needed: " + consumptionNeeded);

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

						SolarCostCalculator solarCostCalculator = new SolarCostCalculator(AVERAGE_MONTHLY_UB, pricePerKwh, panelCount, yearlyDcKwh);
						System.out.println("Savings: " + solarCostCalculator.getSavings());
						System.out.println("Yearly Production AC: " + solarCostCalculator.getYearlyProductionAcKwh());
						System.out.println("Total Cost With Solar: " + solarCostCalculator.getTotalCostWithSolar());
						System.out.println("Total Cost Without Solar: " + solarCostCalculator.getTotalCostWithoutSolar());
						System.out.println("Incentives: " + solarCostCalculator.getSolarIncentives());
						System.out.println("Monthly Bill With Solar: " + solarCostCalculator.getMonthlyBillWithSolar());
						System.out.println("-----------------------");
					}
				}
			}



		};

	}

}




















