package com.solar_insight.app.service;

import com.solar_insight.app.dao.AddressDAO;
import com.solar_insight.app.dao.SolarEstimateDAO;
import com.solar_insight.app.dao.UserSessionDAO;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.entity.UserSession;
import com.solar_insight.app.dto.PreliminaryDataDTO;
import com.solar_insight.app.solar.utility.SolarOutcomeAnalysis;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PreliminaryDataService {

    private final AddressDAO addressDAO;
    private final UserSessionDAO userSessionDAO;
    private final SolarEstimateDAO solarEstimateDAO;

    @Autowired
    public PreliminaryDataService(AddressDAO addressDAO, UserSessionDAO userSessionDAO, SolarEstimateDAO solarEstimateDAO) {
        this.addressDAO = addressDAO;
        this.userSessionDAO = userSessionDAO;
        this.solarEstimateDAO = solarEstimateDAO;
    }

    /**
     * Saves a new user session, address, and solar estimate based on preliminary data.
     * <p>
     * If the address exists:
     * - Create a new user session.
     * - Update or insert a solar estimate for the address.
     * <p>
     * If the address is new:
     * - Insert the address, create a user session, and add a new solar estimate.
     * <p>
     * Transactional: Rolls back if any operation fails.
     *
     * @param data        User-provided preliminary data.
     * @param analysis    Solar analysis data for the estimate.
     * @param sessionUUID Unique ID for the current session.
     */
    @Transactional
    public void processUserSessionData(PreliminaryDataDTO data, SolarOutcomeAnalysis analysis, String sessionUUID) {
        Optional<Address> optionalAddress = addressDAO.findByCoordinatesOrAddress(data);
        if (optionalAddress.isEmpty()) {
            Address address = new Address(data);
            addressDAO.insert(address);
            // Add logging for new address here

            UserSession userSession = new UserSession(data, address, sessionUUID);
            userSessionDAO.insert(userSession);
            // Add logging for new user session here

            SolarEstimate solarEstimate = new SolarEstimate(analysis, address);
            solarEstimateDAO.insert(solarEstimate);
            // Add logging for new solar estimate here
        } else {
            Address address = optionalAddress.get();

            UserSession userSession = new UserSession(data, address, sessionUUID);
            userSessionDAO.insert(userSession);
            // Add logging for new user session here

            Optional<SolarEstimate> optionalSolarEstimate = solarEstimateDAO.findByAddressId(address.getId());
            if (optionalSolarEstimate.isPresent()) {
                SolarEstimate solarEstimate = optionalSolarEstimate.get();
                solarEstimate.refreshSolarEstimate(analysis);
                solarEstimate = solarEstimateDAO.update(solarEstimate);
                // Add logging for solar estimate update here
            } else {
                SolarEstimate solarEstimate = new SolarEstimate(analysis, address);
                solarEstimateDAO.insert(solarEstimate);
                // Add logging for new solar estimate here.
            }
        }
    }

}
