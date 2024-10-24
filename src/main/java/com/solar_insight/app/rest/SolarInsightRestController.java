package com.solar_insight.app.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.solar_insight.app.GeocodedLocation;
import com.solar_insight.app.dto.*;
import com.solar_insight.app.entity.BookedConsultation;
import com.solar_insight.app.entity.ContactAddress;
import com.solar_insight.app.entity.InMarketZip;
import com.solar_insight.app.entity.PostcardMailer;
import com.solar_insight.app.lob_mailer.dto.CreateMailerResponse;
import com.solar_insight.app.lob_mailer.dto.TrackingEventData;
import com.solar_insight.app.lob_mailer.service.MailerDataService;
import com.solar_insight.app.service.MarketDataService;
import com.solar_insight.app.service.SessionDataService;
import com.solar_insight.app.google_solar.service.SatelliteImageService;
import com.solar_insight.app.google_solar.service.SolarBuildingInsightService;
import com.solar_insight.app.google_solar.utility.SolarConsumptionAnalyzer;
import com.solar_insight.app.google_solar.utility.SolarOutcomeAnalysis;
import com.solar_insight.app.ycbm.service.BookingUrlService;
import com.solar_insight.app.zoho_crm.service.ZohoIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class SolarInsightRestController {

    private static final Logger logger = LoggerFactory.getLogger(SolarInsightRestController.class);

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    private final SolarBuildingInsightService solarBuildingInsightService;
    private final SatelliteImageService imageService;
    private final SessionDataService sessionDataService;
    private final ZohoIntegrationService zohoIntegrationService;
    private final BookingUrlService bookingUrlService;
    private final MarketDataService marketDataService;
    private final MailerDataService mailerDataService;

    @Autowired
    public SolarInsightRestController(SolarBuildingInsightService solarBuildingInsightService,
                                      SatelliteImageService imageService,
                                      SessionDataService sessionDataService,
                                      ZohoIntegrationService zohoIntegrationService,
                                      BookingUrlService bookingUrlService,
                                      MarketDataService marketDataService,
                                      MailerDataService mailerDataService) {

        this.solarBuildingInsightService = solarBuildingInsightService;
        this.imageService = imageService;
        this.sessionDataService = sessionDataService;
        this.zohoIntegrationService = zohoIntegrationService;
        this.bookingUrlService = bookingUrlService;
        this.marketDataService = marketDataService;
        this.mailerDataService = mailerDataService;
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
        if (cachedImage != null) System.out.println("Satellite Image added to cache.");

        Map<String, String> response = createPayload(solarOutcome, base64Image, preliminaryDataDTO.getSessionUUID());

        try { // Persist data
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




    /**
     * Handles the contact information submitted by the user.
     * <p>
     * If the lead is new (generated for the first time), it will be returned by processUserSessionData,
     * and a record will be created in the database and in Zoho CRM with the contact details.
     * <p>
     * If no lead is returned (Optional.empty()), this *should* indicate the user is an existing lead,
     * and only the current User Session will be updated in the database (no need to update Zoho CRM as the contact already exists).
     * <p>
     * Regardless of whether the lead is new or existing, a booking query URL is generated
     * and sent back to the client, enabling the browser and YCBM to map potential appointments back to the server.
     * <p>
     * @param contactInfo the contact information provided by the user
     * @return a response containing the booking page query URL
     */
    @PostMapping("/contact_info")
    public Map<String, String> contactInfoController(@RequestBody ContactInfoDTO contactInfo) {
        Optional<ContactAddress> optGeneratedLead = sessionDataService.processUserSessionData(contactInfo);
        optGeneratedLead.ifPresent(zohoIntegrationService::addContactToEstimate); // Send to Zoho

        Map<String, String> response = new HashMap<>();
        response.put("bookingPageQueryUrl", bookingUrlService.buildQueryUrl(contactInfo.getSessionUUID()));
        return response; // Return query URL
    }




    @PostMapping("/booked_consultation")
    public void bookedConsultationsController(@RequestBody BookingDTO bookingData) {
        System.out.println("Booking Data: " + bookingData);
        Optional<BookedConsultation> optBookedConsultation = sessionDataService.processUserSessionData(bookingData);
        optBookedConsultation.ifPresent(zohoIntegrationService::addBookingToEstimate);
    }




    @PostMapping("/mailer_tracking_events")
    public void mailerTrackingEventsController(@RequestBody TrackingEventData trackingEventData) {
        Optional<PostcardMailer> optMailer = mailerDataService.updateMailerStatusAndData(trackingEventData);
        if (optMailer.isPresent()) {
            logger.info("Mailer data updated successfully. Mailer: {}", optMailer.get());
        } else {
            logger.error("Mailer data was not able to be updated. Event Data: {}", trackingEventData);
        }
    }




    @PostMapping("/market_check")
    public ResponseEntity<MarketCheckResponseDTO> marketCheckController(@RequestBody InMarketZipDTO zipDTO) {
        // Check if the zipDTO is null or if the zip field is null (invalid key or missing key)
        if (zipDTO == null || zipDTO.getZip() == null) {
            MarketCheckResponseDTO errorResponse = new MarketCheckResponseDTO();
            errorResponse.setStatus("error");
            errorResponse.setMessage("missing_or_invalid_zip");
            errorResponse.setCounty(null);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse); // HTTP 400
        }

        try {
            Optional<InMarketZip> optMarketData = marketDataService.findMarketInfoByZip(zipDTO);

            MarketCheckResponseDTO responseDTO = new MarketCheckResponseDTO();

            if (optMarketData.isPresent()) {
                responseDTO.setStatus("success");
                responseDTO.setMessage("in_market");
                responseDTO.setCounty(optMarketData.get().getCounty());
            } else {
                responseDTO.setStatus("success");
                responseDTO.setMessage("out_of_market");
                responseDTO.setCounty(null);
            }

            return ResponseEntity.ok(responseDTO); // HTTP 200

        } catch (Exception e) {
            // Optional: log the error here
            System.out.println(e.getMessage());
            MarketCheckResponseDTO errorResponse = new MarketCheckResponseDTO();
            errorResponse.setStatus("error");
            errorResponse.setMessage("internal_server_error");
            errorResponse.setCounty(null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // HTTP 500
        }
    }




    private Map<String, String> createPayload(SolarOutcomeAnalysis analysis, String base64Image, String sessionUUID) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

        Map<String, String> response = new HashMap<>();
        response.put("estimatedSavings", numberFormat.format(analysis.getSavings()));
        response.put("systemSizeDc", String.valueOf(analysis.getYearlyDcSystemSize()));
        response.put("monthlyPayment", String.valueOf(analysis.getMonthlyBillWithSolar()));
        response.put("incentives", numberFormat.format(analysis.getSolarIncentives()));
        response.put("panelCount", String.valueOf(analysis.getPanelCount()));
        response.put("imagery", base64Image);
        response.put("uuid", sessionUUID);

        return response;
    }

}
