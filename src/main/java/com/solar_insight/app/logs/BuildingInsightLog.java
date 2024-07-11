package com.solar_insight.app.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;

public class BuildingInsightLog {

    public static void parsingError(JsonProcessingException ex, Logger logger) {
        logger.error("Error parsing building insight response. Message: {}", ex.getMessage());
    }

}
