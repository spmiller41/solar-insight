package com.solar_insight.app.zoho_crm.service;

import com.solar_insight.app.dao.AddressDAO;
import com.solar_insight.app.dao.SolarEstimateDAO;
import com.solar_insight.app.dao.UserSessionDAO;
import com.solar_insight.app.dto.UserSessionDTO;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.entity.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ZohoIntegrationService {

    /*
     * Retrieve User Session via sessionUUID
     * Retrieve Address from User Session via addressId
     * Retrieve Solar Estimate from Address via addressId
     * Post Address and Solar Estimate to Zoho
     * Note: Perhaps add this to one class.
     */

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
                    solarInsightService.createLeadPreliminaryData(address, solarEstimate, userSession.getSessionUUID());
                }
            }
        }
    }

}
