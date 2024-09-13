package com.solar_insight.app;

import com.solar_insight.app.zoho_crm.enums.ZohoModuleAccess;
import com.solar_insight.app.zoho_crm.service.TokenService;
import com.solar_insight.app.zoho_crm.service.ZohoRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SolarInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarInsightApplication.class, args);
	}

	@Autowired
	private ZohoRequestService solarInsightService;

	@Autowired
	private TokenService tokenService;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {

			/*

			Address address = new Address();
			address.setStreet("155 Fake St");
			address.setCity("Test City");
			address.setState("New York");
			address.setZip("11967");

			SolarEstimate solarEstimate = new SolarEstimate();
			solarEstimate.setMonthlyBill(250);
			solarEstimate.setCostWithSolar(59000);
			solarEstimate.setCostWithoutSolar(29000);
			solarEstimate.setSystemSizeDc(9200);
			solarEstimate.setPanelCount(23);
			solarEstimate.setIncentives(12045);
			solarEstimate.setEstimatedSavings(42000);
			solarEstimate.setAnnualProductionAc(9925);

			solarInsightService.createLeadPreliminaryData(address, solarEstimate, UUID.randomUUID().toString());

			 */

		};

	}

}




















