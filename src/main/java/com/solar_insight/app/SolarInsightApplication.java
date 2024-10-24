package com.solar_insight.app;

import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.PostcardMailer;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.lob_mailer.dto.CreateMailerResponse;
import com.solar_insight.app.lob_mailer.service.MailerDataService;
import com.solar_insight.app.lob_mailer.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	/*
	@Autowired
	private MailerService mailerService;

	@Autowired
	private MailerDataService mailerDataService;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {
			String testSessionUUID = "a62ae04c-4429-487d-b45e-a04eb6d7b22c";

			Optional<Map<String, Object>> optMap = mailerDataService.getAddressAndEstimateData(testSessionUUID);
			if (optMap.isPresent()) {
				Address address = (Address) optMap.get().get("address");
				SolarEstimate estimate = (SolarEstimate) optMap.get().get("solar_estimate");

				Optional<CreateMailerResponse> optMailerResponse = mailerService.sendPostcard(address, estimate);
				if (optMailerResponse.isPresent()) {
					Optional<PostcardMailer> optMailer = mailerDataService.processMailerInsert(optMailerResponse.get(), address);
                    optMailer.ifPresent(postcardMailer -> System.out.println("Mailer inserted: " + postcardMailer));
				} else {
					System.out.println("There was an issue parsing mailer response or the request itself.");
				}
			}
		};
	}
	 */

}




















