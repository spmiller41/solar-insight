package com.solar_insight.app.solar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class SolarBuildingInsightService {

    private static final Logger logger = LoggerFactory.getLogger(SolarBuildingInsightService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public SolarBuildingInsightService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }



}
