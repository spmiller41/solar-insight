package com.solar_insight.app.ycbm.logs;

import com.solar_insight.app.entity.Contact;
import org.slf4j.Logger;

public class UrlBuildLogger {

    public static void logFailedUrlBuildErr(Contact contact, String sessionUUID, Logger logger, Exception ex) {
        logger.error("Exception occurred while generating a query URL for Contact Id: {} --- Session UUID: {} --- Message: {}",
                contact.getId(), sessionUUID, ex.getMessage());
    }

}
