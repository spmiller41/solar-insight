package com.solar_insight.app.ycbm.service;

import com.solar_insight.app.dao.AddressDAO;
import com.solar_insight.app.dao.ContactAddressDAO;
import com.solar_insight.app.dao.ContactDAO;
import com.solar_insight.app.dao.UserSessionDAO;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.Contact;
import com.solar_insight.app.entity.ContactAddress;
import com.solar_insight.app.entity.UserSession;
import com.solar_insight.app.ycbm.BookingUrlBuilder;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingUrlService {

    @Value("${booking.page.base.url}")
    private String baseUrl;

    private final ContactAddressDAO contactAddressDAO;
    private final UserSessionDAO userSessionDAO;
    private final AddressDAO addressDAO;
    private final ContactDAO contactDAO;

    @Autowired
    public BookingUrlService(ContactAddressDAO contactAddressDAO, UserSessionDAO userSessionDAO,
                             AddressDAO addressDAO, ContactDAO contactDAO) {

        this.contactAddressDAO = contactAddressDAO;
        this.userSessionDAO = userSessionDAO;
        this.addressDAO = addressDAO;
        this.contactDAO = contactDAO;
    }

    @Transactional
    public String buildQueryUrl(String sessionUUID) {
        // Note logging should be added for identifying where a session uuid mapping may be broken.
        // In addition, if mapping is broken, there will be no purpose of creating a query URL.

        Optional<UserSession> optUserSession = userSessionDAO.findBySessionUUID(sessionUUID);
        if (optUserSession.isEmpty()) {
            System.err.println("User Session could not be located based on the " +
                    "Session UUID when attempting to create booking query url. Session UUID: " + sessionUUID);
            return baseUrl;
        }

        Optional<ContactAddress> optGeneratedLead = contactAddressDAO.findByUserSession(optUserSession.get());
        if (optGeneratedLead.isEmpty()) {
            System.err.println("ContactAddress (Generated Lead) could not be located based on the " +
                    "User Session when attempting to create booking query url. User Session: " + optUserSession.get());
            return baseUrl;
        }

        Optional<Address> optAddress = addressDAO.findById(optGeneratedLead.get().getAddressId());
        Optional<Contact> optContact = contactDAO.findById(optGeneratedLead.get().getContactId());

        if (optAddress.isEmpty() || optContact.isEmpty()) {
            // Add more organized error logging here
            System.err.println("Could not locate either the contact or address via the generated lead in " +
                    "ContactAddress entity while attempting to created booking query url: " + optGeneratedLead.get());
            return baseUrl;
        }

        BookingUrlBuilder bookingUrlBuilder = new BookingUrlBuilder();
        Optional<String> optQueryUrl =
                bookingUrlBuilder.generate(baseUrl, optContact.get(), sessionUUID);

        // If the value (booking query url) is present it's returned. If not, the value supplied (baseUrl) is returned)
        return optQueryUrl.orElseGet(() -> baseUrl);
    }

}
