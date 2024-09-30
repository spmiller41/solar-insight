package com.solar_insight.app.zoho_crm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.GeocodedLocation;
import com.solar_insight.app.entity.*;
import com.solar_insight.app.google_solar.service.SatelliteImageService;
import com.solar_insight.app.zoho_crm.enums.ZohoModuleAccess;
import com.solar_insight.app.zoho_crm.enums.ZohoModuleApiName;
import com.solar_insight.app.zoho_crm.logs.RequestLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ZohoRequestService {

    private static final Logger logger = LoggerFactory.getLogger(ZohoRequestService.class);

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

        if (jsonPayload != null) {
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonPayload, headers);
            try {
                ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, httpEntity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    RequestLogger.logPostStatus(response, logger);

                    // Parse the Zoho Record Id from the response and return it if possible.
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    String recordId = jsonNode.path("data").get(0).path("details").path("id").asText();
                    return Optional.of(recordId);
                } else {
                    RequestLogger.logPostStatus(response, logger);
                    return Optional.empty();
                }
            } catch (Exception ex) {
                RequestLogger.logExceptionMsg(solarEstimate, logger, ex);
                return Optional.empty();
            }
        } else {
            RequestLogger.logEmptyPayloadErr(logger);
            return Optional.empty();
        }
    }




    public void updateSolarInsightLead(Object object, Address address) throws IllegalArgumentException {
        if (!(object instanceof Contact || object instanceof BookedConsultation))
            throw new IllegalArgumentException("Object must be either a Contact or a BookedConsultation.");

        String accessToken = tokenService.getAccessToken(ZohoModuleAccess.CUSTOM_MODULE.toString());
        String crmRecordId = address.getZohoSolarInsightLeadId();
        String endpoint = baseUrl + ZohoModuleApiName.SOLAR_INSIGHT_LEADS + "/" + crmRecordId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload;

        try {
            if (object instanceof Contact contact) {
                jsonPayload = objectMapper.writeValueAsString(createPayload(contact));
            } else {
                BookedConsultation bookedConsultation = (BookedConsultation) object;
                jsonPayload = objectMapper.writeValueAsString(createPayload(bookedConsultation));
            }
        } catch (Exception ex) {
            RequestLogger.logPayloadCreateErr(object, logger, ex);
            return;
        }

        if (jsonPayload != null) {
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonPayload, headers);
            try {
                ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, String.class);
                RequestLogger.logPutStatus(response, logger);
            } catch (Exception ex) {
                RequestLogger.logExceptionMsg(address, logger, ex);
            }
        } else {
            RequestLogger.logEmptyPayloadErr(logger);
        }
    }




    private Map<String, Object> createPayload(Contact contact) {
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




    private Map<String, Object> createPayload(BookedConsultation bookedConsultation) {
        Map<String, Object> body = new HashMap<>();
        body.put("Appointment_Date_Time", formatDateTimeForZoho(bookedConsultation.getAppointmentDateTime()));
        body.put("Appointment_Type", bookedConsultation.getAppointmentType());

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
                RequestLogger.logImageUploadStatus(response, logger);
                return Optional.of(rootNode.path("data").get(0).path("details").path("id").asText());
            } else {
                RequestLogger.logImageUploadStatus(response, logger);
                return Optional.empty();
            }
        } catch (Exception ex) {
            RequestLogger.logImgExceptionMsg(logger, ex);
            return Optional.empty();
        }
    }




    /*
     * Formats the given LocalDateTime into a Zoho-compatible date-time string.
     *
     * @param dateTime The LocalDateTime object to be formatted.
     * @return A string formatted for Zoho expected date-time format.
     */
    private String formatDateTimeForZoho(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        return dateTime.atZone(ZoneId.of("America/New_York")).format(formatter);
    }




}
