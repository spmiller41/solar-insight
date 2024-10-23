package com.solar_insight.app;

import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.lob_mailer.dto.CreateMailerResponse;
import com.solar_insight.app.lob_mailer.service.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@SpringBootApplication
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	/*
	@Autowired
	private MailerService mailerService;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {
			Address address = new Address();
			address.setStreet("254 Dogwood Road West");
			address.setCity("Mastic Beach");
			address.setState("NY");
			address.setZip("11951");

			SolarEstimate solarEstimate = new SolarEstimate();
			solarEstimate.setEstimatedSavings(49500);
			solarEstimate.setIncentives(13895);

			Optional<CreateMailerResponse> optMailerResponse = mailerService.sendPostcard(address, solarEstimate);
			if (optMailerResponse.isPresent()) {
				System.out.println(optMailerResponse.get());
			} else {
				System.out.println("There was an issue parsing mailer response or the request itself.");
			}
		};
	}
	 */

}




















