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
import com.solar_insight.app.ycbm.logs.UrlServiceLogger;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingUrlService {

    private static final Logger logger = LoggerFactory.getLogger(BookingUrlService.class);

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
            UrlServiceLogger.logMissingUserSessionErr(sessionUUID, logger);
            return baseUrl;
        }

        Optional<ContactAddress> optGeneratedLead = contactAddressDAO.findByUserSession(optUserSession.get());
        if (optGeneratedLead.isEmpty()) {
            UrlServiceLogger.logMissingLeadErr(optUserSession.get(), logger);
            return baseUrl;
        }

        Optional<Address> optAddress = addressDAO.findById(optGeneratedLead.get().getAddressId());
        Optional<Contact> optContact = contactDAO.findById(optGeneratedLead.get().getContactId());

        if (optAddress.isEmpty() || optContact.isEmpty()) {
            UrlServiceLogger.logMissingLeadDataErr(optGeneratedLead.get(), logger);
            return baseUrl;
        }

        BookingUrlBuilder bookingUrlBuilder = new BookingUrlBuilder();
        Optional<String> optQueryUrl =
                bookingUrlBuilder.generate(baseUrl, optContact.get(), sessionUUID);

        // If the value (booking query url) is present it's returned. If not, the value supplied (baseUrl) is returned)
        return optQueryUrl.orElseGet(() -> baseUrl);
    }

}
