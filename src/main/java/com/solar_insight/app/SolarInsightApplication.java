package com.solar_insight.app;

import com.solar_insight.app.dao.ContactAddressDAO;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.Contact;
import com.solar_insight.app.entity.ContactAddress;
import com.solar_insight.app.entity.SolarEstimate;
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

	/*
	@Autowired
	private ZohoRequestService solarInsightService;

	@Autowired
	private TokenService tokenService;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {
			Address address = new Address();
			address.setZohoSolarInsightLeadId("3880966000279568167");

			Contact contact = new Contact();
			contact.setFirstName("Sean");
			contact.setLastName("Miller");
			contact.setEmail("spmiller41@gmail.com");
			contact.setPhone("6318895508");

			solarInsightService.updateSolarInsightLead(contact, address);
		};

	}
	*/

}




















