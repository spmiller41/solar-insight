package com.solar_insight.app.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.solar_insight.app.GeocodedLocation;
import com.solar_insight.app.solar.service.SolarBuildingInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SolarInsightRestController {

    private final SolarBuildingInsightService solarBuildingInsightService;

    @Autowired
    public SolarInsightRestController(SolarBuildingInsightService solarBuildingInsightService) {
        this.solarBuildingInsightService = solarBuildingInsightService;
    }

    @PostMapping("/preliminary_data")
    public void preliminaryDataController(@RequestBody PreliminaryDataDTO preliminaryDataDTO) {
        System.out.println(preliminaryDataDTO);

        GeocodedLocation geocodedLocation =
                new GeocodedLocation(preliminaryDataDTO.getLatitude(), preliminaryDataDTO.getLongitude());
        System.out.println("Geocode: " + geocodedLocation);

        JsonNode buildingInsightResponse = solarBuildingInsightService.getSolarData(geocodedLocation);
        System.out.println(buildingInsightResponse);
    }
}
