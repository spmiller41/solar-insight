package com.solar_insight.app.lob_mailer.service;

import com.solar_insight.app.dao.AddressDAO;
import com.solar_insight.app.dao.PostcardMailerDAO;
import com.solar_insight.app.dao.SolarEstimateDAO;
import com.solar_insight.app.dao.UserSessionDAO;
import com.solar_insight.app.entity.Address;
import com.solar_insight.app.entity.PostcardMailer;
import com.solar_insight.app.entity.SolarEstimate;
import com.solar_insight.app.entity.UserSession;
import com.solar_insight.app.lob_mailer.MailerStatus;
import com.solar_insight.app.lob_mailer.dto.CreateMailerResponse;
import com.solar_insight.app.lob_mailer.dto.TrackingEventData;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MailerDataService {

    private static final Logger logger = LoggerFactory.getLogger(MailerDataService.class);

    private final PostcardMailerDAO postcardMailerDAO;
    private final AddressDAO addressDAO;
    private final UserSessionDAO userSessionDAO;
    private final SolarEstimateDAO solarEstimateDAO;

    @Autowired
    public MailerDataService(PostcardMailerDAO postcardMailerDAO, AddressDAO addressDAO, UserSessionDAO userSessionDAO, SolarEstimateDAO solarEstimateDAO) {
        this.postcardMailerDAO = postcardMailerDAO;
        this.addressDAO = addressDAO;
        this.userSessionDAO = userSessionDAO;
        this.solarEstimateDAO = solarEstimateDAO;
    }

    public PostcardMailer updateMailer(PostcardMailer mailer) { return postcardMailerDAO.update(mailer); }

    public Optional<List<PostcardMailer>> getAllMailerByAddressId(int addressId) {
        return postcardMailerDAO.getAllMailerByAddressId(addressId);
    }




    /**
     * Retrieves address and solar estimate data for the provided session UUID.
     * <p>
     * Map keys:
     * <p>
     * "address" : Address object
     * <p>
     * "solar_estimate" : SolarEstimate object
     *
     * @param sessionUUID the session UUID to lookup
     * @return an Optional containing a map with the address and solar estimate, or an empty Optional if not found
     */
    @Transactional
    public Optional<Map<String, Object>> getAddressAndEstimateData(String sessionUUID) {
        Map<String, Object> data = new HashMap<>();

        Optional<UserSession> optUserSession = userSessionDAO.findBySessionUUID(sessionUUID);
        if (optUserSession.isEmpty()) {
            logger.error("User Session could not be located while attempting to generate mailer. Session UUID: {}", sessionUUID);
            return Optional.empty();
        }

        Optional<Address> optAddress = addressDAO.findById(optUserSession.get().getAddressId());
        if (optAddress.isEmpty()) {
            logger.error("Address could not be located while attempting to generate mailer. Session UUID: {}", sessionUUID);
            return Optional.empty();
        }

        Optional<SolarEstimate> optSolarEstimate = solarEstimateDAO.findByAddressId(optAddress.get().getId());
        if (optSolarEstimate.isEmpty()) {
            logger.error("Solar Estimate could not be located while attempting to generate mailer. Address: {}", optAddress.get());
            return Optional.empty();
        }

        data.put("address", optAddress.get());
        data.put("solar_estimate", optSolarEstimate.get());

        return Optional.of(data);
    }



    @Transactional
    public Optional<PostcardMailer> processMailerInsert(CreateMailerResponse mailerResponse, Address address) {
        if (mailerResponse.getStatus().equals(MailerStatus.FAILED.getLobEvent())) {
            logger.error("Failed to create mailer. No insert executed. Response: {}", mailerResponse);
            return Optional.empty();
        } else {
            PostcardMailer mailer = new PostcardMailer(mailerResponse, address);
            postcardMailerDAO.insert(mailer);
            return Optional.of(mailer);
        }
    }



    @Transactional
    public Optional<PostcardMailer> updateMailerStatusAndData(TrackingEventData eventData) {
        Optional<PostcardMailer> optMailer = postcardMailerDAO.findMailerByReferenceId(eventData.getReferenceId());
        if (optMailer.isEmpty()) {
            logger.error("Could not located mailer while attempting to update tracking data. Reference Id: {}", eventData.getReferenceId());
            return Optional.empty();
        }

        // Update Mailer
        PostcardMailer mailer = optMailer.get();
        mailer.refreshMailerData(eventData);
        mailer = postcardMailerDAO.update(mailer);
        return Optional.of(mailer);
    }




}
