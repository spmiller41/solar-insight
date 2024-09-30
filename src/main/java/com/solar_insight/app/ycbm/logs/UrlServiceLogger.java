package com.solar_insight.app.ycbm.logs;

import com.solar_insight.app.entity.ContactAddress;
import com.solar_insight.app.entity.UserSession;
import org.slf4j.Logger;

public class UrlServiceLogger {

    public static void logMissingUserSessionErr(String sessionUUID, Logger logger) {
        logger.error("User Session could not be located based on the Session UUID when attempting to create booking query url. Session UUID: {}", sessionUUID);
    }

    public static void logMissingLeadErr(UserSession userSession, Logger logger) {
        logger.error("ContactAddress (Generated Lead) could not be located based on the User Session when attempting to create booking query url. User Session Id: {}", userSession.getId());
    }

    public static void logMissingLeadDataErr(ContactAddress contactAddress, Logger logger) {
        logger.error("Could not locate either the contact or address via the generated lead in ContactAddress entity while attempting to created booking query url: {}", contactAddress);
    }

}
