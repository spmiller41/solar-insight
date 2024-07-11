package com.solar_insight.app.logs;

import com.solar_insight.app.solar.SolarBuildingInsightService;
import org.slf4j.Logger;

public class RestTemplateLog {

    public static <T> void requestError(Class<T> classType, Exception ex, Logger logger) {
        if (classType.equals(SolarBuildingInsightService.class)) {
            logger.error("Failed to retrieve building insight data via Google Maps Solar API: {}", ex.getMessage());
        }
    }

}
