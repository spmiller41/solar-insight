package com.solar_insight.app.zoho_crm.logs;

import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.SolarEstimate;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

public class RequestLogger {

    public static void logPostStatus(ResponseEntity<String> response, Logger logger) {
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Zoho POST executed. Success Status Code: {}", response.getStatusCode());
        } else if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            logger.error("Zoho POST executed. Error Status Code: {}", response.getStatusCode());
        } else {
            logger.info("Zoho POST executed. Status Code: {}", response.getStatusCode());
        }
    }

    public static void logPutStatus(ResponseEntity<String> response, Logger logger) {
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Zoho Put executed. Success Status Code: {}", response.getStatusCode());
        } else if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            logger.error("Zoho Put executed. Error Status Code: {}", response.getStatusCode());
        } else {
            logger.info("Zoho Put executed. Status Code: {}", response.getStatusCode());
        }
    }

    public static void logExceptionMsg(SolarEstimate estimate, Logger logger, Exception ex) {
        logger.error("Exception occurred while attempting to create Zoho record. Address Id: {} --- Estimate Id: {} --- Message: {}",
                estimate.getAddressId(), estimate.getId(), ex.getMessage());
    }

    public static void logExceptionMsg(Address address, Logger logger, Exception ex) {
        logger.error("Exception occurred while attempting to update Zoho record. Address Id: {} --- Zoho Record Id: {} --- Message: {}",
                address.getId(), address.getZohoSolarInsightLeadId(), ex.getMessage());
    }

    public static void logPayloadCreateErr(Object object, Logger logger, Exception ex) {
        logger.error("Exception during payload creation. Zoho Module: {} --- Message: {}", object.getClass(), ex.getMessage());
    }

    public static void logEmptyPayloadErr(Logger logger) {
        logger.error("JsonPayload is null, skipping API call.");
    }

    public static void logImageUploadStatus(ResponseEntity<String> response, Logger logger) {
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Zoho POST executed for image upload to Zoho. Success Status Code: {}", response.getStatusCode());
        } else if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            logger.error("Zoho POST executed for image upload to Zoho. Error Status Code: {}", response.getStatusCode());
        } else {
            logger.info("Zoho POST executed for image upload to Zoho. Status Code: {}", response.getStatusCode());
        }
    }

    public static void logImgExceptionMsg(Logger logger, Exception ex) {
        logger.error("Exception occurred while attempting to upload image to Zoho. Message: {}", ex.getMessage());
    }

}
