package com.solar_insight.app;

import com.solar_insight.app.solar.SolarBuildingInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@SpringBootApplication
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	@Autowired
	private RestTemplate restTemplate;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {

			String username = "sean@gopowersolutions.com";
			String password = "Power200";
			String apiUrl = "https://secure.rateacuity.com/RateAcuityJSONAPI/api";

			// Step 1: Get Utility by Zip Code
			String zipCode = "11951";
			UriComponentsBuilder utilityUriBuilder = UriComponentsBuilder.fromHttpUrl(apiUrl + "/utilitybyzip/" + zipCode)
					.queryParam("p1", username)
					.queryParam("p2", password);

			String utilityUri = utilityUriBuilder.toUriString();
			HttpHeaders headers = new HttpHeaders();
			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<String> utilityResponse = restTemplate.exchange(utilityUri, HttpMethod.GET, entity, String.class);

			if (utilityResponse.getStatusCode() == HttpStatus.OK) {
				System.out.println("Utility Response:");
				System.out.println(utilityResponse.getBody());

				// Assuming you parse the response to get the UtilityID (e.g., "502")
				String utilityId = "219";

				// Step 2: Get Schedules for the Utility
				UriComponentsBuilder scheduleUriBuilder = UriComponentsBuilder.fromHttpUrl(apiUrl + "/schedule/" + utilityId)
						.queryParam("p1", username)
						.queryParam("p2", password);

				String scheduleUri = scheduleUriBuilder.toUriString();
				ResponseEntity<String> scheduleResponse = restTemplate.exchange(scheduleUri, HttpMethod.GET, entity, String.class);

				if (scheduleResponse.getStatusCode() == HttpStatus.OK) {
					System.out.println("Schedule Response:");
					System.out.println(scheduleResponse.getBody());

					// Assuming you parse the response to get the ScheduleID (e.g., "16334")
					String scheduleId = "4957";

					// Step 3: Get Rate Details using ScheduleID
					UriComponentsBuilder rateUriBuilder = UriComponentsBuilder.fromHttpUrl(apiUrl + "/energy/" + scheduleId)
							.queryParam("p1", username)
							.queryParam("p2", password);

					String rateUri = rateUriBuilder.toUriString();
					ResponseEntity<String> rateResponse = restTemplate.exchange(rateUri, HttpMethod.GET, entity, String.class);

					if (rateResponse.getStatusCode() == HttpStatus.OK) {
						System.out.println("Rate Response:");
						System.out.println(rateResponse.getBody());
					} else {
						System.out.println("Failed to fetch rate details: " + rateResponse.getStatusCode());
					}
				} else {
					System.out.println("Failed to fetch schedules: " + scheduleResponse.getStatusCode());
				}
			} else {
				System.out.println("Failed to fetch utility rates: " + utilityResponse.getStatusCode());
			}
		};

	}



}