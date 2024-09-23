package com.solar_insight.app.zoho_crm.logs;

import com.solar_insight.app.dto.UserSessionDTO;
import com.solar_insight.app.entity.*;
import org.slf4j.Logger;

public class ZohoIntegrationLogger {

    public static void logExistingUserInfo(Address address, Logger logger) {
        logger.info("User with existing address/estimate in Zoho began new session. Address: {}", address);
    }

    public static void logSuccessfulPostInfo(String zohoRecordId, Logger logger) {
        logger.info("Estimate/Address added to Zoho successfully. Address updated with the Zoho Record Id: {}", zohoRecordId);
    }

    public static void logUserSessionNotFoundErr(UserSessionDTO userSessionDTO, Logger logger) {
        logger.error("User Session could not be found while attempting to add address and estimate to Zoho. User Session Data: {}", userSessionDTO);
    }

    public static void logAddressNotFoundErr(UserSession userSession, Logger logger) {
        logger.error("Address could not be found while attempting to add address and estimate to Zoho. User Session Data: {}", userSession);
    }

    public static void logAddressNotFoundErr(ContactAddress contactAddress, Logger logger) {
        logger.error("Could not locate Address while attempting to update Zoho with booking data. Generated Lead Data: {}", contactAddress);
    }

    public static void logSolarEstimateNotFoundErr(Address address, Logger logger) {
        logger.error("Solar Estimate could not be found while attempting to add address and estimate to Zoho. Address Data: {}", address);
    }

    public static void logZohoRecordCreationErr(SolarEstimate solarEstimate, Logger logger) {
        logger.error("Address/Estimate may not have been added to Zoho. Zoho Record ID was not returned. Estimate Data: {}", solarEstimate);
    }

    public static void logMissingLeadDataErr(ContactAddress contactAddress, Logger logger) {
        logger.error(
                "One or more records could not be found for this generated lead while attempting to add contact to estimate in Zoho. Generated Lead: {}",
                contactAddress);
    }

    public static void logContactAddressNotFoundErr(BookedConsultation bookedConsultation, Logger logger) {
        logger.error("Could not locate ContactAddress while attempting to update Zoho with booking data. Booking Data: {}", bookedConsultation);
    }

    public static void logContactUpdateSuccessInfo(String zohoRecordId, Logger logger) {
        logger.info("Contact Info added to Zoho record successfully. Zoho Record Id: {}", zohoRecordId);
    }

    public static void logBookingUpdateSuccessInfo(String zohoRecordId, Logger logger) {
        logger.info("Booking Info added to Zoho record successfully. Zoho Record Id: {}", zohoRecordId);
    }

}
