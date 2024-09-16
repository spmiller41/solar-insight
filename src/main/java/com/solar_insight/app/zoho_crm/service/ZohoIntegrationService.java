package com.solar_insight.app.zoho_crm.service;

import com.solar_insight.app.dao.AddressDAO;
import com.solar_insight.app.dao.ContactDAO;
import com.solar_insight.app.dao.SolarEstimateDAO;
import com.solar_insight.app.dao.UserSessionDAO;
import com.solar_insight.app.dto.UserSessionDTO;
import com.solar_insight.app.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ZohoIntegrationService {

    private final UserSessionDAO userSessionDAO;
    private final AddressDAO addressDAO;
    private final SolarEstimateDAO estimateDAO;
    private final ZohoRequestService solarInsightService;
    private final ContactDAO contactDAO;

    @Autowired
    public ZohoIntegrationService(UserSessionDAO userSessionDAO, AddressDAO addressDAO,
                                  SolarEstimateDAO estimateDAO, ZohoRequestService solarInsightService, ContactDAO contactDAO) {
        this.userSessionDAO = userSessionDAO;
        this.addressDAO = addressDAO;
        this.estimateDAO = estimateDAO;
        this.solarInsightService = solarInsightService;
        this.contactDAO = contactDAO;
    }

    @Transactional
    public void sendAddressAndEstimate(UserSessionDTO userSessionDTO) {
        // Find user session, make sure it's present.
        Optional<UserSession> optUserSession = userSessionDAO.findBySessionUUID(userSessionDTO.getSessionUUID());
        if (optUserSession.isPresent()) {
            UserSession userSession = optUserSession.get();

            // Find address, make sure it's present.
            Optional<Address> optAddress = addressDAO.findById(userSession.getAddressId());
            if (optAddress.isPresent()) {
                Address address = optAddress.get();

                // If the address has already been added to Zoho, return.
                if (address.getZohoSolarInsightLeadId() != null) {
                    // Add organized info logging here
                    return;
                }

                // Find solar estimate, make sure it's present.
                Optional<SolarEstimate> optSolarEstimate = estimateDAO.findByAddressId(address.getId());
                if (optSolarEstimate.isPresent()) {
                    SolarEstimate solarEstimate = optSolarEstimate.get();

                    // Send address, estimate, and session uuid to Zoho CRM.
                    Optional<String> optZohoRecordId = solarInsightService
                            .createSolarInsightLead(address, solarEstimate, userSession.getSessionUUID());

                    // Update this address with the returned record id from Zoho.
                    if (optZohoRecordId.isPresent()) {
                        address.setZohoSolarInsightLeadId(optZohoRecordId.get());
                        address = addressDAO.update(address);
                        System.out.println("The address was updated with the Zoho Record Id: " + address.getZohoSolarInsightLeadId());
                        // Add info logging here
                    } else {
                        // Add more organized error logging here
                        System.err.println("There was an issue retrieving and updating the address with the Zoho Record Id.");
                    }
                }
            }
        }
    }

    @Transactional
    public void addContactToEstimate(ContactAddress contactAddress) {
        Optional<Contact> optContact = contactDAO.findById(contactAddress.getContactId());
        Optional<Address> optAddress = addressDAO.findById(contactAddress.getAddressId());

        if (optContact.isEmpty() || optAddress.isEmpty()) {
            // Add organized error logging here
            System.err.println("One or more records could not be found for this generated lead.");
            return;
        }

        solarInsightService.updateSolarInsightLead(optContact.get(), optAddress.get());
    }

}
