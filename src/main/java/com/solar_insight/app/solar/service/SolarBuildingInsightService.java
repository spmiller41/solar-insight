package com.solar_insight.app.solar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solar_insight.app.logs.RestTemplateLog;
import com.solar_insight.app.logs.SolarBuildingInsightLog;
import com.solar_insight.app.GeocodedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SolarBuildingInsightService {

    private static final Logger logger = LoggerFactory.getLogger(SolarBuildingInsightService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.maps.api.key}")
    private String googleMapAPIKey;

    @Value("${google.building.insight.base.url}")
    private String googleBuildingInsightBaseURL;

    @Autowired
    public SolarBuildingInsightService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }




    /**
     * Fetches solar data for a given location.
     *
     * @param location The geocoded location.
     * @return The JSON node containing the solar data.
     */
    public JsonNode getSolarData(GeocodedLocation location) {
        String requestURL = String.format("%s?location.latitude=%s&location.longitude=%s&requiredQuality=HIGH&key=%s",
                googleBuildingInsightBaseURL, location.latitude(), location.longitude(), googleMapAPIKey);

        String response;
        try {
            response = restTemplate.getForObject(requestURL, String.class);
        } catch (Exception ex) {
            RestTemplateLog.requestError(SolarBuildingInsightService.class, ex, logger);
            return null;
        }

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException ex) {
            SolarBuildingInsightLog.parsingError(location, ex, logger);
            return null;
        }

        return jsonNode;
    }




}
