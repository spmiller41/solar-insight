package com.solar_insight.app.rest;

import org.apache.coyote.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SolarInsightRestController {

    @PostMapping("/preliminary_data")
    public void preliminaryDataController(@RequestBody PreliminaryDataDTO preliminaryDataDTO) {
        System.out.println(preliminaryDataDTO);
    }
}
