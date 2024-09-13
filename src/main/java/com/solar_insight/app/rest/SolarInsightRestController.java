package com.solar_insight.app.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.solar_insight.app.GeocodedLocation;
import com.solar_insight.app.dto.ContactInfoDTO;
import com.solar_insight.app.dto.PreliminaryDataDTO;
import com.solar_insight.app.dto.UserSessionDTO;
import com.solar_insight.app.entity.ContactAddress;
import com.solar_insight.app.service.SessionDataService;
import com.solar_insight.app.google_solar.service.SatelliteImageService;
import com.solar_insight.app.google_solar.service.SolarBuildingInsightService;
import com.solar_insight.app.google_solar.utility.SolarConsumptionAnalyzer;
import com.solar_insight.app.google_solar.utility.SolarOutcomeAnalysis;
import com.solar_insight.app.zoho_crm.service.ZohoIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class SolarInsightRestController {

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    private final SolarBuildingInsightService solarBuildingInsightService;
    private final SatelliteImageService imageService;
    private final SessionDataService sessionDataService;
    private final ZohoIntegrationService zohoIntegrationService;

    @Autowired
    public SolarInsightRestController(SolarBuildingInsightService solarBuildingInsightService,
                                      SatelliteImageService imageService,
                                      SessionDataService sessionDataService,
                                      ZohoIntegrationService zohoIntegrationService) {

        this.solarBuildingInsightService = solarBuildingInsightService;
        this.imageService = imageService;
        this.sessionDataService = sessionDataService;
        this.zohoIntegrationService = zohoIntegrationService;
    }

    // Health Check Endpoint
    @GetMapping("/status")
    public String status() {
        return "Solar Insight server is running...";
    }

    /**
     * This controller method handles the submission of preliminary data from the frontend.
     * It processes the provided geolocation and energy data to perform a solar analysis.
     * The method then generates a unique session UUID (userSessionUUID) to track the user's session.
     * The analysis results, along with the generated UUID, are returned in the response.
     *
     * @param preliminaryDataDTO - The DTO containing the preliminary data such as latitude, longitude, and average monthly energy bill.
     * @return A map containing the solar analysis results, including estimated savings, system size,
     *         monthly payment, incentives, panel count, satellite imagery, and the generated session UUID.
     */
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


        byte[] cachedImage = imageService.getSatelliteImage(geocodedLocation);
        String base64Image = Base64.getEncoder().encodeToString(cachedImage);

        if (cachedImage != null) {
            // Proceed with sending the image and other data to Zoho
            System.out.println("Image retrieved from cache.");
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        Map<String, String> response = new HashMap<>();
        response.put("estimatedSavings", numberFormat.format(solarOutcome.getSavings()));
        response.put("systemSizeDc", String.valueOf(solarOutcome.getYearlyDcSystemSize()));
        response.put("monthlyPayment", String.valueOf(solarOutcome.getMonthlyBillWithSolar()));
        response.put("incentives", numberFormat.format(solarOutcome.getSolarIncentives()));
        response.put("panelCount", String.valueOf(solarOutcome.getPanelCount()));
        response.put("imagery", base64Image);
        response.put("uuid", preliminaryDataDTO.getSessionUUID());

        try {
            sessionDataService.processUserSessionData(preliminaryDataDTO, solarOutcome);
        } catch (Exception ex) {
            // Add error logging here
            System.err.println("Error processing user session: " + ex.getMessage());
        }

        return response;
    }

    @PostMapping("/address_confirm")
    public void addressConfirmController(@RequestBody UserSessionDTO userSessionDTO) {
        System.out.println("User Session UUID on Address Confirm: " + userSessionDTO.getSessionUUID());
        zohoIntegrationService.sendAddressAndEstimate(userSessionDTO);
    }

    @PostMapping("/contact_info")
    public void contactInfoController(@RequestBody ContactInfoDTO contactInfo) {
        System.out.println(contactInfo);
        Optional<ContactAddress> optGeneratedLead = sessionDataService.processUserSessionData(contactInfo);
    }

}
