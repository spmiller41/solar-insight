package com.solar_insight.app.zoho_crm.logs;

import org.slf4j.Logger;

public class TokenLogger {

    public static void logSuccessfulTokenInfo(String moduleType, Logger logger) {
        logger.info("Token refresh successful for module: {}", moduleType);
    }

    public static void logFailedTokenErr(String moduleType, Logger logger) {
        logger.error("Could not refresh access token for module: {}", moduleType);
    }

    public static void logTokenExceptionErr(String moduleType, Logger logger, Exception ex) {
        logger.error("Exception occurred while refreshing token for module: {} --- Message: {}", moduleType, ex.getMessage());
    }

}
