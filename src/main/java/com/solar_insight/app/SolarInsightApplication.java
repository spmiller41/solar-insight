package com.solar_insight.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	/*

	@Autowired
	private ZohoRequestService zohoRequestService;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {
			String testSolarInsightLeadId = "3880966000288700151";
			Optional<String> optResponse =
					zohoRequestService.getSolarInsightLeadById(testSolarInsightLeadId);
			optResponse.ifPresent(response -> {
				ZohoMailerSubformDTO mailerData = new ZohoMailerSubformDTO(optResponse.get());
				System.out.println(mailerData);
			});
		};
	}

	 */

}




















