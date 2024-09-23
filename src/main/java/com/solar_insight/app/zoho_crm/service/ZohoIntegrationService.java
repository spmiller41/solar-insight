package com.solar_insight.app.zoho_crm.service;

import com.solar_insight.app.dao.*;
import com.solar_insight.app.dto.UserSessionDTO;
import com.solar_insight.app.entity.*;
import com.solar_insight.app.zoho_crm.logs.ZohoIntegrationLogger;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ZohoIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(ZohoIntegrationService.class);

    private final UserSessionDAO userSessionDAO;
    private final AddressDAO addressDAO;
    private final SolarEstimateDAO estimateDAO;
    private final ZohoRequestService zohoRequestService;
    private final ContactDAO contactDAO;
    private final ContactAddressDAO contactAddressDAO;

    @Autowired
    public ZohoIntegrationService(UserSessionDAO userSessionDAO,
                                  AddressDAO addressDAO,
                                  SolarEstimateDAO estimateDAO,
                                  ZohoRequestService zohoRequestService,
                                  ContactDAO contactDAO,
                                  ContactAddressDAO contactAddressDAO) {

        this.userSessionDAO = userSessionDAO;
        this.addressDAO = addressDAO;
        this.estimateDAO = estimateDAO;
        this.zohoRequestService = zohoRequestService;
        this.contactDAO = contactDAO;
        this.contactAddressDAO = contactAddressDAO;
    }




    @Transactional
    public void sendAddressAndEstimate(UserSessionDTO userSessionDTO) {
        Optional<UserSession> optUserSession = userSessionDAO.findBySessionUUID(userSessionDTO.getSessionUUID());
        if (optUserSession.isEmpty()) {
            ZohoIntegrationLogger.logUserSessionNotFoundErr(userSessionDTO, logger);
            return;
        }

        Optional<Address> optAddress = addressDAO.findById(optUserSession.get().getAddressId());
        if (optAddress.isEmpty()) {
            ZohoIntegrationLogger.logAddressNotFoundErr(optUserSession.get(), logger);
            return;
        }

        // If the address has already been added to Zoho, return.
        if (optAddress.get().getZohoSolarInsightLeadId() != null) {
            ZohoIntegrationLogger.logExistingUserInfo(optAddress.get(), logger);
            return;
        }

        Optional<SolarEstimate> optSolarEstimate = estimateDAO.findByAddressId(optAddress.get().getId());
        if (optSolarEstimate.isEmpty()) {
            ZohoIntegrationLogger.logSolarEstimateNotFoundErr(optAddress.get(), logger);
            return;
        }

        // Send address, estimate, and session uuid to Zoho CRM.
        Optional<String> optZohoRecordId = zohoRequestService
                .createSolarInsightLead(optAddress.get(), optSolarEstimate.get(), optUserSession.get().getSessionUUID());

        // Update this address with the returned record id from Zoho.
        if (optZohoRecordId.isPresent()) {
            Address address = optAddress.get();
            address.setZohoSolarInsightLeadId(optZohoRecordId.get());
            address = addressDAO.update(address);
            ZohoIntegrationLogger.logSuccessfulPostInfo(address.getZohoSolarInsightLeadId(), logger);
        } else {
            ZohoIntegrationLogger.logZohoRecordCreationErr(optSolarEstimate.get(), logger);
        }
    }




    @Transactional
    public void addContactToEstimate(ContactAddress contactAddress) {
        Optional<Contact> optContact = contactDAO.findById(contactAddress.getContactId());
        Optional<Address> optAddress = addressDAO.findById(contactAddress.getAddressId());

        if (optContact.isEmpty() || optAddress.isEmpty()) {
            ZohoIntegrationLogger.logMissingLeadDataErr(contactAddress, logger);
            return;
        }

        try {
            zohoRequestService.updateSolarInsightLead(optContact.get(), optAddress.get());
            ZohoIntegrationLogger.logContactUpdateSuccessInfo(optAddress.get().getZohoSolarInsightLeadId(), logger);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }




    @Transactional
    public void addBookingToEstimate(BookedConsultation bookedConsultation) {
        Optional<ContactAddress> optContactAddress = contactAddressDAO.findById(bookedConsultation.getContactAddressId());
        if (optContactAddress.isEmpty()) {
            ZohoIntegrationLogger.logContactAddressNotFoundErr(bookedConsultation, logger);
            return;
        }

        Optional<Address> optAddress = addressDAO.findById(optContactAddress.get().getAddressId());
        if (optAddress.isEmpty()) {
            ZohoIntegrationLogger.logAddressNotFoundErr(optContactAddress.get(), logger);
            return;
        }

        try {
            zohoRequestService.updateSolarInsightLead(bookedConsultation, optAddress.get());
            ZohoIntegrationLogger.logBookingUpdateSuccessInfo(optAddress.get().getZohoSolarInsightLeadId(), logger);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
        }
    }

}
