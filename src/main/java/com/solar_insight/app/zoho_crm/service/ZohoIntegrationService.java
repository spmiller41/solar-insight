package com.solar_insight.app.zoho_crm.service;

import com.solar_insight.app.dao.AddressDAO;
import com.solar_insight.app.dao.SolarEstimateDAO;
import com.solar_insight.app.dao.UserSessionDAO;
import com.solar_insight.app.dto.UserSessionDTO;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.entity.UserSession;
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

    @Autowired
    public ZohoIntegrationService(UserSessionDAO userSessionDAO, AddressDAO addressDAO,
                                  SolarEstimateDAO estimateDAO, ZohoRequestService solarInsightService) {
        this.userSessionDAO = userSessionDAO;
        this.addressDAO = addressDAO;
        this.estimateDAO = estimateDAO;
        this.solarInsightService = solarInsightService;
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

                // Find solar estimate, make sure it's present.
                Optional<SolarEstimate> optSolarEstimate = estimateDAO.findByAddressId(address.getId());
                if (optSolarEstimate.isPresent()) {
                    SolarEstimate solarEstimate = optSolarEstimate.get();

                    // Send address, estimate, and session uuid to Zoho CRM.
                    Optional<String> optZohoRecordId = solarInsightService
                            .createLeadPreliminaryData(address, solarEstimate, userSession.getSessionUUID());

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

}
