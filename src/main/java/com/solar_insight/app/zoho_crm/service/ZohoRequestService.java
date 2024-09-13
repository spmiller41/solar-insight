package com.solar_insight.app.zoho_crm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.GeocodedLocation;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.Contact;
import com.solar_insight.app.entity.ContactAddress;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.google_solar.service.SatelliteImageService;
import com.solar_insight.app.zoho_crm.enums.ZohoModuleAccess;
import com.solar_insight.app.zoho_crm.enums.ZohoModuleApiName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ZohoRequestService {

    @Value("${zoho.api.base.url}")
    private String baseUrl;

    private final TokenService tokenService;
    private final RestTemplate restTemplate;
    private final SatelliteImageService satelliteImageService;

    @Autowired
    public ZohoRequestService(TokenService tokenService, RestTemplate restTemplate, SatelliteImageService satelliteImageService) {
        this.tokenService = tokenService;
        this.restTemplate = restTemplate;
        this.satelliteImageService = satelliteImageService;
    }




    public Optional<String> createSolarInsightLead(Address address, SolarEstimate solarEstimate, String sessionUUID) {
        String accessToken = tokenService.getAccessToken(ZohoModuleAccess.CUSTOM_MODULE.toString());
        String endpoint = baseUrl + ZohoModuleApiName.SOLAR_INSIGHT_LEADS;

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
        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, httpEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // Add more organized info logging here.
                System.out.println("Solar Insight Lead created with address and estimate successfully.");

                // Parse the Zoho Record Id from the response and return it if possible.
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String recordId = jsonNode.path("data").get(0).path("details").path("id").asText();
                return Optional.of(recordId);
            } else {
                // Add more organized error logging here.
                System.err.println("Failed to create lead with address and estimate.");
                return Optional.empty();
            }
        } catch (Exception ex) {
            // Add organized error logging here
            System.err.println("Exception Message: " + ex.getMessage());
            return Optional.empty();
        }
    }



    public void updateSolarInsightLead(Contact contact, Address address) {
        String accessToken = tokenService.getAccessToken(ZohoModuleAccess.CUSTOM_MODULE.toString());
        String crmRecordId = address.getZohoSolarInsightLeadId();
        String endpoint = baseUrl + ZohoModuleApiName.SOLAR_INSIGHT_LEADS + "/" + crmRecordId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(createPayload(contact, address));
        } catch (Exception ex) {
            // Add more organized error logging here
            System.err.println(ex.getMessage());
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(jsonPayload, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // Add organized info logging here
                System.out.println("Solar Insight Lead updated successfully with contact information.");
            } else {
                // Add organized error logging here
                System.err.println("Failed to update Solar Insight Lead with contact information.");
            }
        } catch (Exception ex) {
            // Add organized error logging here
            System.err.println("Exception Message: " + ex.getMessage());
        }
    }




    private Map<String, Object> createPayload(Contact contact, Address address) {
        Map<String, Object> body = new HashMap<>();
        body.put("First_Name", contact.getFirstName());
        body.put("Last_Name", contact.getLastName());
        body.put("Email", contact.getEmail());
        body.put("Phone", contact.getPhone());

        // Wrap the record inside a "data" key, as Zoho expects an array of records
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", List.of(body));

        return payload;
    }




    private Map<String, Object> createPayload(Address address, SolarEstimate solarEstimate, String sessionUUID) {
        Optional<String> optZohoFileId = Optional.empty();

        try {
            File jpegSatellite = satelliteImageService.createJpegFromCachedImage(
                            new GeocodedLocation(address.getLatitude(), address.getLongitude()));
            optZohoFileId = uploadImageToZoho(jpegSatellite);
        } catch (IOException ex) {
            // Add more organized error logging here
            System.out.println(ex.getMessage());
        }

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

        // Wrap the Encrypted_Id for the Satellite_Image field
        optZohoFileId.ifPresent(zohoFileId -> {
            Map<String, String> imageUpload = new HashMap<>();
            imageUpload.put("Encrypted_Id", zohoFileId);  // Use 'Encrypted_Id' for image upload
            body.put("Satellite_Image", List.of(imageUpload));  // Wrap it in a list
        });

        // Wrap the record inside a "data" key, as Zoho expects an array of records
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", List.of(body));

        return payload;
    }




    private Optional<String> uploadImageToZoho(File imageFile) {
        String accessToken = tokenService.getAccessToken(ZohoModuleAccess.FILE_UPLOAD.toString());
        String endpoint = baseUrl + ZohoModuleApiName.FILE_UPLOAD;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(accessToken);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(imageFile));  // Make sure the key is 'file'

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                return Optional.of(rootNode.path("data").get(0).path("details").path("id").asText());
            } else {
                // Add more organized error logging here.
                System.err.println("Failed to upload image to Zoho: " + response.getBody());
                return Optional.empty();
            }
        } catch (Exception ex) {
            // Add more organized error logging here.
            System.err.println("Exception while uploading image: " + ex.getMessage());
            return Optional.empty();
        }
    }




}
