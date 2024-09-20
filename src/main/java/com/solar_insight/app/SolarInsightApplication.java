package com.solar_insight.app;

import com.solar_insight.app.dao.ContactAddressDAO;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.Contact;
import com.solar_insight.app.entity.ContactAddress;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.ycbm.service.BookingUrlService;
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
	private BookingUrlService bookingUrlService;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {
			ContactAddress contactAddress = new ContactAddress();
			contactAddress.setAddressId(1);
			contactAddress.setContactId(1);
			contactAddress.setId(1);

			String sessionUUID= "01112ea8-1056-45cf-8012-6c312aff7d82";

			String queryUrl = bookingUrlService.buildQueryUrl(contactAddress, sessionUUID);
			System.out.
			println("Booking Query URL: " + queryUrl);
		};

	}
	 */


}




















