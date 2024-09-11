package com.solar_insight.app.zoho_crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.zoho_crm.ZohoModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ZohoRequestService {

    @Value("${zoho.api.base.url}")
    private String baseUrl;

    private final TokenService tokenService;
    private final RestTemplate restTemplate;

    @Autowired
    public ZohoRequestService(TokenService tokenService, RestTemplate restTemplate) {
        this.tokenService = tokenService;
        this.restTemplate = restTemplate;
    }

    public void createLeadPreliminaryData(Address address, SolarEstimate solarEstimate, String sessionUUID) {
        String accessToken = tokenService.getAccessToken();
        String endpoint = baseUrl + ZohoModule.Solar_Insight_Leads;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(createPayload(address, solarEstimate, sessionUUID));
        } catch (Exception ex) {
            // Add more organized error logging here
            System.err.println(ex.getMessage());
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(jsonPayload, headers);
        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, httpEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // Add more organized info logging here.
            System.out.println("Lead created with address and estimate successfully.");
        } else {
            // Add more organized error logging here.
            System.err.println("Failed to create lead with address and estimate.");
        }
    }

    private Map<String, Object> createPayload(Address address, SolarEstimate solarEstimate, String sessionUUID) {
        Map<String, Object> body = new HashMap<>();

        // Zoho expects a name for the record. Use street and zip.
        String recordName = address.getStreet() + " - " + address.getZip();
        body.put("Name", recordName);

        body.put("Street", address.getStreet());
        body.put("Unit", address.getUnit());
        body.put("City", address.getCity());
        body.put("State", address.getState());
        body.put("Zip", address.getZip());
        body.put("Monthly_Bill", solarEstimate.getMonthlyBill());
        body.put("System_Size_DC", solarEstimate.getSystemSizeDc());
        body.put("Estimated_Panels_Needed", solarEstimate.getPanelCount());
        body.put("Savings", solarEstimate.getEstimatedSavings());
        body.put("Cost_Without_Solar", solarEstimate.getCostWithoutSolar());
        body.put("Cost_With_Solar", solarEstimate.getCostWithSolar());
        body.put("Incentives", solarEstimate.getIncentives());
        body.put("Annual_AC_Production", solarEstimate.getAnnualProductionAc());
        body.put("User_Session_UUID", sessionUUID);

        // Wrap the record inside a "data" key, as Zoho expects an array of records
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", List.of(body));

        return payload;
    }

}
