package com.solar_insight.app.zoho_crm.service;

import com.solar_insight.app.dao.*;
import com.solar_insight.app.dto.UserSessionDTO;
import com.solar_insight.app.lob_mailer.dto.TrackingEventData;
import com.solar_insight.app.zoho_crm.dto.CreateMailerRequestDTO;
import com.solar_insight.app.entity.*;
import com.solar_insight.app.lob_mailer.dto.CreateMailerResponse;
import com.solar_insight.app.lob_mailer.service.MailerDataService;
import com.solar_insight.app.lob_mailer.service.MailerService;
import com.solar_insight.app.zoho_crm.dto.FetchedSubformData;
import com.solar_insight.app.zoho_crm.logs.IntegrationLogger;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final MailerDataService mailerDataService;
    private final MailerService mailerService;

    @Autowired
    public ZohoIntegrationService(UserSessionDAO userSessionDAO,
                                  AddressDAO addressDAO,
                                  SolarEstimateDAO estimateDAO,
                                  ZohoRequestService zohoRequestService,
                                  ContactDAO contactDAO,
                                  ContactAddressDAO contactAddressDAO,
                                  MailerDataService mailerDataService, MailerService mailerService) {

        this.userSessionDAO = userSessionDAO;
        this.addressDAO = addressDAO;
        this.estimateDAO = estimateDAO;
        this.zohoRequestService = zohoRequestService;
        this.contactDAO = contactDAO;
        this.contactAddressDAO = contactAddressDAO;
        this.mailerDataService = mailerDataService;
        this.mailerService = mailerService;
    }




    @Transactional
    public void sendAddressAndEstimate(UserSessionDTO userSessionDTO) {
        Optional<UserSession> optUserSession = userSessionDAO.findBySessionUUID(userSessionDTO.getSessionUUID());
        if (optUserSession.isEmpty()) {
            IntegrationLogger.logUserSessionNotFoundErr(userSessionDTO, logger);
            return;
        }

        Optional<Address> optAddress = addressDAO.findById(optUserSession.get().getAddressId());
        if (optAddress.isEmpty()) {
            IntegrationLogger.logAddressNotFoundErr(optUserSession.get(), logger);
            return;
        }

        // If the address has already been added to Zoho, return.
        if (optAddress.get().getZohoSolarInsightLeadId() != null) {
            IntegrationLogger.logExistingUserInfo(optAddress.get(), logger);
            return;
        }

        Optional<SolarEstimate> optSolarEstimate = estimateDAO.findByAddressId(optAddress.get().getId());
        if (optSolarEstimate.isEmpty()) {
            IntegrationLogger.logSolarEstimateNotFoundErr(optAddress.get(), logger);
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
            IntegrationLogger.logSuccessfulPostInfo(address.getZohoSolarInsightLeadId(), logger);
        } else {
            IntegrationLogger.logZohoRecordCreationErr(optSolarEstimate.get(), logger);
        }
    }




    @Transactional
    public void addContactToEstimate(ContactAddress contactAddress) {
        Optional<Contact> optContact = contactDAO.findById(contactAddress.getContactId());
        Optional<Address> optAddress = addressDAO.findById(contactAddress.getAddressId());

        if (optContact.isEmpty() || optAddress.isEmpty()) {
            IntegrationLogger.logMissingLeadDataErr(contactAddress, logger);
            return;
        }

        try {
            zohoRequestService.updateSolarInsightLead(optContact.get(), optAddress.get());
            IntegrationLogger.logContactUpdateSuccessInfo(optAddress.get().getZohoSolarInsightLeadId(), logger);
        } catch (IllegalArgumentException ex) {
            IntegrationLogger.logIllegalArgumentException(optAddress.get(), logger);
        }
    }




    @Transactional
    public void addBookingToEstimate(BookedConsultation bookedConsultation) {
        Optional<ContactAddress> optContactAddress = contactAddressDAO.findById(bookedConsultation.getContactAddressId());
        if (optContactAddress.isEmpty()) {
            IntegrationLogger.logContactAddressNotFoundErr(bookedConsultation, logger);
            return;
        }

        Optional<Address> optAddress = addressDAO.findById(optContactAddress.get().getAddressId());
        if (optAddress.isEmpty()) {
            IntegrationLogger.logAddressNotFoundErr(optContactAddress.get(), logger);
            return;
        }

        try {
            zohoRequestService.updateSolarInsightLead(bookedConsultation, optAddress.get());
            IntegrationLogger.logBookingUpdateSuccessInfo(optAddress.get().getZohoSolarInsightLeadId(), logger);
        } catch (IllegalArgumentException ex) {
            IntegrationLogger.logIllegalArgumentException(optAddress.get(), logger);
        }
    }



    /**
     * Creates and sends a mailer, then synchronizes the data with Zoho CRM.
     * <p>
     * Checks if a mailer was sent within the last three months before sending a new one.
     * If successful, updates the record in Zoho CRM.
     *
     * @param mailerRequestData the request data containing user session information
     * @return a message indicating success, skipping, or any encountered errors
     */
    @Transactional
    public String createMailerAndSyncToZoho(CreateMailerRequestDTO mailerRequestData) {
        String sessionUUID = mailerRequestData.getUserSessionUUID();
        Optional<Map<String, Object>> optMap = mailerDataService.getAddressAndEstimateData(sessionUUID);

        if (optMap.isEmpty()) {
            return "Unable to retrieve address and estimate data. Please verify session UUID.";
        }

        Address address = (Address) optMap.get().get("address");
        SolarEstimate estimate = (SolarEstimate) optMap.get().get("solar_estimate");

        // Check if a mailer was sent within the last three months
        if (hasRecentMailer(address.getId())) {
            return "Mailer was already sent within the last three months. Skipping...";
        }

        // Send postcard mailer and process response
        Optional<CreateMailerResponse> optMailerResponse = mailerService.sendPostcard(address, estimate);

        if (optMailerResponse.isEmpty()) {
            return "Failed to send mailer. Please check mailer service for issues.";
        }

        // Insert mailer data and synchronize with Zoho
        Optional<PostcardMailer> optMailer = mailerDataService.processMailerInsert(optMailerResponse.get(), address);

        if (optMailer.isPresent()) {
            ResponseEntity<String> response = zohoRequestService.syncMailerRecordToZoho(optMailer.get(), mailerRequestData);

            if (response!= null && !response.getStatusCode().is4xxClientError() && !response.getStatusCode().is5xxServerError()) {
                return "Mailer has been processed! " +
                            "You may send another in three months. Expected Delivery: " + optMailer.get().getExpectedDeliveryDate();
            } else {
                return "Error occurred while updating Zoho record. Please verify Zoho API.";
            }
        }

        return "Your mailer has been processed, but we couldn’t update our records. Please reach out to support to confirm your mailer details.";
    }




    /**
     * Checks if a mailer has been sent within the last three months for the given address ID.
     */
    private boolean hasRecentMailer(int addressId) {
        Optional<List<PostcardMailer>> optMailerList = mailerDataService.getAllMailerByAddressId(addressId);
        if (optMailerList.isPresent()) {
            LocalDateTime threeMonthsAgo = LocalDate.now().minusMonths(3).atStartOfDay();
            return optMailerList.get().stream().anyMatch(mailer -> mailer.getSendDate().isAfter(threeMonthsAgo));
        }
        return false;
    }




    /**
     * Asynchronously updates the status and data of a mailer record based on tracking event information,
     * then retrieves and synchronizes the corresponding Zoho subform record.
     * <p>
     * This method includes a brief delay to accommodate timing issues with data availability,
     * performs transactional updates on the database, and syncs data back to Zoho CRM.
     *
     * @param trackingEventData the tracking event data used to update the mailer status
     */
    @Async
    @Transactional
    public void updateMailerAndSyncToZoho(TrackingEventData trackingEventData) {
        // Introduce a delay for processing
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            logger.error("Error occurred while sleeping thread for 5 seconds. Message: {}", ex.getMessage());
            Thread.currentThread().interrupt();  // Restore interrupted state for safe exit
            return;
        }

        // Update mailer status and retrieve updated mailer
        Optional<PostcardMailer> optMailer = mailerDataService.updateMailerStatusAndData(trackingEventData);
        if (optMailer.isEmpty()) {
            logger.error("Mailer data was not able to be updated. Event Data: {}", trackingEventData);
            return;
        }

        PostcardMailer mailer = optMailer.get();
        logger.info("Mailer data updated successfully. Mailer: {}", mailer);

        // Retrieve the related address record using the mailer's address ID
        Optional<Address> optAddress = addressDAO.findById(mailer.getAddressId());
        if (optAddress.isEmpty()) {
            logger.error("Could not locate address when attempting to update Zoho with tracking event data. Mailer Data: {}", mailer);
            return;
        }

        Address address = optAddress.get();

        // Get Solar Insight Lead from Zoho based on the address's Zoho lead ID
        Optional<String> optResponse = zohoRequestService.getSolarInsightLeadById(address.getZohoSolarInsightLeadId());
        if (optResponse.isEmpty()) {
            logger.error("Failed to fetch Zoho Solar Insight lead for Zoho ID: {}", address.getZohoSolarInsightLeadId());
            return;
        }

        // Deserialize response and update the Zoho subform record
        FetchedSubformData fetchedData = new FetchedSubformData(optResponse.get());
        zohoRequestService.updateMailerSubformRecord(fetchedData, trackingEventData);
    }


}
