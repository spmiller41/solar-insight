package com.solar_insight.app.logs;

import com.solar_insight.app.dto.ContactInfoDTO;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.Contact;
import com.solar_insight.app.entity.ContactAddress;
import com.solar_insight.app.entity.UserSession;
import org.slf4j.Logger;

public class SessionDataLogger {

    // Preliminary Data Logging

    public static void logNewUserNotification(Logger logger) {
        logger.info("New User & Session - Creating User Session, Address, and Estimate.");
    }

    public static void logLiveSessionNewDataNotification(Logger logger, UserSession userSession) {
        logger.info("Live Session (User Session Exists). Creating Address and Estimate. Associating Address. --- Session UUID: {}", userSession.getSessionUUID());
    }

    public static void logNewSessionExistingDataNotification(Logger logger, Address address) {
        logger.info("Existing Address, New User Session - Creating new User Session and associating Address: {}", address);
    }

    public static void logLiveSessionAndAddressNotification(Logger logger, UserSession userSession, Address address) {
        logger.info("Address and User Session Exist. Checking if last address associated is current --- Session UUID: {} --- Address: {}",
                userSession.getSessionUUID(), address);
    }

    public static void logLiveSessionAddressResult(Logger logger, UserSession userSession, boolean addressChange) {
        if (!addressChange) {
            logger.info("No changes to address in live session, only updating Solar Estimate --- Session UUID: {}", userSession.getSessionUUID());
        } else {
            logger.info("Change to address in live session, managing currently associated address. Session UUID: {}", userSession.getSessionUUID());
        }
    }




    // Contact Info Logging

    public static void logMissingUserSessionErr(Logger logger, ContactInfoDTO contactInfo) {
        logger.error("User Session not found while attempting to manage contact info: {}", contactInfo);
    }

    public static void logMissingAddressErr(Logger logger, UserSession userSession) {
        logger.error("Address not found while attempting to manage contact info --- Session UUID: {}", userSession.getSessionUUID());
    }

    public static void logNewContactInfoNotification(Logger logger, ContactInfoDTO contactInfo) {
        logger.info("New Contact - Executing Insert. Contact Info: {}", contactInfo);
    }

    public static void logGeneratingLeadNotification(Logger logger, Contact contact, Address address) {
        logger.info("Creating generated lead. Contact Id: {} --- Address Id: {}", contact.getId(), address.getId());
    }

    public static void logExistingContactNewAddressNotification(Logger logger, Contact contact, Address address) {
        logger.info("Contact exists with new address. Generating new lead info (ContactAddress). Contact Id: {} --- Address Id: {}", contact.getId(), address.getId());
    }

    public static void logExistingLeadNotification(Logger logger, ContactAddress generatedLead) {
        logger.info("Existing Lead/User. Contact and Address already associated. Refreshing User Session with live session. ContactAddress Data: {}", generatedLead);
    }

    public static void logUserSessionUpdateNotification(Logger logger, ContactAddress contactAddress) {
        logger.info("Updated generated lead (ContactAddress): {}", contactAddress);
    }

}
