package com.solar_insight.app.zoho_crm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.GeocodedLocation;
import com.solar_insight.app.lob_mailer.MailerStatus;
import com.solar_insight.app.lob_mailer.dto.TrackingEventData;
import com.solar_insight.app.zoho_crm.dto.CreateMailerRequestDTO;
import com.solar_insight.app.entity.*;
import com.solar_insight.app.google_solar.service.SatelliteImageService;
import com.solar_insight.app.zoho_crm.dto.MailerDTO;
import com.solar_insight.app.zoho_crm.dto.FetchedSubformData;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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




    public Optional<String> createSolarInsightLead(Address address, SolarEstimate solarEstimate, String sessionUUID, String referrer) {
        String accessToken = tokenService.getAccessToken(ZohoModuleAccess.CUSTOM_MODULE.toString());
        String endpoint = baseUrl + ZohoModuleApiName.SOLAR_INSIGHT_LEADS;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(createPayload(address, solarEstimate, sessionUUID, referrer));
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




    public void updateMailerSubformRecord(FetchedSubformData fetchedData, TrackingEventData eventData) {
        String accessToken = tokenService.getAccessToken(ZohoModuleAccess.CUSTOM_MODULE.toString());
        String endpoint = String.format("%s/%s/%s",
                baseUrl, ZohoModuleApiName.SOLAR_INSIGHT_LEADS, fetchedData.getParentId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = null;

        Optional<Map<String, Object>> optPayload = createPayload(fetchedData, eventData);
        if (optPayload.isPresent()) {
            try {
                jsonPayload = objectMapper.writeValueAsString(optPayload.get());
            } catch (Exception ex) {
                logger.error("Exception occurred while attempting to serialize data during tracking event update to Zoho. Message: {}", ex.getMessage());
            }
        } else {
            logger.error("Payload could not be created to update Zoho with tracking event. Fetched Data: {} --- Event Data: {}", fetchedData, eventData);
            return;
        }

        if (jsonPayload != null) {
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonPayload, headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Tracking event updated in Zoho successfully. Status Code: {}", response.getStatusCode());
                } else if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
                    logger.error("Tracking event could not be updated in Zoho. Status Code: {}", response.getStatusCode());
                }
            } catch (Exception ex) {
                logger.error("Exception occurred while attempting exchange with Zoho. Message: {}", ex.getMessage());
            }
        }
    }




    public Optional<String> getSolarInsightLeadById(String recordId) {
        String url = String.format("%s/%s/%s", baseUrl, ZohoModuleApiName.SOLAR_INSIGHT_LEADS, recordId);
        String accessToken = tokenService.getAccessToken(ZohoModuleAccess.CUSTOM_MODULE.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make the GET request
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Fetch Zoho Solar Insight data successful. Status Code: {}", response.getStatusCode());
                return Optional.ofNullable(response.getBody());
            } else if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
                logger.error("Fetch Zoho Solar Insight Error. Status Code: {}", response.getStatusCode());
            }
        } catch (Exception ex) {
            logger.error("Fetch Zoho Solar Insight Exception Occurred. Message: {}", ex.getMessage());
        }

        return Optional.empty();
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




    public ResponseEntity<String> syncMailerRecordToZoho(PostcardMailer mailer, CreateMailerRequestDTO mailerRequestData) {
        String accessToken = tokenService.getAccessToken(ZohoModuleAccess.CUSTOM_MODULE.toString());
        String crmRecordId = mailerRequestData.getSolarInsightLeadId();
        String endpoint = baseUrl + ZohoModuleApiName.SOLAR_INSIGHT_LEADS + "/" + crmRecordId;
        ResponseEntity<String> response = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = null;
        Map<String, Object> payload = createPayload(mailer, mailerRequestData);

        try {
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            logger.error("Error occurred in ObjectMapper. There was an issue while attempting to serialize payload for mailer creation. Payload: {}", jsonPayload);
        }

        if (jsonPayload != null) {
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonPayload, headers);

            try {
                response = restTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Mailer data sent to Zoho Successfully. Status Code: {}", response.getStatusCode());
                } else if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
                    logger.error("Error while attempting to send mailer data to Zoho: {}", response.getStatusCode());
                }
            } catch (Exception ex) {
                logger.error("There was an issue while attempting to send mailer data to Zoho");
            }
        }

        return response;
    }




    public void createLeadFromMailerAppointment(MailerBooking mBooking) {
        String accessToken = tokenService.getAccessToken(ZohoModuleAccess.LEADS.toString());
        String endpoint = baseUrl + ZohoModuleApiName.LEADS;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = null;

        try {
            jsonPayload = objectMapper.writeValueAsString(createPayload(mBooking));
        } catch (Exception ex) {
            logger.error("Error while attempting to generate payload for zoho lead from mailer. Message: {}", ex.getMessage());
        }

        if (jsonPayload != null) {
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonPayload, headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, httpEntity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Zoho lead created with appointment from mailer. Status Code: {}", response.getStatusCode());
                } else if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
                    logger.error("Error while attempting to create zoho lead with appointment from mailer. Status Code: {}", response.getStatusCode());
                } else {
                    logger.warn("Zoho lead creation attempt with appointment from mailer. Status Code: {}", response.getStatusCode());
                }
            } catch (Exception ex) {
                logger.error("Exception occurred while attempting to generate zoho lead with appointment from mailer. Message: {}", ex.getMessage());
            }
        } else {
            logger.error("Empty payload while attempting to create zoho lead with appointment from mailer");
        }
    }




    private Map<String, Object> createPayload(MailerBooking mBooking) {
        Map<String, Object> body = new HashMap<>();

        body.put("Owner", "3880966000020271001");
        body.put("Appointment", formatDateTimeForZoho(mBooking.getStartsAt()));
        body.put("Description", "Appointment Type: " + mBooking.getAppointmentType());
        body.put("Lead_Source", "Mailer");
        body.put("Sub_Source", "Solar Insight");
        body.put("Product1", List.of("Residential Solar"));
        body.put("First_Name", mBooking.getFirstName());
        body.put("Last_Name", mBooking.getLastName());
        body.put("Email", mBooking.getEmail());
        body.put("Phone", mBooking.getPhone());
        body.put("Mobile", mBooking.getPhone());
        body.put("Street", mBooking.getStreet());
        body.put("City", mBooking.getCity());
        body.put("State", mBooking.getState());
        body.put("Zip_Code", mBooking.getZip());

        // Wrap the record inside a "data" key, as Zoho expects an array of records
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", List.of(body));

        return payload;
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




    private Map<String, Object> createPayload(Address address, SolarEstimate solarEstimate, String sessionUUID, String referrer) {
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
        body.put("Referrer1", referrer);

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



    private Map<String, Object> createPayload(PostcardMailer mailer, CreateMailerRequestDTO mailerRequestData) {
        List<Map<String, Object>> mailerList = new ArrayList<>();
        Map<String, Object> newMailer = new HashMap<>();
        newMailer.put("Status", mailer.getStatus());
        newMailer.put("Expected_Delivery", formatDateForZoho(mailer.getExpectedDeliveryDate()));
        newMailer.put("Reference_Id", mailer.getReferenceId());
        newMailer.put("Send_Date", formatDateTimeForZoho(mailer.getSendDate()));

        // Add new mailer to list for payload.
        mailerList.add(newMailer);

        Map<String, Object> body = new HashMap<>();
        body.put("Mailers", mailerList);
        body.put("User_Session_UUID", mailerRequestData.getUserSessionUUID());
        body.put("Solar_Insight_Lead_Id", mailerRequestData.getSolarInsightLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("data", List.of(body));

        return payload;
    }




    private Optional<Map<String, Object>> createPayload(FetchedSubformData fetchedData, TrackingEventData eventData) {
        Map<String, Object> updatedMailer = new HashMap<>();

        // Find and update the specific mailer record
        for (MailerDTO mailerDTO : fetchedData.getMailerList()) {
            if (mailerDTO.getReferenceId().equals(eventData.getReferenceId())) {
                updatedMailer.put("id", mailerDTO.getRecordId());
                updatedMailer.put("Status", MailerStatus.fromLobEvent(eventData.getEvent()));
                updatedMailer.put("Expected_Delivery", formatDateForZoho(eventData.getExpectedDeliveryDate()));
                break;  // Stop after finding the correct mailer
            }
        }

        // If we found a mailer to update, wrap it in the Zoho structure
        if (!updatedMailer.isEmpty()) {
            Map<String, Object> body = new HashMap<>();
            body.put("id", fetchedData.getParentId());      // Parent record ID
            body.put("Mailers", List.of(updatedMailer));    // List containing the updated mailer

            // Wrap the parent record in "data" as expected by Zoho
            Map<String, Object> payload = new HashMap<>();
            payload.put("data", List.of(body));

            return Optional.of(payload);
        } else {
            return Optional.empty();
        }
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




    /**
     * Formats the given LocalDateTime into a Zoho-compatible date-time string.
     *
     * @param dateTime The LocalDateTime object to be formatted.
     * @return A string formatted for Zoho expected date-time format.
     */
    private String formatDateTimeForZoho(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        return dateTime.atZone(ZoneId.of("America/New_York")).format(formatter);
    }




    /**
     * Formats the given LocalDate into a Zoho-compatible date string.
     *
     * @param date The LocalDate object to be formatted.
     * @return A string formatted for Zoho expected date format.
     */
    private String formatDateForZoho(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }



}
