package com.solar_insight.app.google_solar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.logs.RestTemplateLogger;
import com.solar_insight.app.logs.SolarBuildingInsightLogger;
import com.solar_insight.app.GeocodedLocation;
import com.solar_insight.app.google_solar.utility.SolarConsumptionAnalyzer;
import com.solar_insight.app.google_solar.utility.SolarOutcomeAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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
     * Attempts to retrieve solar data for a given location by progressively
     * degrading the quality of data from HIGH to MEDIUM to LOW if necessary.
     * Handles 4xx errors by trying the next quality level and logs warnings
     * for unavailable data at each level. Returns null if all attempts fail.
     *
     * @param location The geocoded location (latitude and longitude) to query for solar data.
     * @return A JsonNode containing the solar data, or null if data is unavailable.
     */
    public JsonNode getSolarData(GeocodedLocation location) {
        String[] qualityLevels = {"HIGH", "MEDIUM", "LOW"};
        String response = null;

        for (String quality : qualityLevels) {
            String requestUrl = String.format(
                    "%s?location.latitude=%s&location.longitude=%s&requiredQuality=%s&key=%s",
                    googleBuildingInsightBaseURL, location.latitude(), location.longitude(), quality, googleMapAPIKey);

            try {
                response = restTemplate.getForObject(requestUrl, String.class);
                if (response != null) {
                    break;
                }
            } catch (HttpClientErrorException e) {
                if (!e.getStatusCode().is4xxClientError()) {
                    RestTemplateLogger.requestError(SolarBuildingInsightService.class, e, logger);
                    return null;
                }
                logger.warn("{} quality data not available for location: {}", quality, location);
            } catch (Exception e) {
                RestTemplateLogger.requestError(SolarBuildingInsightService.class, e, logger);
                return null;
            }
        }

        if (response != null) {
            try {
                return objectMapper.readTree(response); // Parse the JSON response
            } catch (JsonProcessingException e) {
                SolarBuildingInsightLogger.parsingError(location, e, logger);
            }
        }

        logger.error("No data available for location: {}, {}", location.latitude(), location.longitude());
        return null;
    }




    /**
     * Parses the solar consumption analysis from the given JSON response.
     * This method extracts financial details related to solar energy consumption.
     * It dynamically searches for a valid panel configuration by checking the "panelConfigIndex" field.
     * If no valid panel configuration is found or if any required financial fields are missing or invalid
     * (e.g., non-numeric values or missing data), the method returns null.
     *
     * @param solarBuildingInsightResponse The JSON response containing solar building insight data.
     * @param monthlyBill The monthly electricity bill amount.
     * @return A SolarConsumptionAnalyzer object populated with the extracted data, or null if there are issues.
     */
    public SolarConsumptionAnalyzer parseConsumptionAnalysis(JsonNode solarBuildingInsightResponse, int monthlyBill) {
        try {
            JsonNode financialAnalysis = solarBuildingInsightResponse.path("solarPotential").path("financialAnalyses");

            int validPanelConfigIndex = -1;  // Default to invalid index
            for (int i = 0; i < financialAnalysis.size(); i++) {
                if (financialAnalysis.get(i).path("panelConfigIndex").asInt() != -1) {
                    validPanelConfigIndex = i;
                    break;
                }
            }

            // Handle case where no valid panelConfigIndex is found
            if (validPanelConfigIndex == -1) {
                System.err.println("No valid panel configuration found.");
                return null;
            }

            JsonNode financialDetails = financialAnalysis.get(validPanelConfigIndex).path("financialDetails");

            double costOfElectricityWithoutSolar = financialDetails.path("costOfElectricityWithoutSolar").path("units").asDouble(0.0);
            double initialAcKwhPerYear = financialDetails.path("initialAcKwhPerYear").asDouble(0.0);
            double solarPercentage = financialDetails.path("solarPercentage").asDouble(0.0);

            if (costOfElectricityWithoutSolar == 0.0 || initialAcKwhPerYear == 0.0 || solarPercentage == 0.0) {
                System.err.println("Required fields are missing or invalid");
                return null;
            }

            return new SolarConsumptionAnalyzer(monthlyBill, costOfElectricityWithoutSolar, initialAcKwhPerYear, solarPercentage);
        } catch (Exception ex) {
            System.err.println("Error in parseConsumptionAnalysis: " + ex.getMessage());
            return null;
        }
    }




    /**
     * Analyzes the ideal solar panel configuration for a building based on the given solar building insight response.
     * The method iterates through different solar panel configurations and calculates the potential savings for each.
     * It returns the SolarOutcomeAnalysis instance representing the configuration with the maximum savings.
     *
     * @param solarBuildingInsightResponse The JSON response containing solar panel configuration data.
     * @param consumptionAnalysis The analysis of solar consumption data, including the price per kWh.
     * @param monthlyBill The monthly electricity bill amount.
     * @return A SolarOutcomeAnalysis object representing the most cost-effective solar panel configuration, or null if an error occurs.
     */
    public SolarOutcomeAnalysis idealSolarAnalysis(JsonNode solarBuildingInsightResponse, SolarConsumptionAnalyzer consumptionAnalysis, int monthlyBill) {
        try {
            double pricePerKwh = consumptionAnalysis.getPricePerKwh();
            SolarOutcomeAnalysis solarOutcome = null;

            JsonNode solarPanelConfig = solarBuildingInsightResponse.path("solarPotential").path("solarPanelConfigs");

            for (JsonNode config : solarPanelConfig) {
                int panelCount = config.path("panelsCount").asInt();
                double yearlyDcKwh = config.path("yearlyEnergyDcKwh").asDouble();

                SolarOutcomeAnalysis tempCostCalculator = new SolarOutcomeAnalysis(monthlyBill, pricePerKwh, panelCount, yearlyDcKwh);

                if (solarOutcome == null || tempCostCalculator.getSavings() > solarOutcome.getSavings()) {
                    solarOutcome = tempCostCalculator;
                }
            }

            return solarOutcome;
        } catch (Exception ex) {
            System.err.println("Error in idealSolarAnalysis: " + ex.getMessage());
            return null;
        }
    }




}
