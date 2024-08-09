package com.solar_insight.app.solar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.logs.RestTemplateLog;
import com.solar_insight.app.logs.SolarBuildingInsightLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.IntStream;

@Service
public class SolarBuildingInsightService {

    private static final Logger logger = LoggerFactory.getLogger(SolarBuildingInsightService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.maps.api.key}")
    private String googleMapAPIKey;

    @Value("${google.building.insight.base.url}")
    private String googleBuildingInsightBaseURL;

    @Autowired
    public SolarBuildingInsightService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }




    /**
     * Fetches solar data for a given location.
     *
     * @param location The geocoded location.
     * @return The JSON node containing the solar data.
     */
    public JsonNode getSolarData(GeocodedLocation location) {
        String requestURL = String.format("%s?location.latitude=%s&location.longitude=%s&requiredQuality=HIGH&key=%s",
                googleBuildingInsightBaseURL, location.latitude(), location.longitude(), googleMapAPIKey);

        String response;
        try {
            response = restTemplate.getForObject(requestURL, String.class);
        } catch (Exception ex) {
            RestTemplateLog.requestError(SolarBuildingInsightService.class, ex, logger);
            return null;
        }

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException ex) {
            SolarBuildingInsightLog.parsingError(location, ex, logger);
            return null;
        }

        return jsonNode;
    }



    public void solarCostCalculator() {
        // Solar configuration
        int panelsCount = 20;
        double yearlyEnergyDcKwh = 12000;

        // Basic settings
        double monthlyAverageEnergyBill = 300;
        double energyCostPerKwh = 0.31;
        double panelCapacityWatts = 400;
        double installationCostPerWatt = 4.0;
        int installationLifeSpan = 20;

        // Advanced settings
        double dcToAcDerate = 0.85;
        double efficiencyDepreciationFactor = 0.995;
        double costIncreaseFactor = 1.022;
        double discountRate = 1.04;

        // Solar installation
        double installationSizeKw = (panelsCount * panelCapacityWatts) / 1000.0;
        double installationCostTotal = installationCostPerWatt * installationSizeKw * 1000;

        // Federal tax incentive: 30% of the total installation cost
        double solarIncentives = installationCostTotal * 0.30;

        // Energy consumption
        double monthlyKwhEnergyConsumption = monthlyAverageEnergyBill / energyCostPerKwh;
        double yearlyKwhEnergyConsumption = monthlyKwhEnergyConsumption * 12;

        // Energy produced for installation life span
        double initialAcKwhPerYear = yearlyEnergyDcKwh * dcToAcDerate;
        double[] yearlyProductionAcKwh = new double[installationLifeSpan];
        for (int year = 0; year < installationLifeSpan; year++) {
            yearlyProductionAcKwh[year] = initialAcKwhPerYear * Math.pow(efficiencyDepreciationFactor, year);
        }

        // Cost with solar for installation life span
        double[] yearlyUtilityBillEstimates = new double[installationLifeSpan];
        for (int year = 0; year < installationLifeSpan; year++) {
            double billEnergyKwh = yearlyKwhEnergyConsumption - yearlyProductionAcKwh[year];
            double billEstimate = (billEnergyKwh * energyCostPerKwh * Math.pow(costIncreaseFactor, year)) / Math.pow(discountRate, year);
            yearlyUtilityBillEstimates[year] = Math.max(billEstimate, 0);
        }
        double remainingLifetimeUtilityBill = java.util.Arrays.stream(yearlyUtilityBillEstimates, 0, installationLifeSpan).sum();
        double totalCostWithSolar = installationCostTotal + remainingLifetimeUtilityBill - solarIncentives;
        System.out.printf("Cost with solar: $%.2f%n", totalCostWithSolar);

        // Cost without solar for installation life span
        double[] yearlyCostWithoutSolar = new double[installationLifeSpan];
        for (int year = 0; year < installationLifeSpan; year++) {
            yearlyCostWithoutSolar[year] = (monthlyAverageEnergyBill * 12 * Math.pow(costIncreaseFactor, year)) / Math.pow(discountRate, year);
        }
        double totalCostWithoutSolar = java.util.Arrays.stream(yearlyCostWithoutSolar, 0, installationLifeSpan).sum();
        System.out.printf("Cost without solar: $%.2f%n", totalCostWithoutSolar);

        // Savings with solar for installation life span
        double savings = totalCostWithoutSolar - totalCostWithSolar;
        System.out.printf("Savings: $%.2f in %d years (including $%.2f in incentives)%n", savings + solarIncentives, installationLifeSpan, solarIncentives);
    }




}
