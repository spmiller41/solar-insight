package com.solar_insight.app.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.solar_insight.app.solar.GeocodedLocation;
import org.slf4j.Logger;

public class SolarBuildingInsightLog {

    public static void parsingError(GeocodedLocation location, JsonProcessingException ex, Logger logger) {
        logger.error("Error parsing building insight response. Location: {}, {} Message: {}",
                location.latitude(), location.longitude(), ex.getMessage());
    }

}
