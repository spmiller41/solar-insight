package com.solar_insight.app;

import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.SolarEstimate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {
			String templateId = "tmpl_4b243da5ff5fd97";
			String size = "6x9";

			Address address = new Address();
			address.setStreet("254 Dogwood Road West");
			address.setCity("Mastic Beach");
			address.setState("NY");
			address.setZip("11951");

			SolarEstimate solarEstimate = new SolarEstimate();
			solarEstimate.setEstimatedSavings(49500);
			solarEstimate.setIncentives(13895);

			Map<String, Object> mailerRequest = buildMailerRequest(address, solarEstimate, templateId, size);
			System.out.println(mailerRequest);

		};
	}

	private Map<String, String> buildFromAddress() {
		Map<String, String> fromAddress = new HashMap<>();
		fromAddress.put("name", "Power Solutions");
		fromAddress.put("address_line1", "2060 Ocean Ave");
		fromAddress.put("address_city", "Ronkonkoma");
		fromAddress.put("address_state", "NY");
		fromAddress.put("address_zip", "11779");
		fromAddress.put("address_country", "US");
		return fromAddress;
	}

	private Map<String, Object> buildMailerRequest(Address address, SolarEstimate solarEstimate, String templateId, String size) {
		Map<String, Object> request = new HashMap<>();

		// Inline address object
		Map<String, Object> toAddress = new HashMap<>();
		toAddress.put("address_line1", address.getStreet());
		toAddress.put("address_city", address.getCity());
		toAddress.put("address_state", address.getState());
		toAddress.put("address_zip", address.getZip());
		toAddress.put("address_country", "US");

		// Merge variables for template
		Map<String, Object> mergeVariables = new HashMap<>();
		mergeVariables.put("savings", solarEstimate.getEstimatedSavings());
		mergeVariables.put("incentives", solarEstimate.getIncentives());

		// Create request payload
		request.put("to", toAddress);
		request.put("from", buildFromAddress());
		request.put("merge_variables", mergeVariables);
		request.put("front", templateId);
		request.put("size", size);
		request.put("mail_type", "usps_first_class");

		return request;
	}

}




















