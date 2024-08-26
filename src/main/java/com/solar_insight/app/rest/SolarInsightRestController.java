package com.solar_insight.app.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.solar_insight.app.GeocodedLocation;
import com.solar_insight.app.solar.service.SatelliteImageService;
import com.solar_insight.app.solar.service.SolarBuildingInsightService;
import com.solar_insight.app.solar.utility.SolarConsumptionAnalyzer;
import com.solar_insight.app.solar.utility.SolarOutcomeAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.NumberFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SolarInsightRestController {

    private final SolarBuildingInsightService solarBuildingInsightService;

    private final SatelliteImageService imageService;

    @Autowired
    public SolarInsightRestController(SolarBuildingInsightService solarBuildingInsightService, SatelliteImageService imageService) {
        this.solarBuildingInsightService = solarBuildingInsightService;
        this.imageService = imageService;
    }

    @PostMapping("/preliminary_data")
    public Map<String, String> preliminaryDataController(@RequestBody PreliminaryDataDTO preliminaryDataDTO) {
        System.out.println(preliminaryDataDTO);

        GeocodedLocation geocodedLocation =
                new GeocodedLocation(preliminaryDataDTO.getLatitude(), preliminaryDataDTO.getLongitude());

        JsonNode buildingInsightResponse = solarBuildingInsightService.getSolarData(geocodedLocation);

        SolarConsumptionAnalyzer consumptionAnalysis =
                solarBuildingInsightService.parseConsumptionAnalysis(
                        buildingInsightResponse, preliminaryDataDTO.getAvgMonthlyEnergyBill());

        SolarOutcomeAnalysis solarOutcome =
                solarBuildingInsightService.idealSolarAnalysis(
                        buildingInsightResponse, consumptionAnalysis, preliminaryDataDTO.getAvgMonthlyEnergyBill());


        byte[] imageBytes = imageService.getSatelliteImage(geocodedLocation);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        Map<String, String> response = new HashMap<>();
        response.put("estimatedSavings", numberFormat.format(solarOutcome.getSavings()));
        response.put("systemSizeDc", String.valueOf(solarOutcome.getYearlyDcSystemSize()));
        response.put("monthlyPayment", String.valueOf(solarOutcome.getMonthlyBillWithSolar()));
        response.put("incentives", numberFormat.format(solarOutcome.getSolarIncentives()));
        response.put("panelCount", String.valueOf(solarOutcome.getPanelCount()));
        response.put("imagery", base64Image);

        return response;
    }

}
